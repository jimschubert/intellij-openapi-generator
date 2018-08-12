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

import com.intellij.util.ui.ColumnInfo
import com.intellij.util.ui.ValidatingTableEditor
import javax.swing.JTextField
import javax.swing.table.TableCellEditor

internal class ValidatingValueColumnInfo<T : SimpleValueItem>(
        columnName: String,
        private val editor: TableCellEditor? = null
) : ColumnInfo<T, Any>(columnName), ValidatingTableEditor.RowHeightProvider {
    override fun valueOf(item: T): String? {
        return item.value
    }

    override fun setValue(item: T, value: Any?) {
        item.value = value.toString()
    }

    override fun isCellEditable(item: T): Boolean {
        return editor != null
    }

    override fun getEditor(item: T): TableCellEditor? {
        return editor
    }

    override fun getRowHeight(): Int {
        return JTextField().preferredSize.height + 1
    }
}

