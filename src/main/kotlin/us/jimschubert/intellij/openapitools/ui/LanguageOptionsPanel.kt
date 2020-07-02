/*
 * Copyright (c) 2020 Jim Schubert
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

import com.intellij.icons.AllIcons
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.FormBuilder
import org.openapitools.codegen.CodegenConfig
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTextField

internal class LanguageOptionsPanel(val language: CodegenConfig) {
    companion object {
        private val generatorInfoPanel = us.jimschubert.intellij.openapitools.ui.GeneratorInfoPanel().component
        val empty: JPanel
            get() = generatorInfoPanel
    }

    private var _component: JPanel? = null
    val component: JPanel
        get() = _component ?: empty

    private var optionTrackers: MutableList<UserOptionInput> = mutableListOf()
    val userOptions: MutableList<UserOptionInput>
        get() = optionTrackers

    init {
        // TODO: Wrap JComponent instances to required fields

        var form = FormBuilder.createFormBuilder()
            .setAlignLabelOnRight(false)
            .setVerticalGap(4)

        language.cliOptions().forEachIndexed { _, cliOption ->
            val enums: Map<String, String> = cliOption.enum ?: mapOf()
            val optionComponent: JComponent = when {
                // TODO: Support any other cliOption types?
                cliOption.type == "string" && enums.isEmpty() -> {
                    val text = JTextField(cliOption.default)

                    optionTrackers.add(UserOptionInput(cliOption) { text.text })

                    text
                }
                cliOption.type == "string" && enums.isNotEmpty() -> {
                    val options =
                        enums.map { us.jimschubert.intellij.openapitools.ui.CliConstrainedOption.fromKvp(it) }.toList()
                    val panel =
                        us.jimschubert.intellij.openapitools.ui.ConstrainedOptionsPanel(options, cliOption.default)

                    optionTrackers.add(UserOptionInput(cliOption) { panel.value })

                    panel
                }
                cliOption.type == "boolean" -> {
                    val panel = JTrueFalseRadioPanel(cliOption.default!!.toBoolean())

                    optionTrackers.add(UserOptionInput(cliOption) { panel.value.toString() })

                    panel
                }
                else -> {
                    println("${cliOption.opt} is type ${cliOption.type}")
                    val text = JTextField(cliOption.default)

                    optionTrackers.add(UserOptionInput(cliOption) { text.text })

                    text
                }
            }

            val label = JBLabel(cliOption.opt)
            if (cliOption.description != null) {
                label.icon = AllIcons.Toolwindows.Documentation
                label.toolTipText = cliOption.description
            }

            form = form.addLabeledComponent(label, optionComponent)

            _component = form.panel
        }

    }

}