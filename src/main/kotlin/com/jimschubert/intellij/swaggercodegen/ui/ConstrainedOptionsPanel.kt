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

import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.JPanel
import javax.swing.JRadioButton

internal class ConstrainedOptionsPanel(val options: List<CliConstrainedOption>, val default: String? = null) : JPanel(GridBagLayout()) {
    private val defaultIdentifier = "<default>"
    private val group = ActionButtonGroup()

    init {
        // set width
        var x = 0
        var y = 0
        options.forEach { opt ->
            val gridConstraint = GridBagConstraints()
            gridConstraint.weightx = 0.5
            gridConstraint.gridx = x
            gridConstraint.gridy = y
            gridConstraint.fill = GridBagConstraints.HORIZONTAL

            val button = JRadioButton(opt.option)
            button.toolTipText = opt.description
            button.actionCommand = opt.option
            button.isSelected = opt.option == default ?: defaultIdentifier

            group.add(button)

            add(button, gridConstraint)

            x++
            if (x == 2) {
                // Arbitrary column overflow, reset and move to next row
                x = 0
                y++
            }
        }
        // avoid group.selection to be null when there is no default option
        if (group.selection == null) group.elements.nextElement().isSelected = true        
    }

    val value: String
        get() = group.selection.actionCommand
}
