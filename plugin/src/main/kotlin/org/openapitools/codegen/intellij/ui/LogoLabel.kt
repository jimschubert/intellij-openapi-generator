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