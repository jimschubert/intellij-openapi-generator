/*
 * Copyright 2016 Jim Schubert
 * Copyright 2018 OpenAPI-Generator Contributors (https://openapi-generator.tech)
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

package org.openapitools.codegen.intellij.ui

import com.intellij.util.ui.ValidatingTableEditor
import org.openapitools.codegen.intellij.Message
import java.awt.BorderLayout
import javax.swing.JPanel

internal class KeyValuePropertiesPanel(val key: String = "Key", val value: String = "Value") : JPanel(BorderLayout()) {
    private val _items: MutableList<SimpleKeyValueItem>
    private val _editor: ValidatingTableEditor<SimpleKeyValueItem>

    val items: List<SimpleKeyValueItem>
        get() = _items.toList()

    init {
        _items = mutableListOf<SimpleKeyValueItem>()
        _editor = object : ValidatingTableEditor<SimpleKeyValueItem>() {
            override fun createItem(): SimpleKeyValueItem? = SimpleKeyValueItem("", "")

            override fun cloneOf(item: SimpleKeyValueItem?): SimpleKeyValueItem? = SimpleKeyValueItem(item?.key ?: "", item?.value ?: "")

            override fun validate(item: SimpleKeyValueItem?): String? {
                when {
                    item == null -> {
                        return org.openapitools.codegen.intellij.Message of "panel.key-value-properties.invalid-empty"
                    }
                    item.key == "" -> {
                        return org.openapitools.codegen.intellij.Message of "panel.key-value-properties.invalid-key"
                    }
                    item.value == "" -> {
                        return org.openapitools.codegen.intellij.Message of "panel.key-value-properties.invalid-value"
                    }
                }

                return null
            }
        }

        _editor.setModel(
                arrayOf(
                        ValidatingKeyColumnInfo<SimpleKeyValueItem>(key, PlainTextCellEditor()),
                        ValidatingValueColumnInfo<SimpleKeyValueItem>(value, PlainTextCellEditor())
                ),
                _items
        )

        add(_editor.contentPane, BorderLayout.CENTER)
    }

    fun setEmptyMessage(value: String) {
        _editor.emptyText.text = value
    }

}


