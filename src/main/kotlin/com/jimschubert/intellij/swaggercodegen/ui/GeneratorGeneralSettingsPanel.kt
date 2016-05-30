/*
 * Copyright 2016 Jim Schubert
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

package com.jimschubert.intellij.swaggercodegen.ui

import com.intellij.json.JsonFileType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.UIBundle
import com.intellij.util.ui.FormBuilder
import com.jimschubert.intellij.swaggercodegen.Message
import java.awt.AWTEvent
import java.awt.event.TextEvent
import java.io.File
import javax.swing.JCheckBox
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

internal class GeneratorGeneralSettingsPanel : Validatable {

    private val _component: JPanel
    val component: JPanel
        get() = _component

    private var _isVerbose = false
    val isVerbose: Boolean
        get() = _isVerbose

    private var _skipOverwrite = false
    val skipOverwrite: Boolean
        get() = _skipOverwrite

    private var _includeSystemProperties = false
    val includeSystemProperties: Boolean
        get() = _includeSystemProperties

    private var _templateDirectory: String? = null
    val templateDirectory: String?
        get() = _templateDirectory

    private var _configurationFile: String? = null
    val configurationFile: String?
        get() = _configurationFile

    private var _library: String? = null
    val library: String?
        get() = _library

    private var _invokerPackage: String? = null
    val invokerPackage: String?
        get() = _invokerPackage

    private var _groupId: String? = null
    val groupId: String?
        get() = _groupId

    private var _artifactId: String? = null
    val artifactId: String?
        get() = _artifactId

    private var _artifactVersion: String? = null
    val artifactVersion: String?
        get() = _artifactVersion

    private var _apiPackage: String? = null
    val apiPackage: String?
        get() = _apiPackage

    private var _auth: String? = null
    val auth: String?
        get() = _auth

    init {

        val settingsPanelBuilder = FormBuilder.createFormBuilder()
                .addLabeledComponent("Verbose", checkbox { selected -> _isVerbose = selected })
                .addLabeledComponent("Skip Overwrite", checkbox { selected -> _skipOverwrite = selected })
                .addLabeledComponent("Include System Properties", checkbox { selected -> _includeSystemProperties = selected })
                .addLabeledComponent("Template Directory",
                        browse("panel.general-settings.browse.template-dir",
                                FileChooserDescriptorFactory.createSingleFolderDescriptor()) { e ->
                            _templateDirectory = e.current
                        })
                .addLabeledComponent("Configuration File",
                        browse("panel.general-settings.browse.config-file",
                                FileChooserDescriptorFactory.createSingleFileDescriptor(JsonFileType.INSTANCE)) { e ->
                            _configurationFile = e.current
                        })
                .addLabeledComponent("Library", text { e -> _library = e.current })
                .addLabeledComponent("Invoker Package", text { e -> _invokerPackage = e.current })
                .addLabeledComponent("Group ID", text { e -> _groupId = e.current })
                .addLabeledComponent("Artifact ID", text { e -> _artifactId = e.current })
                .addLabeledComponent("Artifact Version", text { e -> _artifactVersion = e.current })
                .addLabeledComponent("API Package", text { e -> _apiPackage = e.current })
                .addLabeledComponent("Auth", text { e -> _auth = e.current })

        _component = settingsPanelBuilder.panel
    }

    fun text(listener: (JTextFieldChangeEvent) -> Unit): JTextField {
        val element = JTextField()
        element.onChange { evt -> listener(evt) }

        return element
    }

    fun checkbox(listener: (Boolean) -> Unit): JCheckBox {
        val element = JCheckBox()
        element.addChangeListener { e -> listener(element.isSelected) }
        return element
    }

    fun browse(bundleKey: String?, descriptor: FileChooserDescriptor, listener: (JTextFieldChangeEvent) -> Unit): TextFieldWithBrowseButton {
        val element = TextFieldWithBrowseButton()
        element.addBrowseFolderListener(
                Message of (bundleKey ?: UIBundle.message("file.chooser.default.title")),
                null,
                null,
                descriptor
        )

        element.textField.onChange { evt -> listener(evt) }

        return element
    }

    override fun doValidate(): ValidationInfo? {
        if(_templateDirectory != null && _templateDirectory != "") {
            val tmplDir = File(_templateDirectory)
            if(false == (tmplDir.exists() && tmplDir.isDirectory && tmplDir.canRead())) {
                return ValidationInfo("Invalid template directory")
            }
        }
        if(_configurationFile != null && _configurationFile != "") {
            val configFile = File(_configurationFile)
            if(false == (configFile.exists() && configFile.isFile && configFile.canRead())) {
                return ValidationInfo("Invalid configuration file")
            }
        }
        // TODO: Validate other settings
        return super.doValidate()
    }
}

fun JTextField.onChange(listener: (JTextFieldChangeEvent) -> Unit) {
    document.addDocumentListener(TextChangeListener(this, listener))
}

class TextChangeListener(val source: JTextField, val listener: (JTextFieldChangeEvent) -> Unit) : DocumentListener {
    private var prev: String? = null
    private val app = ApplicationManager.getApplication()

    override fun changedUpdate(e: DocumentEvent?) = handle()
    override fun insertUpdate(e: DocumentEvent?) = handle()
    override fun removeUpdate(e: DocumentEvent?) = handle()
    private fun handle() {
        app.invokeLater {
            listener(JTextFieldChangeEvent(source, prev, source.text))
        }
        prev = source.text
    }
}

class JTextFieldChangeEvent(val source: JTextField, val previous: String?, val current: String?) :
        AWTEvent(source, TextEvent.TEXT_VALUE_CHANGED) {

}
