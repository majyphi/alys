package com.github.majestic.autohermod.stockreading

import com.github.majestic.autohermod.stockreading.imageloading.{Digit, DigitsLoader, Icon, IconsLoader}
import com.github.majestic.autohermod.stockreading.matching.{DigitsLocator, ItemLocator}
import nu.pattern.OpenCV
import org.opencv.core.{Mat, Size}
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc


case class StockReader(digits: List[Digit], icons: List[Icon]) {

  def extractStocksFromImage(img: Mat) = {

    val resizedImage = new Mat()
    Imgproc.resize(img, resizedImage, new Size(1920, 1080))

    icons
      .flatMap(icon => ItemLocator.locateFromIcon(resizedImage)(icon))
      .map(_.toItemValueLocation())
      .map(_.extractValueImg(resizedImage))
      .map(DigitsLocator.parseDigits(digits))

  }

  def extractStocksFromPath(path: String) = {

    val img = Imgcodecs.imread(path, Imgcodecs.IMREAD_GRAYSCALE)

    extractStocksFromImage(img)

  }

}

object StockReader {

  def apply(): StockReader = {
    OpenCV.loadLocally()
    val digits = DigitsLoader.getDigits()
    val icons = IconsLoader.getIcons()
    StockReader(digits, icons)
  }

}


