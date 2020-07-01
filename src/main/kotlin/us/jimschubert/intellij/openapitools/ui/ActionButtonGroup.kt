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

import com.intellij.openapi.application.ApplicationManager
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.AbstractButton
import javax.swing.ButtonGroup
import javax.swing.event.EventListenerList

// TODO: Is there some Kotlin magic to use here?
internal class ActionButtonGroup : ButtonGroup() {
    private val app = ApplicationManager.getApplication()
    private val boundListeners = EventListenerList()
    private val listener = Listener(this) {
        e: ActionEvent? ->
        onChange(e)
    }

    override fun add(b: AbstractButton?) {
        b?.addActionListener(listener)
        super.add(b)
    }

    private fun addActionListener(listener: ActionListener) {
        boundListeners.add(listener.javaClass, listener)
    }

    fun addActionListener(anonymousListener: (ActionEvent?) -> Unit) {
        addActionListener(Listener(this, anonymousListener))
    }

    @Suppress("unused")
    fun removeActionListener(listener: ActionListener) {
        boundListeners.remove(listener.javaClass, listener)
    }

    private fun onChange(e: ActionEvent?) {
        val command = e?.actionCommand ?: ""
        val evt = ActionEvent(this, ActionEvent.ACTION_PERFORMED, command)
        boundListeners.listenerList.forEach {
            app.invokeLater {
                (it as? ActionListener)?.actionPerformed(evt)
            }
        }
    }

    private class Listener(@Suppress("unused") val owner: ActionButtonGroup, val callback: (ActionEvent?) -> Unit) : ActionListener {
        override fun actionPerformed(e: ActionEvent?) {
            callback.invoke(e)
        }

    }
}