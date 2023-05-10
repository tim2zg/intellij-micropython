package com.jetbrains.micropython.ui

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
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
        toolWindow.contentManager.addContent(contentFactory.createContent(CalendarToolWindowContent(toolWindow).contentPanel, "Calendar", false))

        project.firstMicroPythonFacet?.let {
            MicroPythonReplManager.getInstance(it.module).startREPL()
        }
    }

    override fun init(toolWindow: ToolWindow) {
        toolWindow.stripeTitle = "MicroPython"
    }


    private class CalendarToolWindowContent(toolWindow: ToolWindow) {
        val contentPanel = JPanel()
        private val currentDate = JLabel()
        private val timeZone = JLabel()
        private val currentTime = JLabel()

        private val list = JList<String>(arrayOf("a", "b", "c"))

        init {
            contentPanel.layout = BorderLayout(0, 20)
            contentPanel.border = BorderFactory.createEmptyBorder(40, 0, 0, 0)
            contentPanel.add(createCalendarPanel(), BorderLayout.PAGE_START)
            contentPanel.add(createControlsPanel(toolWindow), BorderLayout.CENTER)
            updateCurrentDateTime()
        }

        private fun createCalendarPanel(): JPanel {
            val calendarPanel = JPanel()
            calendarPanel.add(currentDate)
            calendarPanel.add(timeZone)
            calendarPanel.add(currentTime)
            return calendarPanel
        }


        private fun createControlsPanel(toolWindow: ToolWindow): JPanel {
            val controlsPanel = JPanel()
            val refreshDateAndTimeButton = JButton("Refresh")
            refreshDateAndTimeButton.addActionListener { e: ActionEvent? -> updateCurrentDateTime() }
            controlsPanel.add(refreshDateAndTimeButton)
            val hideToolWindowButton = JButton("Hide")
            hideToolWindowButton.addActionListener { e: ActionEvent? -> toolWindow.hide(null) }
            controlsPanel.add(hideToolWindowButton)
            controlsPanel.add(list)
            return controlsPanel
        }

        private fun updateCurrentDateTime() {
            currentDate.text = "haha"
            timeZone.text = "Hehe"
            currentTime.text = "hihi"
        }

    }


}
