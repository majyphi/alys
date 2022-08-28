package com.github.majestic.alys.stockreading

import com.github.majestic.alys.ALysConfig
import com.github.majestic.alys.stockreading.imageloading.{Digit, DigitsLoader, Icon, IconsLoader}
import com.github.majestic.alys.stockreading.matching.{DigitsLocator, ItemsLocator}
import nu.pattern.OpenCV
import org.opencv.core.Mat
import org.opencv.imgcodecs.Imgcodecs


case class StockReader(digits: List[Digit], icons: List[Icon]) {

  def extractStocksFromImage(img: Mat) = {


    val foundItems = ItemsLocator.identifyItemsFromIcons(icons,img)

    val result = foundItems.map(_.toItemValueLocation())
      .map(_.extractValueImg(img))
      .map(DigitsLocator.parseDigits(digits))

    img.release()

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


