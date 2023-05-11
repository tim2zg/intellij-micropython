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

fun getMicroUploadCommand(path: String, module: Module): List<String>? {
  val facet = module.microPythonFacet ?: return null
  val pythonPath = facet.pythonPath ?: return null
  val devicePath = facet.getOrDetectDevicePathSynchronously() ?: return null
  val file = StandardFileSystems.local().findFileByPath(path) ?: return null
  val rootDir = getClosestRoot(file, module) ?: return null
  val excludeRoots = ModuleRootManager.getInstance(module).excludeRoots
  val excludes = excludeRoots
      .asSequence()
      .filter { VfsUtil.isAncestor(file, it, false) }
      .map { VfsUtilCore.getRelativePath(it, rootDir) }
      .filterNotNull()
      .map { listOf("-X", it) }
      .flatten()
      .toList()
  val files = ModuleRootManager.getInstance(module).contentRoots
    val lultest = files.map { VfsUtilCore.getRelativePath(file, it) }
  return listOf(pythonPath, "${MicroPythonFacet.scriptsPath}/microupload.py", "-C", rootDir.path) +
      excludes +
      listOf("-v", devicePath, path, lultest.toString())
}

fun betterUpload(path: String, module: Module): MutableList<String> {
    val commandsToRun = mutableListOf<String>()

    val excludeRoots = ModuleRootManager.getInstance(module).excludeRoots

    val allFiles = mutableListOf<String>()
    Files.walk(Paths.get(path))
        .filter { Files.isRegularFile(it) }
        .forEach {
            if (it.toString().endsWith(".py")) {
                for (e in excludeRoots) {
                    if (!it.pathString.contains(e.path.replace("/", "\\"))) {
                        allFiles.add(it.toString())
                    }
                }
            }
        }

    val facet = module.microPythonFacet
    val pythonPath = facet?.pythonPath
    val devicePath = facet?.getOrDetectDevicePathSynchronously()


    for (e in allFiles) {
        commandsToRun.add(pythonPath + " " + MicroPythonFacet.scriptsPath + "/pyboard.py -d " + devicePath + " -f cp " + e + " :")
    }

    return commandsToRun

}

private fun getClosestRoot(file: VirtualFile, module: Module): VirtualFile? {
  val roots = mutableSetOf<VirtualFile>().apply {
    val rootManager = module.rootManager
    addAll(rootManager.contentRoots)
    addAll(rootManager.sourceRoots)
  }
  var parent: VirtualFile? = file
  while (parent != null) {
    if (parent in roots) {
      break
    }
    parent = parent.parent
  }
  return parent ?: module.guessModuleDir()
}
