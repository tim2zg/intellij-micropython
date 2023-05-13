package com.jetbrains.micropython.ui

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import com.jetbrains.micropython.devices.hehe
import com.jetbrains.micropython.repl.MicroPythonReplManager
import com.jetbrains.micropython.repl.ToolWindowReplTab
import com.jetbrains.micropython.settings.firstMicroPythonFacet
import java.awt.BorderLayout
import java.awt.event.ActionEvent
import javax.swing.*


class MicroPythonToolWindowFactory : ToolWindowFactory, DumbAware {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val contentFactory = ContentFactory.getInstance()
        val terminalContent = contentFactory.createContent(null, "REPL-TOM", true)

        project.firstMicroPythonFacet?.let {
            terminalContent.component = ToolWindowReplTab(it.module, terminalContent).createUI()
        }

        toolWindow.contentManager.addContent(terminalContent)
        toolWindow.contentManager.setSelectedContent(terminalContent)
        toolWindow.contentManager.addContent(contentFactory.createContent(hehe(module = ,toolWindow).contentPanel, "Calendar", false))


        project.firstMicroPythonFacet?.let {
            MicroPythonReplManager.getInstance(it.module).startREPL()
        }
    }

    override fun init(toolWindow: ToolWindow) {
        toolWindow.stripeTitle = "MicroPython"
    }

}
