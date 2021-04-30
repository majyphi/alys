package com.github.majestic.autohermod.stockreading.imageloading

import org.opencv.core.Mat
import org.opencv.imgcodecs.Imgcodecs


object DigitsLoader {

  private val iconsPath = "src/main/resources/images/digits/"

  def getDigits() : List[Digit] = {
    FileUtils.getListOfFilesIn(iconsPath)
      .map(file => {
        val filename = file.getName
        val img = Imgcodecs.imread(iconsPath+filename,Imgcodecs.IMREAD_GRAYSCALE)
        Digit(
          name = filename.replaceAll(".png","")
            .replaceAll("digit_",""),
          template = img)
      })

  }

}

case class Digit(name : String, template : Mat)
