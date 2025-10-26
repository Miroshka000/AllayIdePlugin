package miroshka.allayideplugin.project

import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.ide.wizard.*
import com.intellij.ide.wizard.NewProjectWizardChainStep.Companion.nextStep
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.observable.properties.PropertyGraph
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.JavaSdk
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.roots.ui.configuration.JdkComboBox
import com.intellij.openapi.roots.ui.configuration.projectRoot.ProjectSdksModel
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vcs.VcsNotifier
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.ui.dsl.builder.*
import git4idea.commands.Git
import miroshka.allayideplugin.AllayIcons
import java.io.File
import java.nio.file.Paths
import javax.swing.Icon

class AllayGeneratorNewProjectWizard : GeneratorNewProjectWizard {
    
    override val id: String = "allayPlugin"
    
    override val name: String = "Allay Plugin"
    
    override val icon: Icon = AllayIcons.MODULE_ICON
    
    override val ordinal: Int = 200
    
    override fun createStep(context: WizardContext): NewProjectWizardStep {
        return RootNewProjectWizardStep(context)
            .nextStep(::NewProjectWizardBaseStep)
            .nextStep(::GitNewProjectWizardStep)
            .nextStep(::AllayProjectSettingsStep)
    }
}

class AllayProjectSettingsStep(parent: NewProjectWizardStep) : AbstractNewProjectWizardStep(parent) {
    
    private val groupIdProperty = propertyGraph.property("com.example")
    private val pluginNameProperty = propertyGraph.property("MyAllayPlugin")
    private val pluginVersionProperty = propertyGraph.property("1.0.0")
    private val pluginDescriptionProperty = propertyGraph.property("My Allay Plugin")
    private val pluginAuthorProperty = propertyGraph.property(System.getProperty("user.name"))
    private val allayApiVersionProperty = propertyGraph.property("0.15.0")
    private val mainClassProperty = propertyGraph.property("MyPlugin")
    
    private val sdksModel = ProjectSdksModel()
    private var selectedJdk: Sdk? = null
    
    override fun setupUI(builder: Panel) {
        with(builder) {
            row("JDK:") {
                val jdkComboBox = JdkComboBox(null, sdksModel, { it is JavaSdk }, null, null, null)
                cell(jdkComboBox)
                    .align(AlignX.FILL)
                    .onChanged {
                        selectedJdk = jdkComboBox.selectedJdk
                    }
                sdksModel.reset(null)
                jdkComboBox.reloadModel()
                selectedJdk = jdkComboBox.selectedJdk
            }
            
            row("Group ID:") {
                textField()
                    .bindText(groupIdProperty)
                    .columns(COLUMNS_LARGE)
            }
            
            row("Plugin Name:") {
                textField()
                    .bindText(pluginNameProperty)
                    .columns(COLUMNS_LARGE)
            }
            
            row("Plugin Version:") {
                textField()
                    .bindText(pluginVersionProperty)
                    .columns(COLUMNS_LARGE)
            }
            
            row("Plugin Description:") {
                textField()
                    .bindText(pluginDescriptionProperty)
                    .columns(COLUMNS_LARGE)
            }
            
            row("Author:") {
                textField()
                    .bindText(pluginAuthorProperty)
                    .columns(COLUMNS_LARGE)
            }
            
            row("Allay API Version:") {
                textField()
                    .bindText(allayApiVersionProperty)
                    .columns(COLUMNS_LARGE)
            }
            
            row("Main Class Name:") {
                textField()
                    .bindText(mainClassProperty)
                    .columns(COLUMNS_LARGE)
            }
        }
    }
    
    override fun setupProject(project: Project) {
        val projectLocation = context.projectDirectory
        val baseDir = projectLocation.toFile()
        
        val packagePath = groupIdProperty.get().replace('.', '/')
        
        val srcMainJava = File(baseDir, "src/main/java/$packagePath")
        val srcMainResources = File(baseDir, "src/main/resources")
        
        FileUtil.createDirectory(srcMainJava)
        FileUtil.createDirectory(srcMainResources)
        
        createBuildGradle(baseDir)
        createSettingsGradle(baseDir)
        createGradleProperties(baseDir)
        createMainClass(srcMainJava)
        createGradleWrapper(baseDir)
        
        selectedJdk?.let { jdk ->
            ApplicationManager.getApplication().runWriteAction {
                ProjectRootManager.getInstance(project).projectSdk = jdk
            }
        }
    }
    
    private fun createBuildGradle(baseDir: File) {
        val buildGradle = File(baseDir, "build.gradle.kts")
        buildGradle.writeText("""
plugins {
    java
    id("org.allaymc.gradle.plugin") version "0.1.0"
}

group = "${groupIdProperty.get()}"
version = "${pluginVersionProperty.get()}"

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

allay {
    api = "${allayApiVersionProperty.get()}"
    apiOnly = true
    
    plugin {
        name = "${pluginNameProperty.get()}"
        entrance = ".${mainClassProperty.get()}"
        version = "${pluginVersionProperty.get()}"
        description = "${pluginDescriptionProperty.get()}"
        authors += "${pluginAuthorProperty.get()}"
        api = ">= ${allayApiVersionProperty.get()}"
    }
}

dependencies {
}
""".trimIndent())
    }
    
    private fun createSettingsGradle(baseDir: File) {
        val settingsGradle = File(baseDir, "settings.gradle.kts")
        settingsGradle.writeText("""
rootProject.name = "${pluginNameProperty.get()}"

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}
""".trimIndent())
    }
    
    private fun createGradleProperties(baseDir: File) {
        val gradleProperties = File(baseDir, "gradle.properties")
        gradleProperties.writeText("""
org.gradle.jvmargs=-Xmx2048m
""".trimIndent())
    }
    
    private fun createMainClass(srcDir: File) {
        val mainClassFile = File(srcDir, "${mainClassProperty.get()}.java")
        mainClassFile.writeText("""
package ${groupIdProperty.get()};

import org.allaymc.api.plugin.Plugin;

public class ${mainClassProperty.get()} extends Plugin {
    @Override
    public void onLoad() {
        getLogger().info("${pluginNameProperty.get()} is loading...");
    }
    
    @Override
    public void onEnable() {
        getLogger().info("${pluginNameProperty.get()} has been enabled!");
    }
    
    @Override
    public void onDisable() {
        getLogger().info("${pluginNameProperty.get()} has been disabled!");
    }
}
""".trimIndent())
    }
    
    private fun createGradleWrapper(baseDir: File) {
        val gradleWrapperDir = File(baseDir, "gradle/wrapper")
        FileUtil.createDirectory(gradleWrapperDir)
        
        val gradleWrapperProperties = File(gradleWrapperDir, "gradle-wrapper.properties")
        gradleWrapperProperties.writeText("""
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-8.9-bin.zip
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
""".trimIndent())
    }
}

