package com.jetbrains.micropython.devices
import com.intellij.openapi.module.Module
import com.intellij.openapi.wm.ToolWindow
import com.jetbrains.micropython.repl.MicroPythonReplManager
import com.jetbrains.micropython.settings.MicroPythonDevicesConfiguration
import com.jetbrains.micropython.settings.MicroPythonFacet
import java.awt.BorderLayout
import com.jetbrains.micropython.settings.microPythonFacet
import java.awt.event.ActionEvent
import javax.swing.*


class hehe(val module: Module, toolWindow: ToolWindow) {
    private val deviceConfiguration = MicroPythonDevicesConfiguration.getInstance(module.project)
    val contentPanel = JPanel()
    private val currentDate = JLabel()
    private val timeZone = JLabel()
    private val currentTime = JLabel()

    private val list = JList(arrayOf("a", "b", "c"))

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

    fun getFilesOnDevice() {
        // get the device path
        val devicePath = deviceConfiguration.devicePath
        // get the python path
        val pythonPath = module.microPythonFacet?.pythonPath
        // get the script path (from the facet)
        val scriptPath = MicroPythonFacet.scriptsPath

        // run a command to get the files
        val command = "$pythonPath $scriptPath/pyboard.py -d $devicePath -f ls"

        // run the command
        val result = Runtime.getRuntime().exec(command)

        // print the result
        println(result)


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
        getFilesOnDevice()
    }

}