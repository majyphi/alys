package com.github.majestic.alys.stockreading

import com.github.majestic.alys.stockreading.imageloading.IconsLoader
import com.github.majestic.alys.stockreading.matching.ItemsLocator
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

    foundItems.exists(_.icon.name == "dunne_fuelrunner") should be(true)
    foundItems.exists(_.icon.name == "lariat") should be(true)
    foundItems.exists(_.icon.name == "ressources") should be(true)
    foundItems.exists(_.icon.name == "bunker_supplies") should be(true)
    foundItems.exists(_.icon.name == "flare_shells") should be(true)
    foundItems.exists(_.icon.name == "spire") should be(true)

    foundItems.exists(_.icon.name == "drummond_loscann") should be(false)
    foundItems.exists(_.icon.name == "lariat_crate") should be(false)
    foundItems.exists(_.icon.name == "pellet_beam") should be(false)
    foundItems.exists(_.icon.name == "petrole") should be(false)
    foundItems.exists(_.icon.name == "shrapnell_shells") should be(false)
    foundItems.exists(_.icon.name == "caine") should be(false)

  }

}
