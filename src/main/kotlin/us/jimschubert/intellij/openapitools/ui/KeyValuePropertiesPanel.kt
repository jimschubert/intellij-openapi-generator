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

import com.intellij.util.ui.ValidatingTableEditor
import us.jimschubert.intellij.openapitools.Message
import java.awt.BorderLayout
import javax.swing.JPanel

internal class KeyValuePropertiesPanel(key: String = "Key", val value: String = "Value") : JPanel(BorderLayout()) {
    private val _items: MutableList<SimpleKeyValueItem> = mutableListOf()
    private val _editor: ValidatingTableEditor<SimpleKeyValueItem>

    val items: List<SimpleKeyValueItem>
        get() = _items.toList()

    init {
        _editor = object : ValidatingTableEditor<SimpleKeyValueItem>() {
            override fun createItem(): SimpleKeyValueItem? = SimpleKeyValueItem("", "")

            override fun cloneOf(item: SimpleKeyValueItem?): SimpleKeyValueItem? =
                SimpleKeyValueItem(item?.key ?: "", item?.value ?: "")

            override fun validate(item: SimpleKeyValueItem?): String? {
                when {
                    item == null -> {
                        return Message of "panel.key-value-properties.invalid-empty"
                    }
                    item.key == "" -> {
                        return Message of "panel.key-value-properties.invalid-key"
                    }
                    item.value == "" -> {
                        return Message of "panel.key-value-properties.invalid-value"
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
