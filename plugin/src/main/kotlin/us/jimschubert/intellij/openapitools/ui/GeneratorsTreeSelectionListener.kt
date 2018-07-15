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

import org.openapitools.codegen.CodegenConfig
import javax.swing.JPanel
import javax.swing.event.TreeSelectionEvent
import javax.swing.event.TreeSelectionListener
import javax.swing.tree.TreePath

internal class GeneratorsTreeSelectionListener(val sync: (JPanel, us.jimschubert.intellij.openapitools.ui.CodegenConfigOptions?) -> Unit) : TreeSelectionListener {

    override fun valueChanged(e: TreeSelectionEvent?) {
        sync(LanguageOptionsPanel.empty, null)

        if (e == null) return
        val paths = e.paths ?: return
        val valuePath: List<TreePath> = paths.filter {
            e.isAddedPath(it)
        }
        if (valuePath.isEmpty()) return
        val selected: TreePath = valuePath.first()

        val node = selected.lastPathComponent as GeneratorsTreeNode
        val language: CodegenConfig = node.value ?: return

        val panel = LanguageOptionsPanel(language)
        sync(panel.component, us.jimschubert.intellij.openapitools.ui.CodegenConfigOptions(language, panel.userOptions))
    }
}
