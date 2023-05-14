package com.jetbrains.micropython.devices

import com.intellij.openapi.module.Module
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.wm.ToolWindow
import com.jetbrains.micropython.settings.MicroPythonDevicesConfiguration
import com.jetbrains.micropython.settings.MicroPythonFacet
import com.jetbrains.micropython.settings.microPythonFacet
import java.awt.BorderLayout
import java.awt.event.ActionEvent
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.nio.file.Files
import javax.swing.*
import kotlin.io.path.Path


class hehe(val module: Module, toolWindow: ToolWindow) {
    private val deviceConfiguration = MicroPythonDevicesConfiguration.getInstance(module.project)
    val contentPanel = JPanel()
    private val mainText = JLabel()
    private val model = DefaultListModel<String>()
    private val dataList = com.intellij.ui.components.JBList(model)

    // new data object
    data class FileData(val name: String, val size: String)

    init {
        contentPanel.layout = BorderLayout(0, 20)
        contentPanel.border = BorderFactory.createEmptyBorder(40, 0, 0, 0)
        contentPanel.add(createCalendarPanel(), BorderLayout.WEST)
        contentPanel.add(createControlsPanel(toolWindow), BorderLayout.PAGE_END)
        contentPanel.add(createControlsPanel2(), BorderLayout.CENTER)
        // Allow multiple selections

        // Allow multiple selections
        dataList.selectionMode = ListSelectionModel.MULTIPLE_INTERVAL_SELECTION
        updateCurrentDateTime()
        mainText.text = "Files on device"
    }

    private fun createCalendarPanel(): JPanel {
        val calendarPanel = JPanel()
        calendarPanel.add(mainText)
        return calendarPanel
    }

    private fun getFilesOnDevice(): MutableList<FileData> {
        var files = mutableListOf<FileData>()

        // get the device path
        val devicePath = deviceConfiguration.devicePath
        // get the python path
        val pythonPath = module.microPythonFacet?.pythonPath
        // get the script path (from the facet)
        val scriptPath = MicroPythonFacet.scriptsPath

        // run a command to get the files
        val command = "$pythonPath $scriptPath/pyboard.py -d $devicePath -f ls"

        try {
            val process = Runtime.getRuntime().exec(command)
            process.waitFor()
            val exitValue = process.exitValue()
            if (exitValue == 0) {
                println("Command executed successfully")
                val reader = BufferedReader(InputStreamReader(process.inputStream))
                val output = reader.readText()

                // split the output into lines
                var lines = output.split("\r\n")
                // remove first line
                lines = lines.drop(1)
                // remove last line
                lines = lines.dropLast(1)
                lines = lines.dropLast(1)
                // parse the lines
                for (line in lines) {
                    val indexData = line.replace("           ", "").replace("          ", "").replace("         ", "").split(" ")
                    files.add(FileData(indexData[1], indexData[0]))
                }
            } else {
                println("Command failed with exit code $exitValue")
                val reader = BufferedReader(InputStreamReader(process.inputStream))
                val output = reader.readText()
                println(output)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return files
    }

    private fun deleteFile(files: List<String>) {
        // get the device path
        val devicePath = deviceConfiguration.devicePath
        // get the python path
        val pythonPath = module.microPythonFacet?.pythonPath
        // get the script path (from the facet)
        val scriptPath = MicroPythonFacet.scriptsPath

        // run a command to get the files
        var command = "$pythonPath $scriptPath/pyboard.py -d $devicePath -f rm "

        for (file in files) {
            val actualFile = file.split("Size")[0]
            command = "$command:$actualFile "
        }

        try {
            val process = Runtime.getRuntime().exec(command)
            process.waitFor()
            val exitValue = process.exitValue()
            if (exitValue == 0) {
                println("Command executed successfully")
                val reader = BufferedReader(InputStreamReader(process.inputStream))
                val output = reader.readText()
                println(output)
                mainText.text = "Files: $files got deleted"
                updateCurrentDateTime()
            } else {
                println("Command failed with exit code $exitValue")
                val reader = BufferedReader(InputStreamReader(process.inputStream))
                val output = reader.readText()
                println(output)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun getFile(files: List<String>) {
        // see if directory is already created

        val directoryPath = module.project.basePath + "/onDevice"
        val dir = File(directoryPath)

        if (dir.isDirectory) {
            // get the device path
            val devicePath = deviceConfiguration.devicePath
            // get the python path
            val pythonPath = module.microPythonFacet?.pythonPath
            // get the script path (from the facet)
            val scriptPath = MicroPythonFacet.scriptsPath

            // run a command to get the files
            var command = "$pythonPath $scriptPath/pyboard.py -d $devicePath -f cp "

            for (file in files) {
                val actualFile = file.split("Size")[0]
                command = "$command:$actualFile "
            }

            command = command + module.project.basePath + "/onDevice"

            try {
                val process = Runtime.getRuntime().exec(command)
                process.waitFor()
                val exitValue = process.exitValue()
                if (exitValue == 0) {
                    println("Command executed successfully")
                    val reader = BufferedReader(InputStreamReader(process.inputStream))
                    val output = reader.readText()
                    println(output)
                    mainText.text = "Files: $files got uploaded"
                } else {
                    println("Command failed with exit code $exitValue")
                    val reader = BufferedReader(InputStreamReader(process.inputStream))
                    val output = reader.readText()
                    println(output)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else {
            // create new directory
            Files.createDirectory(Path(module.project.basePath + "/onDevice"))

            // get the folder exclude list
            val excludeRoots = ModuleRootManager.getInstance(module).excludeRoots

            // add folder to exclude list
            ModuleRootManager.getInstance(module).excludeRoots[excludeRoots.size + 1] =
                VirtualFileManager.getInstance().findFileByUrl("file://" + module.project.basePath + "/onDevice")!!

            // get the device path
            val devicePath = deviceConfiguration.devicePath
            // get the python path
            val pythonPath = module.microPythonFacet?.pythonPath
            // get the script path (from the facet)
            val scriptPath = MicroPythonFacet.scriptsPath

            // run a command to get the files
            var command = "$pythonPath $scriptPath/pyboard.py -d $devicePath -f cp "

            for (file in files) {
                val actualFile = file.split("Size")[0]
                command = "$command:$actualFile "
            }

            command = command + module.project.basePath + "/onDevice"

            try {
                val process = Runtime.getRuntime().exec(command)
                process.waitFor()
                val exitValue = process.exitValue()
                if (exitValue == 0) {
                    println("Command executed successfully")
                    val reader = BufferedReader(InputStreamReader(process.inputStream))
                    val output = reader.readText()
                    println(output)
                    mainText.text = "Files: $files got downloaded"
                } else {
                    println("Command failed with exit code $exitValue")
                    val reader = BufferedReader(InputStreamReader(process.inputStream))
                    val output = reader.readText()
                    println(output)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }


    private fun createControlsPanel2(): JPanel {
        val controlsPanel = JPanel()
        val refreshDateAndTimeButton = JButton("Delete")
        refreshDateAndTimeButton.addActionListener { e: ActionEvent? ->
            // get selected items
            deleteFile(dataList.selectedValuesList)
        }
        controlsPanel.add(refreshDateAndTimeButton)
        val hideToolWindowButton = JButton("Get")
        hideToolWindowButton.addActionListener { e: ActionEvent? -> getFile(dataList.selectedValuesList) }
        controlsPanel.add(hideToolWindowButton)
        controlsPanel.add(dataList)
        return controlsPanel
    }


    private fun createControlsPanel(toolWindow: ToolWindow): JPanel {
        val controlsPanel = JPanel()
        val refreshDateAndTimeButton = JButton("Refresh")
        refreshDateAndTimeButton.addActionListener { e: ActionEvent? -> updateCurrentDateTime() }
        controlsPanel.add(refreshDateAndTimeButton)
        val hideToolWindowButton = JButton("Hide")
        hideToolWindowButton.addActionListener { e: ActionEvent? -> toolWindow.hide(null) }
        controlsPanel.add(hideToolWindowButton)
        controlsPanel.add(dataList)
        return controlsPanel
    }

    private fun updateCurrentDateTime() {
        model.clear()
        val files = getFilesOnDevice()
        var counter = 0
        for (file in files) {
            model.add(counter, file.name + " Size: " + file.size)
            counter++
        }

    }

}