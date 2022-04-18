package com.github.majestic.alys.stockreading.imageloading

import java.io.File

object FileUtils {

  def getListOfFilesIn(path: String): List[File] = {
    val d = new File(path)
    if (d.exists && d.isDirectory) {
      d.listFiles.filter(_.isFile).toList
    } else {
      List[File]()
    }
  }

}
