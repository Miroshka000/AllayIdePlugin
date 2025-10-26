package miroshka.allayideplugin.project

import com.intellij.ide.util.projectWizard.JavaModuleBuilder
import com.intellij.ide.util.projectWizard.ModuleWizardStep
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.openapi.roots.ModifiableRootModel
import com.intellij.openapi.roots.ui.configuration.ModulesProvider
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.LocalFileSystem
import java.io.File

class AllayModuleBuilder : JavaModuleBuilder() {
    
    private var pluginName: String = "MyAllayPlugin"
    private var pluginVersion: String = "1.0.0"
    private var pluginDescription: String = "My Allay Plugin"
    private var pluginAuthor: String = System.getProperty("user.name")
    private var allayApiVersion: String = "0.15.0"
    private var mainClass: String = "MyPlugin"
    private var groupId: String = "com.example"
    
    override fun getBuilderId(): String = "allayPlugin"
    
    override fun getPresentableName(): String = "Allay Plugin"
    
    override fun getDescription(): String = "Allay plugin project for Minecraft server"
    
    override fun createWizardSteps(wizardContext: WizardContext, modulesProvider: ModulesProvider): Array<ModuleWizardStep> {
        val standardSteps = super.createWizardSteps(wizardContext, modulesProvider)
        val customStep = AllayModuleWizardStep(this)
        return standardSteps + customStep
    }
    
    override fun setupRootModel(rootModel: ModifiableRootModel) {
        super.setupRootModel(rootModel)
        
        val rootPath = contentEntryPath ?: return
        
        if (name.isNotEmpty()) {
            pluginName = name
        }
        
        createProjectStructure(rootPath)
        
        LocalFileSystem.getInstance().refresh(false)
    }
    
    private fun createProjectStructure(rootPath: String) {
        val packagePath = groupId.replace('.', '/')
        
        val srcMainJava = File(rootPath, "src/main/java/$packagePath")
        val srcMainResources = File(rootPath, "src/main/resources")
        
        FileUtil.createDirectory(srcMainJava)
        FileUtil.createDirectory(srcMainResources)
        
        createBuildGradle(rootPath, groupId)
        createSettingsGradle(rootPath)
        createGradleProperties(rootPath)
        createMainClass(srcMainJava, groupId)
        createGradleWrapper(rootPath)
    }
    
    private fun createBuildGradle(rootPath: String, groupId: String) {
        val buildGradle = File(rootPath, "build.gradle.kts")
        buildGradle.writeText("""
plugins {
    java
    id("org.allaymc.gradle.plugin") version "0.1.0"
}

group = "$groupId"
version = "$pluginVersion"

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

allay {
    api = "$allayApiVersion"
    apiOnly = true
    
    plugin {
        name = "$pluginName"
        entrance = ".$mainClass"
        version = "$pluginVersion"
        description = "$pluginDescription"
        authors += "$pluginAuthor"
        api = ">= $allayApiVersion"
    }
}

dependencies {
}
""".trimIndent())
    }
    
    private fun createSettingsGradle(rootPath: String) {
        val settingsGradle = File(rootPath, "settings.gradle.kts")
        settingsGradle.writeText("""
rootProject.name = "$pluginName"

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}
""".trimIndent())
    }
    
    private fun createGradleProperties(rootPath: String) {
        val gradleProperties = File(rootPath, "gradle.properties")
        gradleProperties.writeText("""
org.gradle.jvmargs=-Xmx2048m
""".trimIndent())
    }
    
    private fun createMainClass(srcDir: File, groupId: String) {
        val mainClassFile = File(srcDir, "$mainClass.java")
        mainClassFile.writeText("""
package $groupId;

import org.allaymc.api.plugin.Plugin;

public class $mainClass extends Plugin {
    @Override
    public void onLoad() {
        getLogger().info("$pluginName is loading...");
    }
    
    @Override
    public void onEnable() {
        getLogger().info("$pluginName has been enabled!");
    }
    
    @Override
    public void onDisable() {
        getLogger().info("$pluginName has been disabled!");
    }
}
""".trimIndent())
    }
    
    private fun createGradleWrapper(rootPath: String) {
        val gradleWrapperDir = File(rootPath, "gradle/wrapper")
        FileUtil.createDirectory(gradleWrapperDir)
        
        val gradleWrapperProperties = File(gradleWrapperDir, "gradle-wrapper.properties")
        gradleWrapperProperties.writeText("""
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-8.9-bin.zip
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
""".trimIndent())
        
        val gradlewBat = File(rootPath, "gradlew.bat")
        gradlewBat.writeText("""
@if "%DEBUG%" == "" @echo off
@rem ##########################################################################
@rem
@rem  Gradle startup script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%

@rem Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass JVM options to this script.
set DEFAULT_JVM_OPTS="-Xmx64m" "-Xms64m"

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if "%ERRORLEVEL%" == "0" goto execute

echo.
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto execute

echo.
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:execute
@rem Setup the command line

set CLASSPATH=%APP_HOME%\gradle\wrapper\gradle-wrapper.jar

@rem Execute Gradle
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %GRADLE_OPTS% "-Dorg.gradle.appname=%APP_BASE_NAME%" -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*

:end
@rem End local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" endlocal

:fail
rem Set variable GRADLE_EXIT_CONSOLE if you need the _script_ return code instead of
rem the _cmd.exe /c_ return code!
if  not "" == "%GRADLE_EXIT_CONSOLE%" exit 1
exit /b 1
""".trimIndent())
        gradlewBat.setExecutable(true)
        
        val gradlew = File(rootPath, "gradlew")
        gradlew.writeText("""
#!/bin/sh

##############################################################################
##
##  Gradle start up script for UN*X
##
##############################################################################

PRG="${'$'}0"
while [ -h "${'$'}PRG" ] ; do
    ls=`ls -ld "${'$'}PRG"`
    link=`expr "${'$'}ls" : '.*-> \(.*\)${'$'}'`
    if expr "${'$'}link" : '/.*' > /dev/null; then
        PRG="${'$'}link"
    else
        PRG=`dirname "${'$'}PRG"`"/${'$'}link"
    fi
done
SAVED="`pwd`"
cd "`dirname \"${'$'}PRG\"`/" >/dev/null
APP_HOME="`pwd -P`"
cd "${'$'}SAVED" >/dev/null

APP_NAME="Gradle"
APP_BASE_NAME=`basename "${'$'}0"`

DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'

MAX_FD="maximum"

warn () {
    echo "${'$'}*"
}

die () {
    echo
    echo "${'$'}*"
    echo
    exit 1
}

cygwin=false
msys=false
darwin=false
nonstop=false
case "`uname`" in
  CYGWIN* )
    cygwin=true
    ;;
  Darwin* )
    darwin=true
    ;;
  MSYS* | MINGW* )
    msys=true
    ;;
  NONSTOP* )
    nonstop=true
    ;;
esac

CLASSPATH=${'$'}APP_HOME/gradle/wrapper/gradle-wrapper.jar

if [ -n "${'$'}JAVA_HOME" ] ; then
    if [ -x "${'$'}JAVA_HOME/jre/sh/java" ] ; then
        JAVACMD="${'$'}JAVA_HOME/jre/sh/java"
    else
        JAVACMD="${'$'}JAVA_HOME/bin/java"
    fi
    if [ ! -x "${'$'}JAVACMD" ] ; then
        die "ERROR: JAVA_HOME is set to an invalid directory: ${'$'}JAVA_HOME

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation."
    fi
else
    JAVACMD="java"
    which java >/dev/null 2>&1 || die "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation."
fi

save () {
    for i do printf %s\\n "${'$'}i" | sed "s/'/'\\\\''/g;1s/^/'/;\${'$'}s/${'$'}/'/" ; done
    echo " "
}
APP_ARGS=`save "${'$'}@"`

eval set -- ${'$'}DEFAULT_JVM_OPTS ${'$'}JAVA_OPTS ${'$'}GRADLE_OPTS "\"-Dorg.gradle.appname=${'$'}APP_BASE_NAME\"" -classpath "\"${'$'}CLASSPATH\"" org.gradle.wrapper.GradleWrapperMain "${'$'}APP_ARGS"

exec "${'$'}JAVACMD" "${'$'}@"
""".trimIndent())
        gradlew.setExecutable(true)
    }
    
    fun setPluginName(name: String) { pluginName = name }
    fun setPluginVersion(version: String) { pluginVersion = version }
    fun setPluginDescription(description: String) { pluginDescription = description }
    fun setPluginAuthor(author: String) { pluginAuthor = author }
    fun setAllayApiVersion(version: String) { allayApiVersion = version }
    fun setMainClass(className: String) { mainClass = className }
    fun setGroupId(group: String) { groupId = group }
}

