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

import com.intellij.util.ui.ValidatingTableEditor
import com.jimschubert.intellij.swaggercodegen.Message
import java.awt.BorderLayout
import javax.swing.JPanel

internal class ValuePropertiesPanel(val header: String = "Value") : JPanel(BorderLayout()) {
    private val _items: MutableList<SimpleValueItem>
    private val _editor: ValidatingTableEditor<SimpleValueItem>

    val items: List<SimpleValueItem>
        get() = _items.toList()

    init {
        _items = mutableListOf<SimpleValueItem>()
        _editor = object : ValidatingTableEditor<SimpleValueItem>() {
            override fun createItem(): SimpleValueItem? = SimpleValueItem("")

            override fun cloneOf(item: SimpleValueItem?): SimpleValueItem? = SimpleValueItem(item?.value ?: "")

            override fun validate(item: SimpleValueItem?): String? {
                when {
                    item == null -> {
                        return Message of "panel.value-properties.invalid-empty"
                    }
                    item.value == "" -> {
                        return Message of "panel.value-properties.invalid-value"
                    }
                }

                return null
            }
        }

        _editor.setModel(
                arrayOf(
                        ValidatingValueColumnInfo<SimpleValueItem>(header, PlainTextCellEditor())
                ),
                _items
        )

        add(_editor.contentPane, BorderLayout.CENTER)
    }

    fun setEmptyMessage(value: String) {
        _editor.emptyText.text = value
    }
}