package com.github.majestic.alys.stockreading.matching

import com.github.majestic.alys.Utils
import com.github.majestic.alys.model.ItemStock
import com.github.majestic.alys.stockreading.imageloading.Digit
import org.opencv.core._
import org.opencv.imgproc.Imgproc
import org.slf4j.LoggerFactory

import java.util
import scala.annotation.tailrec
import scala.util.{Failure, Success, Try}

object DigitsLocator {

  private val DefaultTreshold = 0.94

  private val TresholdMap = Map(
    "0" -> 0.9,
    "1" -> 0.9
  )

  val logger = LoggerFactory.getLogger(this.getClass)

  def parseDigits(digits: List[Digit])(itemValueImg: ItemValueImg): Try[ItemStock] = {

    val foundDigits: List[DigitLocation] = digits
      .flatMap(locateAllInstancesOfDigit(itemValueImg))

   itemValueImg.release

    if (foundDigits.nonEmpty) {
      logger.debug(s"Found Digits : ${foundDigits}")
      val orderedDigits = foundDigits
        .sortBy(_.offset)

      val digitsLocationsToExclude = orderedDigits
        .sliding(2)
        .toList
        .flatMap(getLowestScoreDigitWhenOverlap)

      val digitsToKeep = orderedDigits.filterNot(digitsLocationsToExclude.contains)

        val value = digitsToKeep
        .map(_.digitValue)
        .mkString
        .toInt

      Success(ItemStock(itemValueImg.itemName, value))
    } else {
      Failure(new Exception(s"Could not parse any digits on ${itemValueImg.itemName}"))
    }

  }

  // Flags the low score digit when they are on the same place
  def getLowestScoreDigitWhenOverlap(list : List[DigitLocation]): Option[DigitLocation] = {
    list match {
      case List(left,right) =>
        val limit = left.offset + (left.width/2)
        if(right.offset < limit ){
          Some(list.minBy(_.score))
        } else {
          None
        }
      case _ => None // Should not happen
    }

  }


  def locateAllInstancesOfDigit(itemValueImg: ItemValueImg)(digit: Digit): List[DigitLocation] = {
    val tresholdForDigit = TresholdMap.getOrElse(digit.name,DefaultTreshold)
    val workImg = new Mat
    itemValueImg.img.copyTo(workImg)

    val template = digit.template
    val resultCols = workImg.cols - template.cols + 1
    val resultRows = workImg.rows - template.rows + 1

    val matchResult = new Mat
    matchResult.create(resultRows, resultCols, CvType.CV_32F)

    // WARN
    // Side Effect on img. Handle with care
    // opencv performance relies on mutating the same object in-memory
    def hideFoundDigit(img: Mat, location: Point, template: Mat): Unit = {

      val topLeft: Point = location
      val topRight = new Point(topLeft.x + template.cols() - 1, topLeft.y)
      val bottomRight = new Point(topLeft.x + template.cols() - 1, topLeft.y + template.rows())
      val bottomLeft = new Point(topLeft.x, topLeft.y + template.rows())

      val matOfPoint = new MatOfPoint(topLeft, topRight, bottomRight, bottomLeft)
      val listOfMatOfPoint = new util.ArrayList[MatOfPoint]()
      listOfMatOfPoint.add(matOfPoint)
      Imgproc.fillPoly(img, listOfMatOfPoint, new Scalar(0, 0, 0))
    }

    @tailrec
    def recursivelyFindAllPositions(accumlatedDigits: List[DigitLocation]): List[DigitLocation] = {
      Imgproc.matchTemplate(workImg, digit.template, matchResult, Imgproc.TM_CCORR_NORMED)

      val minMaxLocation = Core.minMaxLoc(matchResult)
      if (minMaxLocation.maxVal < tresholdForDigit) {
        accumlatedDigits
      } else {
        hideFoundDigit(workImg, minMaxLocation.maxLoc, template)
        val offset = minMaxLocation.maxLoc.x.toInt
        val digits = accumlatedDigits.+:(DigitLocation(digit.name, offset, template.cols(), minMaxLocation.maxVal))
        recursivelyFindAllPositions(digits)
      }

    }

    recursivelyFindAllPositions(List())

  }



}

case class DigitLocation(digitValue: String, offset: Int, width: Int, score: Double)


