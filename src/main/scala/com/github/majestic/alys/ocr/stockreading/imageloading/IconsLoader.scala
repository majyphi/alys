package com.github.majestic.alys.ocr.stockreading.imageloading

import org.opencv.core.Mat
import org.opencv.imgcodecs.Imgcodecs


object IconsLoader {


  def getIcons(iconsPath : String) : List[Icon] = {
    val iconsFolderPath = if (iconsPath.endsWith("/")) iconsPath else iconsPath+"/"
    FileUtils.getListOfFilesIn(iconsPath)
      .map(file => {
        val filename = file.getName
        val img = Imgcodecs.imread(iconsFolderPath+filename,Imgcodecs.IMREAD_GRAYSCALE)
        Icon(filename .replaceAll(".png",""),img)
      })

  }

}

case class Icon(name : String, template : Mat)
