package com.github.majestic.alys.stockreading.matching

import com.github.majestic.alys.{App, Utils}
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
    "1" -> 0.9,
    "6" -> 0.9
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
      val e = new Exception(s"Could not parse any digits on ${itemValueImg.itemName}")
      App.logger.error("Error when parsing digits",e)
      Failure(e)
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

    Imgproc.matchTemplate(workImg, digit.template, matchResult, Imgproc.TM_CCORR_NORMED)
    (for {
      x: Int <- 0 until resultCols
      y: Int <- 0 until resultRows
      value: Double = matchResult.get(y,x)(0)
      digitLocation = DigitLocation(digit.name, x, template.cols(), value) if value > tresholdForDigit
    } yield digitLocation
    ).toList

  }


}

case class DigitLocation(digitValue: String, offset: Int, width: Int, score: Double)


