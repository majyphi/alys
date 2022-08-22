package com.github.majestic.alys.ocr

import com.github.majestic.alys.ocr.stockreading.imageloading.IconsLoader
import com.github.majestic.alys.ocr.stockreading.matching.ItemsLocator
import nu.pattern.OpenCV
import org.opencv.core.{Mat, Size}
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ItemLocatorTest extends AnyFlatSpec with Matchers {

  val imgTestPath = "src/test/resources/items_locations/image_with_confusing_icons.png"

  OpenCV.loadLocally()

  "StockReader" should "correctly discriminate between two confusing icons" in {


    val icons = IconsLoader.getIcons("src/test/resources/items_locations/icons")

    val img = Imgcodecs.imread(imgTestPath, Imgcodecs.IMREAD_GRAYSCALE)
    val resizedImage = new Mat()
    Imgproc.resize(img, resizedImage, new Size(1920, 1080))

    val foundItems = ItemsLocator.identifyItemsFromIcons(icons,resizedImage)

    foundItems should have length 6

    foundItems.exists(_.iconName == "dunne_fuelrunner") should be(true)
    foundItems.exists(_.iconName == "lariat") should be(true)
    foundItems.exists(_.iconName == "ressources") should be(true)
    foundItems.exists(_.iconName == "bunker_supplies") should be(true)
    foundItems.exists(_.iconName == "flare_shells") should be(true)
    foundItems.exists(_.iconName == "spire") should be(true)

    foundItems.exists(_.iconName == "drummond_loscann") should be(false)
    foundItems.exists(_.iconName == "lariat_crate") should be(false)
    foundItems.exists(_.iconName == "pellet_beam") should be(false)
    foundItems.exists(_.iconName == "petrole") should be(false)
    foundItems.exists(_.iconName == "shrapnell_shells") should be(false)
    foundItems.exists(_.iconName == "caine") should be(false)

  }

}
