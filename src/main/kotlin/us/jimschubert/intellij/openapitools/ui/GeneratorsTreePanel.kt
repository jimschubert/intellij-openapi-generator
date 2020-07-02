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

import com.intellij.ui.ColoredTreeCellRenderer
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.ui.UIUtil
import com.intellij.util.ui.tree.TreeUtil
import org.openapitools.codegen.CodegenConfig
import org.openapitools.codegen.CodegenType
import java.awt.BorderLayout
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.JTree
import javax.swing.plaf.basic.BasicTreeUI
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeSelectionModel

internal class GeneratorsTreePanel(private val generators: Map<CodegenType, List<CodegenConfig>>) : JComponent() {

    private val myTreeModel: DefaultTreeModel = buildModel()
    private val _genTypeTree: Tree = Tree(DefaultTreeModel(DefaultMutableTreeNode()))
    val genTypeTree: Tree
        get() = _genTypeTree

    init {
        genTypeTree.model = myTreeModel
        genTypeTree.cellRenderer = Renderer()

        TreeUtil.expandAll(genTypeTree)
        genTypeTree.isRootVisible = false
        genTypeTree.showsRootHandles = true

        // genTypeTree.putClientProperty(WideSelectionTreeUI.TREE_TABLE_TREE_KEY, true)
        genTypeTree.selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION

        genTypeTree.isOpaque = false
        genTypeTree.background = UIUtil.SIDE_PANEL_BACKGROUND
        genTypeTree.inputMap.clear()

        TreeUtil.installActions(genTypeTree)

        val basicUi = genTypeTree.ui as BasicTreeUI
        basicUi.leftChildIndent = 6 // Distance between left margin and where vertical dashes will be drawn.
        basicUi.rightChildIndent =
            4 // Distance to add to leftChildIndent to determine where cell contents will be drawn.

        layout = BorderLayout()
        add(genTypeTree, BorderLayout.CENTER)
    }

    private fun buildModel(): DefaultTreeModel {
        val node = GeneratorsTreeRootNode(generators)
        return DefaultTreeModel(node)
    }

    fun addNodeChangeListener(listener: GeneratorsTreeSelectionListener) {
        genTypeTree.addTreeSelectionListener(listener)
    }

    private class Renderer : ColoredTreeCellRenderer() {
        init {
            border = BorderFactory.createEmptyBorder()
            isOpaque = false
        }

        override fun customizeCellRenderer(
            tree: JTree,
            value: Any?,
            selected: Boolean,
            expanded: Boolean,
            leaf: Boolean,
            row: Int,
            hasFocus: Boolean
        ) {
            val node = value as GeneratorsTreeNode? ?: return
            node.render(this, tree, selected)
        }
    }
}