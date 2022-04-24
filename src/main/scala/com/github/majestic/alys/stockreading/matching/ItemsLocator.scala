package com.github.majestic.alys.stockreading.matching

import com.github.majestic.alys.stockreading.imageloading.Icon
import org.opencv.core.{Core, CvType, Mat, Point}
import org.opencv.imgproc.Imgproc
import org.slf4j.LoggerFactory


object ItemsLocator {

  val ValueRegionWidth = 40

  val logger = LoggerFactory.getLogger(this.getClass)
  private val detectionTreshold = 0.90

  def identifyItemsFromIcons(icons: List[Icon], image: Mat) = {
    val itemsFound = icons
      .flatMap(icon => ItemsLocator.locateFromIcon(image)(icon))

    ItemLocationDecider.discriminateMultipleItemsFoundOnSameLocation(itemsFound)

  }


  def locateFromIcon(img: Mat)(icon: Icon): Option[ItemIconLocation] = {
    val matchResult = new Mat

    val template = icon.template
    val resultCols = img.cols - template.cols + 1
    val resultRows = img.rows - template.rows + 1
    matchResult.create(resultRows, resultCols, CvType.CV_32F)

    Imgproc.matchTemplate(img, icon.template, matchResult, Imgproc.TM_CCORR_NORMED)
    val minMaxLocation = Core.minMaxLoc(matchResult)
    if (minMaxLocation.maxVal > detectionTreshold) {
      Some(ItemIconLocation(icon, minMaxLocation.maxVal, minMaxLocation.maxLoc))
    } else {
      logger.info(s"Could not find ${icon.name} in given image")
      None
    }
  }


}

case class ItemLocationDecider(approximatePosition: Point, potentialItems: List[ItemIconLocation]) {

  def squaredDistanceFrom(point: Point): Double = {
    Math.pow(point.y - this.approximatePosition.y, 2) + Math.pow(point.x - this.approximatePosition.x, 2)
  }

  def addPotentialItems(itemLocationDecider: ItemLocationDecider) = {
    this.copy(approximatePosition, potentialItems ++ itemLocationDecider.potentialItems)
  }

  def findBestFittingItem: ItemIconLocation = {
    if(potentialItems.size > 1){
      ItemsLocator.logger.info(s"Confusion between multiple icons in [${approximatePosition.x},${approximatePosition.y}]: ${potentialItems.map(entry => (entry.icon.name, entry.score)).mkString(",")}")
    }
    potentialItems.maxBy(_.score)
  }

}

object ItemLocationDecider {

  val distanceTreshold = 100

  def discriminateMultipleItemsFoundOnSameLocation(items: Seq[ItemIconLocation]): List[ItemIconLocation] = {
    items
      .map(itemIconLocation => ItemLocationDecider(itemIconLocation.position, List(itemIconLocation)))
      .foldLeft(Map[Point, ItemLocationDecider]()) { case (map, currentLocation) =>
        val closeIconPoint = map.keys.find(point => currentLocation.squaredDistanceFrom(point) < 100)
        closeIconPoint match {
          case None => map + (currentLocation.approximatePosition -> currentLocation)
          case Some(point) => map + (point -> map(point).addPotentialItems(currentLocation))
        }
      }
      .values.map(_.findBestFittingItem)
      .toList
  }

}

case class ItemIconLocation(icon: Icon, score: Double, position: Point) {

  def toItemValueLocation(): ItemValueLocation = {
    val itemWidth = icon.template.width()
    val itemHeight = icon.template.height()
    val topLeft = new Point(position.x + itemWidth + 1, position.y)
    val bottomRight = new Point(topLeft.x + ItemsLocator.ValueRegionWidth, position.y + itemHeight)
    ItemValueLocation(
      itemName = icon.name,
      topLeft,
      bottomRight
    )
  }


}

case class ItemValueLocation(itemName: String, topLeft: Point, bottomRight: Point) {

  def extractValueImg(img: Mat): ItemValueImg = {
    val valueImg = img.submat(
      topLeft.y.toInt,
      bottomRight.y.toInt,
      topLeft.x.toInt,
      bottomRight.x.toInt
    )
    ItemValueImg(itemName, valueImg)
  }

}

case class ItemValueImg(itemName: String, img: Mat)


