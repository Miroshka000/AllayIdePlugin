package miroshka.allayideplugin.project

import com.intellij.ide.util.projectWizard.ModuleWizardStep
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent
import javax.swing.JPanel

class AllayModuleWizardStep(private val builder: AllayModuleBuilder) : ModuleWizardStep() {
    
    private val groupIdField = JBTextField("com.example")
    private val pluginNameField = JBTextField("MyAllayPlugin")
    private val pluginVersionField = JBTextField("1.0.0")
    private val pluginDescriptionField = JBTextField("My Allay Plugin")
    private val pluginAuthorField = JBTextField(System.getProperty("user.name"))
    private val allayApiVersionField = JBTextField("0.15.0")
    private val mainClassField = JBTextField("MyPlugin")
    
    private val panel: JPanel = FormBuilder.createFormBuilder()
        .addLabeledComponent(JBLabel("Group ID (Package):"), groupIdField, 1, false)
        .addLabeledComponent(JBLabel("Plugin Name:"), pluginNameField, 1, false)
        .addLabeledComponent(JBLabel("Plugin Version:"), pluginVersionField, 1, false)
        .addLabeledComponent(JBLabel("Plugin Description:"), pluginDescriptionField, 1, false)
        .addLabeledComponent(JBLabel("Author:"), pluginAuthorField, 1, false)
        .addLabeledComponent(JBLabel("Allay API Version:"), allayApiVersionField, 1, false)
        .addLabeledComponent(JBLabel("Main Class Name:"), mainClassField, 1, false)
        .addComponentFillVertically(JPanel(), 0)
        .panel
    
    override fun getComponent(): JComponent = panel
    
    override fun updateDataModel() {
        builder.setGroupId(groupIdField.text)
        builder.setPluginName(pluginNameField.text)
        builder.setPluginVersion(pluginVersionField.text)
        builder.setPluginDescription(pluginDescriptionField.text)
        builder.setPluginAuthor(pluginAuthorField.text)
        builder.setAllayApiVersion(allayApiVersionField.text)
        builder.setMainClass(mainClassField.text)
    }
}

