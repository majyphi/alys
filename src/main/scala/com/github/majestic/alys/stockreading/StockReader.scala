package com.github.majestic.alys.stockreading

import com.github.majestic.alys.ALysConfig
import com.github.majestic.alys.stockreading.imageloading.{Digit, DigitsLoader, Icon, IconsLoader}
import com.github.majestic.alys.stockreading.matching.{DigitsLocator, ItemIconLocation, ItemsLocator}
import nu.pattern.OpenCV
import org.opencv.core.{Mat, Size}
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc


case class StockReader(digits: List[Digit], icons: List[Icon]) {

  def extractStocksFromImage(img: Mat) = {

    val resizedImage = new Mat()
    Imgproc.resize(img, resizedImage, new Size(1920, 1080))
    img.release()

    val foundItems = ItemsLocator.identifyItemsFromIcons(icons,resizedImage)

    val result = foundItems.map(_.toItemValueLocation())
      .map(_.extractValueImg(resizedImage))
      .map(DigitsLocator.parseDigits(digits))

    resizedImage.release()

    result

  }

  def extractStocksFromPath(path: String) = {

    val img = Imgcodecs.imread(path, Imgcodecs.IMREAD_GRAYSCALE)

    extractStocksFromImage(img)
  }

}

object StockReader {

  def apply(config: ALysConfig): StockReader = {
    OpenCV.loadLocally()
    val digits = DigitsLoader.getDigits(config.digitsImagesPath)
    val icons = IconsLoader.getIcons(config.iconsImagesPath)
    StockReader(digits, icons)
  }

}


