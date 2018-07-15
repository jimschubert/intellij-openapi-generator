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

package us.jimschubert.intellij.openapitools.events

import com.intellij.notification.*
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.ProjectManager

class GenerationNotificationManager {
    private val logger = Logger.getInstance(this.javaClass)

    fun warn(message: String){
        logger.warn(message)
    }

    fun error(message: String){
        logger.error(message)
    }

    fun success(lang: String, outputDir: String) {
        val project = ProjectManager.getInstance().openProjects.firstOrNull()
        val title = us.jimschubert.intellij.openapitools.Message.of("notification.generation.success.title", GROUP)
        val content = us.jimschubert.intellij.openapitools.Message.of("notification.generation.success.content", lang, outputDir)

        // Must invokeLater, otherwise Notification only displays after dialogs or other disposables are disposed
        // see https://intellij-support.jetbrains.com/hc/en-us/community/posts/206766265-Notification-Balloon-not-shown-weird-scenario
        ApplicationManager.getApplication().executeOnPooledThread {
            Notifications.Bus.notify(notificationGroup.createNotification(title, content, NotificationType.INFORMATION, null), project)
        }
    }

    fun failure(t: Throwable? = null){
        val project = ProjectManager.getInstance().openProjects.firstOrNull()
        val title = us.jimschubert.intellij.openapitools.Message.of("notification.generation.failure.title", GROUP)

        error(t?.message ?: "Unknown error")

        // Must invokeLater, otherwise Notification only displays after dialogs or other disposables are disposed
        // see https://intellij-support.jetbrains.com/hc/en-us/community/posts/206766265-Notification-Balloon-not-shown-weird-scenario
        ApplicationManager.getApplication().executeOnPooledThread {
            Notifications.Bus.notify(notificationGroup.createNotification(title, NotificationType.WARNING), project)
        }

    }

    companion object {
        private val GROUP = "OpenAPI"
        private var notificationGroup: NotificationGroup
        init {
            notificationGroup = NotificationGroup(GROUP, NotificationDisplayType.BALLOON, true)
        }
    }
}