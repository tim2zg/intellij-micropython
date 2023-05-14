package com.jetbrains.micropython.run

import com.intellij.openapi.module.Module
import com.intellij.openapi.project.guessModuleDir
import com.intellij.openapi.project.rootManager
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.vfs.StandardFileSystems
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.jetbrains.micropython.settings.MicroPythonFacet
import com.jetbrains.micropython.settings.microPythonFacet
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.pathString

fun betterUpload(path: String, module: Module): String {
    var command = ""

    val excludeRoots = ModuleRootManager.getInstance(module).excludeRoots

    val allFiles = mutableListOf<String>()
    Files.walk(Paths.get(path))
        .filter { Files.isRegularFile(it) }
        .forEach {
            if (it.toString().endsWith(".py")) {
                for (e in excludeRoots) {
                    if (!it.pathString.contains(e.path.replace("/", "\\"))) {
                        if (!it.pathString.contains("onDevice")) {
                            allFiles.add(it.toString())
                        }
                    }
                }
            }
        }

    val facet = module.microPythonFacet
    val pythonPath = facet?.pythonPath
    val devicePath = facet?.getOrDetectDevicePathSynchronously()


    command = pythonPath + " " + MicroPythonFacet.scriptsPath + "/pyboard.py -d " + devicePath + " -f cp "

    for (e in allFiles) {
        command = "$command$e "
    }

    command = "$command:"

    return command

}
