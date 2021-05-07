package com.github.majestic.autohermod.stockreading.imageloading

import org.opencv.core.Mat
import org.opencv.imgcodecs.Imgcodecs


object DigitsLoader {


  def getDigits(digitsPath : String) : List[Digit] = {
    FileUtils.getListOfFilesIn(digitsPath)
      .map(file => {
        val filename = file.getName
        val img = Imgcodecs.imread(digitsPath+filename,Imgcodecs.IMREAD_GRAYSCALE)
        Digit(
          name = filename.replaceAll(".png","")
            .replaceAll("digit_",""),
          template = img)
      })

  }

}

case class Digit(name : String, template : Mat)
