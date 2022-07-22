package com.github.majestic.alys.stockreading

import com.github.majestic.alys.{ALysConfig, ImageProcessing}
import com.github.majestic.alys.model.ItemStock
import com.github.majestic.alys.stockreading.imageloading.{Digit, DigitsLoader, Icon, IconsLoader}
import com.github.majestic.alys.stockreading.matching.{DigitsLocator, ItemIconLocation, ItemsLocator}
import nu.pattern.OpenCV
import org.opencv.core.{Mat, Size}
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc

import scala.util.Try


case class StockReader(digits: List[Digit], icons: List[Icon]) {

  def extractStocksFromImage(img: Mat): Seq[Try[ItemStock]] = {

    Imgproc.resize(img, img, new Size(1920, 1080))

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

  def apply(config: ImageProcessing): StockReader = {
    OpenCV.loadLocally()
    val digits = DigitsLoader.getDigits(config.digitsImagesPath)
    val icons = IconsLoader.getIcons(config.iconsImagesPath)
    StockReader(digits, icons)
  }

}


