package com.github.majestic.autohermod.stockreading.matching

import com.github.majestic.autohermod.stockreading.imageloading.Icon
import org.opencv.core.{Core, CvType, Mat, Point}
import org.opencv.imgproc.Imgproc
import org.slf4j.LoggerFactory



object ItemLocator {

  private val Treshold = 0.9
  val ValueRegionWidth = 40

  val logger = LoggerFactory.getLogger(this.getClass)

  def locateFromIcon(img : Mat)(icon : Icon) : Option[ItemIconLocation] = {
    val matchResult = new Mat

    val template = icon.template
    val resultCols = img.cols - template.cols + 1
    val resultRows = img.rows - template.rows + 1
    matchResult.create(resultRows, resultCols, CvType.CV_32F)

    Imgproc.matchTemplate(img, icon.template, matchResult, Imgproc.TM_CCORR_NORMED)
    val minMaxLocation = Core.minMaxLoc(matchResult)
    if (minMaxLocation.maxVal > Treshold) {
      Some(ItemIconLocation(icon,minMaxLocation.maxLoc))
    } else {
      logger.warn(s"Could not find ${icon.name} in given image")
      None
    }
  }

}

case class ItemIconLocation(icon : Icon, position : Point){

  def toItemValueLocation() : ItemValueLocation = {
    val itemWidth = icon.template.width()
    val itemHeight = icon.template.height()
    val topLeft = new Point(position.x+itemWidth+1,position.y)
    val bottomRight = new Point(topLeft.x+ItemLocator.ValueRegionWidth,position.y+itemHeight)
    ItemValueLocation(
      itemName = icon.name,
      topLeft,
      bottomRight
    )
  }

}

case class ItemValueLocation(itemName : String, topLeft : Point, bottomRight : Point){

  def extractValueImg(img : Mat) : ItemValueImg = {
    val valueImg = img.submat(
      topLeft.y.toInt,
      bottomRight.y.toInt,
      topLeft.x.toInt,
      bottomRight.x.toInt
    )
    ItemValueImg(itemName,valueImg)
  }

}

case class ItemValueImg(itemName : String, img : Mat)


