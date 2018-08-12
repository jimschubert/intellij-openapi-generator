/*
 * Copyright (c) 2018 Jim Schubert
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package us.jimschubert.intellij.openapitools.ui

import com.intellij.ide.CommonActionsManager
import com.intellij.ide.DefaultTreeExpander
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.ActionToolbar
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.OnePixelSplitter
import com.intellij.ui.ScrollPaneFactory
import com.intellij.ui.TabbedPaneWrapper
import com.intellij.ui.tabs.impl.JBTabsImpl
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.tree.TreeUtil
import io.swagger.parser.SwaggerParser
import org.openapitools.codegen.CodegenConfig
import org.openapitools.codegen.CodegenType
import org.openapitools.codegen.DefaultGenerator
import org.openapitools.codegen.config.CodegenConfigurator
import us.jimschubert.intellij.openapitools.Message
import us.jimschubert.intellij.openapitools.Message.Companion
import us.jimschubert.intellij.openapitools.events.GenerationNotificationManager
import java.awt.BorderLayout
import java.io.File
import java.util.*
import javax.swing.*

class GenerateDialog(private val project: Project, val file: VirtualFile, private val notificationManager: GenerationNotificationManager) : DialogWrapper(project) {
    companion object {
        private var generatorTypeMap: MutableMap<CodegenType, MutableList<CodegenConfig>> = mutableMapOf()
//        private val app = ApplicationManager.getApplication()
    }

    private val logger = Logger.getInstance(this.javaClass)
    private lateinit var optionsPanel: JPanel
    private lateinit var settingsPanel: GeneratorGeneralSettingsPanel
    private lateinit var panel: JPanel
    private lateinit var tree: GeneratorsTreePanel
    private lateinit var expander: DefaultTreeExpander

    private lateinit var additionalPropertiesPanel: KeyValuePropertiesPanel
    private lateinit var instantiationTypesPanel: KeyValuePropertiesPanel
    private lateinit var importMappingsPanel: KeyValuePropertiesPanel
    private lateinit var typeMappingsPanel: KeyValuePropertiesPanel
    private lateinit var primitivesPanel: ValuePropertiesPanel

    private var currentConfigOptions: us.jimschubert.intellij.openapitools.ui.CodegenConfigOptions? = null
    private val langPanel = TabbedPaneWrapper(this.project) // TODO: Implement disposable on Dialog?

    private val emptyBorder = JBUI.Borders.empty()
    private val outputBrowse = TextFieldWithBrowseButton()

    init {
        outputBrowse.addBrowseFolderListener(
                us.jimschubert.intellij.openapitools.Message of "dialog.generate.output-browse.title",
                null,
                project,
                FileChooserDescriptorFactory.createSingleFolderDescriptor()
        )

        // TODO: Prior to building anything here, read and parse file. If that fails, bail immediately with error message.
        if (generatorTypeMap.isEmpty()) {
            val extensions: List<CodegenConfig> = ServiceLoader.load(CodegenConfig::class.java, CodegenConfig::class.java.classLoader).toList()
            for (extension in extensions) {
                if (!generatorTypeMap.containsKey(extension.tag)) {
                    generatorTypeMap[extension.tag] = mutableListOf(extension)
                } else {
                    generatorTypeMap[extension.tag]!!.add(extension)
                }
            }
        }
        title = Message of "dialog.generate.title"
        setOKButtonText(Message of "dialog.generate.ok")
        setCancelButtonText(Message of "dialog.generate.cancel")

        createOptionsPanel()
        createTree()

        createAdditionalPropertiesPanel()
        createInstantiationTypesPanel()
        createImportMappingsPanel()
        createTypeMappingsPanel()
        createPrimitivesPanel()

        setResizable(false)
        init()
    }

    private fun createPrimitivesPanel() {
        primitivesPanel = ValuePropertiesPanel(
                Message of "panel.primitive-types.value-column"
        )
        primitivesPanel.setEmptyMessage(Message of "panel.primitive-types.empty-message")
    }

    private fun createTypeMappingsPanel() {
        typeMappingsPanel = KeyValuePropertiesPanel(
                us.jimschubert.intellij.openapitools.Message of "panel.type-mappings.key-column",
                us.jimschubert.intellij.openapitools.Message of "panel.type-mappings.value-column"
        )
        typeMappingsPanel.setEmptyMessage(Message of "panel.type-mappings.empty-message")
    }

    private fun createImportMappingsPanel() {
        importMappingsPanel = KeyValuePropertiesPanel(
                Message of "panel.import-mappings.key-column",
                Message of "panel.import-mappings.value-column"
        )
        importMappingsPanel.setEmptyMessage(Message of "panel.import-mappings.empty-message")
    }

    private fun createAdditionalPropertiesPanel() {
        additionalPropertiesPanel = KeyValuePropertiesPanel(
                Message of "panel.additional-properties.key-column"
        )
        additionalPropertiesPanel.setEmptyMessage(Message of "panel.additional-properties.empty-message")
    }

    private fun createInstantiationTypesPanel() {
        instantiationTypesPanel = KeyValuePropertiesPanel(
                Message of "panel.instantiation-types.key-column",
                Message of "panel.instantiation-types.value-column"
        )
        instantiationTypesPanel.setEmptyMessage(Message of "panel.instantiation-types.empty-message")
    }

    private fun createOptionsPanel() {
        optionsPanel = JPanel(BorderLayout())
        optionsPanel.border = JBUI.Borders.empty(0, 2, 0, 2)
        optionsPanel.preferredSize = JBUI.size(800 - 185, 400)
    }

    // TODO: Form validation
    override fun doValidate(): ValidationInfo? {
        // valid OpenAPI file
        SwaggerParser().readWithInfo(file.path) ?: return ValidationInfo("Specification file is invalid.", null)

        if(outputBrowse.text.isEmpty()){
            notificationManager.warn("Output directory is empty.")
            return ValidationInfo("Output directory is empty.", outputBrowse)
        } else {
            val path = outputBrowse.text.replaceFirst("^~", System.getProperty("user.home"))
            if(outputBrowse.text != path) {
                outputBrowse.text = path
            }

            val output = File(FileUtil.toSystemDependentName(path))
            if(output.exists() && !output.isDirectory) {
                notificationManager.warn("Output directory is not a valid directory.")
                return ValidationInfo("Output directory is not a valid directory.", outputBrowse)
            }
            if(!output.exists()) {
                if(FileUtil.createDirectory(output)) {
                    notificationManager.warn("Could not create output directory.")
                    return ValidationInfo("Could not create output directory.", outputBrowse)
                }
            }

            if(!output.canWrite()) {
                notificationManager.warn("Output directory is not writable.")
                return ValidationInfo("Output directory is not writable.", outputBrowse)
            }
        }

        // TODO: Does this need to switch to the tab on error or flash it maybe?
        return settingsPanel.doValidate() ?: super.doValidate()
    }

    private fun syncOptionsPanelOnChange(newPanel: JPanel) {
        optionsPanel.removeAll()

        val wrapper = JPanel(BorderLayout())
        wrapper.add(newPanel, BorderLayout.NORTH)

        val scroller = ScrollPaneFactory.createScrollPane(wrapper, true)
        scroller.preferredSize = optionsPanel.preferredSize

        optionsPanel.add(scroller, BorderLayout.CENTER)

        optionsPanel.revalidate()
        panel.revalidate()
        optionsPanel.repaint()

        // TODO: Reset settings and other panels?
    }

    private fun createTree() {
        tree = GeneratorsTreePanel(generatorTypeMap)
        tree.addNodeChangeListener(GeneratorsTreeSelectionListener { p: JPanel, configOptions: CodegenConfigOptions? ->
            currentConfigOptions = configOptions
            val tabPane = langPanel.component.components.first() as? JBTabsImpl
            if (currentConfigOptions != null) {
                langPanel.setTitleAt(0, currentConfigOptions?.config?.name)
                tabPane?.tabs?.forEachIndexed { index, tabInfo -> if(index > 0) tabInfo.isHidden = false }
            } else {
                langPanel.setTitleAt(0, "OpenAPI Generator")
                tabPane?.tabs?.forEachIndexed { index, tabInfo -> if(index > 0) tabInfo.isHidden = true }
            }
            langPanel.selectedIndex = 0
            syncOptionsPanelOnChange(p)
        })
        expander = DefaultTreeExpander(tree.genTypeTree)
    }

    override fun doOKAction() {
        logger.debug("Inputs:")
        logger.debug(currentConfigOptions?.config?.javaClass?.simpleName)
        currentConfigOptions?.inputs?.forEach { logger.debug("${it.cliOption.opt}: ${it.userInput.invoke()}") }

        val configurator = CodegenConfigurator.fromFile(settingsPanel.configurationFile) ?: CodegenConfigurator()

        configurator.inputSpec = file.path
        configurator.generatorName = currentConfigOptions?.config?.javaClass?.typeName
        configurator.outputDir = outputBrowse.text

        configurator.instantiationTypes = instantiationTypesPanel.itemsAsMap()
        configurator.importMappings = importMappingsPanel.itemsAsMap()
        configurator.typeMappings = typeMappingsPanel.itemsAsMap()
        configurator.languageSpecificPrimitives = primitivesPanel.itemsAsSet()
        @Suppress("USELESS_CAST")
        configurator.additionalProperties = additionalPropertiesPanel.itemsAsMap() as Map<String, Any>?

        configurator.isVerbose = settingsPanel.isVerbose
        configurator.isSkipOverwrite = settingsPanel.skipOverwrite

        if(settingsPanel.templateDirectory != null)
            configurator.templateDir = settingsPanel.templateDirectory

        configurator.library = settingsPanel.library
        configurator.invokerPackage = settingsPanel.invokerPackage
        configurator.groupId = settingsPanel.groupId
        configurator.artifactId = settingsPanel.artifactId
        configurator.artifactVersion = settingsPanel.artifactVersion
        configurator.apiPackage = settingsPanel.apiPackage
        configurator.auth = settingsPanel.auth

        if (settingsPanel.includeSystemProperties) {
            System.getProperties().propertyNames().toList().forEach { prop ->
                when (prop) {
                    is String -> configurator.addSystemProperty(prop, System.getProperty(prop))
                }
            }
        }

        currentConfigOptions?.inputs?.forEach { userInput ->
            configurator.addAdditionalProperty(userInput.cliOption.opt, userInput.userInput())
        }

        // Required for internal ServiceLoader calls to operate as expected.
        Thread.currentThread().contextClassLoader = javaClass.classLoader

        try {
            val files: MutableList<File> = DefaultGenerator()
                    .opts(configurator.toClientOptInput())
                    .generate()

            logger.debug("Generated contents can be found in ${outputBrowse.text}.")

            if(files.count() > 0) logger.debug("Generated files:")
            files.forEach { f -> logger.debug(f.canonicalPath) }

            notificationManager.success(currentConfigOptions?.config?.name ?: configurator.generatorName, configurator.outputDir)
        } catch (t: Throwable) {
            // notificationManager logs the error here, and we don't want to duplicate
            notificationManager.failure(t)
        }

        super.doOKAction()
    }

    override fun createCenterPanel(): JComponent? {
        panel = JPanel(BorderLayout())

        // splitter divides the panel into two parts
        val splitter = OnePixelSplitter(false, .2f)
        splitter.border = emptyBorder
        splitter.setHonorComponentsMinimumSize(true)

        val scrollPaneLeft = ScrollPaneFactory.createScrollPane(tree, true)
        scrollPaneLeft.preferredSize = JBUI.size(185, 400)
        scrollPaneLeft.border = BorderFactory.createEmptyBorder(5, 5, 5, 5)

        val leftPanel = JPanel(BorderLayout())
        leftPanel.add(createActionToolbar(tree).component, BorderLayout.NORTH)
        leftPanel.add(scrollPaneLeft, BorderLayout.CENTER)

        panel.add(splitter, BorderLayout.CENTER)
        splitter.firstComponent = leftPanel

        // TABS
        langPanel.addTab(currentConfigOptions?.config?.name ?: "Language", optionsPanel)

        langPanel.addTab("Additional Properties", additionalPropertiesPanel)
        langPanel.addTab("Instantiation Types", instantiationTypesPanel)
        langPanel.addTab("Imports", importMappingsPanel)
        langPanel.addTab("Type Mappings", typeMappingsPanel)
        langPanel.addTab("Primitives", primitivesPanel)

        settingsPanel = GeneratorGeneralSettingsPanel()

        langPanel.addTab("Generator Settings", settingsPanel.component)

        splitter.secondComponent = langPanel.component

        val outputPanel = JPanel(BorderLayout())
        outputPanel.add(JSeparator(), BorderLayout.NORTH)
        outputPanel.add(JLabel("Output Directory: "), BorderLayout.WEST)
        outputPanel.add(outputBrowse, BorderLayout.CENTER)

        // https://github.com/JetBrains/intellij-plugins/blob/a37e133b767d5b407b172758bce8475515becc16/Dart/src/com/jetbrains/lang/dart/ide/runner/server/ui/DartCommandLineConfigurationEditorForm.java#L38
        panel.add(outputPanel, BorderLayout.SOUTH)

        if (currentConfigOptions == null || tree.genTypeTree.selectionPath == null) {
            TreeUtil.selectFirstNode(tree.genTypeTree)
        }

        return panel
    }

    private fun createActionToolbar(target: JComponent): ActionToolbar {
        val actionManager = CommonActionsManager.getInstance()
        val actions = DefaultActionGroup()

        actions.add(actionManager.createCollapseAllAction(expander, tree.genTypeTree))
        actions.add(actionManager.createExpandAllAction(expander, tree.genTypeTree))

        val toolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.UNKNOWN, actions, true)
        toolbar.setTargetComponent(target)
        return toolbar
    }
}
