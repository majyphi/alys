package com.github.majestic.autohermod.stockreading.imageloading

import org.opencv.core.Mat
import org.opencv.imgcodecs.Imgcodecs


object IconsLoader {


  def getIcons(iconsPath : String) : List[Icon] = {
    FileUtils.getListOfFilesIn(iconsPath)
      .map(file => {
        val filename = file.getName
        val img = Imgcodecs.imread(iconsPath+filename,Imgcodecs.IMREAD_GRAYSCALE)
        Icon(filename .replaceAll(".png",""),img)
      })

  }

}

case class Icon(name : String, template : Mat)
