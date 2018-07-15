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

import com.intellij.ui.SimpleColoredComponent
import com.intellij.ui.SimpleTextAttributes
import org.openapitools.codegen.CodegenConfig
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode

internal open class GeneratorsTreeNode(val display: String?, val value: CodegenConfig? = null, val parentNode: GeneratorsTreeNode? = null) : DefaultMutableTreeNode(value) {
    fun render(component: SimpleColoredComponent, tree: JTree, selected: Boolean) {
        val unobtrusive = SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, tree.foreground.darker().darker())
        val normal = SimpleTextAttributes(SimpleTextAttributes.STYLE_OPAQUE, tree.foreground)
        val brighter = SimpleTextAttributes(SimpleTextAttributes.STYLE_OPAQUE, tree.foreground.brighter())
        val attrs = when {
            selected -> if (value != null) unobtrusive else brighter
            else -> normal
        }
        component.append(display ?: "", attrs, true)
        if (parentNode != null) {
            // Enforces no icons on generator items
            component.icon = null
        }
    }
}