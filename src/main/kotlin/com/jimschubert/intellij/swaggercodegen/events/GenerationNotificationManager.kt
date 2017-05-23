package com.jimschubert.intellij.swaggercodegen.events

import com.intellij.notification.*
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.ProjectManager
import com.jimschubert.intellij.swaggercodegen.Message

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
        val title = Message.of("notification.generation.success.title", GROUP)
        val content = Message.of("notification.generation.success.content", lang, outputDir)

        // Must invokeLater, otherwise Notification only displays after dialogs or other disposables are disposed
        // see https://intellij-support.jetbrains.com/hc/en-us/community/posts/206766265-Notification-Balloon-not-shown-weird-scenario
        ApplicationManager.getApplication().executeOnPooledThread {
            Notifications.Bus.notify(notificationGroup.createNotification(title, content, NotificationType.INFORMATION, null), project)
        }
    }

    fun failure(t: Throwable? = null){
        val project = ProjectManager.getInstance().openProjects.firstOrNull()
        val title = Message.of("notification.generation.failure.title", GROUP)

        error(t?.message ?: "Unknown error")

        // Must invokeLater, otherwise Notification only displays after dialogs or other disposables are disposed
        // see https://intellij-support.jetbrains.com/hc/en-us/community/posts/206766265-Notification-Balloon-not-shown-weird-scenario
        ApplicationManager.getApplication().executeOnPooledThread {
            Notifications.Bus.notify(notificationGroup.createNotification(title, NotificationType.WARNING), project)
        }

    }

    companion object {
        private val GROUP = "Swagger Codegen"
        private var notificationGroup: NotificationGroup
        init {
            notificationGroup = NotificationGroup(GROUP, NotificationDisplayType.BALLOON, true)
        }
    }
}