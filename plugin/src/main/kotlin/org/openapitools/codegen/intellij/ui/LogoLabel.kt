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

package org.openapitools.codegen.intellij.ui

import com.intellij.openapi.util.IconLoader
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.UIUtil
import javax.swing.Icon

class LogoLabel : JBLabel() {

    /**
     * Returns the graphic image (glyph, icon) that the label displays.
     *
     * @return an Icon
     * @see .setIcon
     */
    override fun getIcon(): Icon {
        return when {
            UIUtil.isUnderDarcula() -> dark
            else -> light
        }
    }

    companion object {
        private val light = IconLoader.getIcon("/images/openapi-generator.png")
        private val dark = IconLoader.getIcon("/images/openapi-generator_dark.png")
    }
}