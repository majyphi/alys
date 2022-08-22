package com.github.majestic.alys.ocr.stockreading

import nu.pattern.OpenCV
import org.opencv.core._
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc

import java.io.{ByteArrayInputStream, File}
import java.util.UUID
import javax.imageio.ImageIO
import javax.swing.JFrame
import scala.::


//Ne fonctionne qu'en 4k
object ImageTool {

  val closenessThreshold = 50
  val stockpileWidth = 800
  val digitAreaTemplateBorder = 14

  val iconWidth = 112
  val iconHeight = 64


  def main(args: Array[String]): Unit = {

    import org.opencv.imgcodecs.Imgcodecs

    OpenCV.loadLocally()
    val digitPattern: Mat = Imgcodecs.imread("resources/images/digit_zone_icon.png", Imgcodecs.IMREAD_GRAYSCALE)

    val imageToSearch: Mat = Imgcodecs.imread("src/test/resources/4k/War-Win64-Shipping 2022-02-27 13-49-21.png", Imgcodecs.IMREAD_GRAYSCALE)

    val stockpileImage = findStockpileLocation(imageToSearch)
    findNumberPositions(stockpileImage, digitPattern)
      .map { case (x, y, _) => extractIcon(stockpileImage, x, y) }
      .foreach(img => Imgcodecs.imwrite(s"resources/images/export_icons/${UUID.randomUUID().toString}.png", img))

    deduplicateImages

  }

  def deduplicateImages(): Unit = {
    import java.io.File
    val listOfFileNames = getListOfFiles("resources/images/export_icons")

    val listOfImages = listOfFileNames
      .map(name => (name, Imgcodecs.imread(name, Imgcodecs.IMREAD_GRAYSCALE)))


    val deduplicatedListOfImages =
      listOfImages.foldLeft(Seq[(String, Mat)]()) { case (uniqueImages, (name, currentImage)) => {
        if (uniqueImages.isEmpty) Seq((name, currentImage))
        else {

          val duplication = uniqueImages.map(_._2).map(uniqueImage => {
            val errorL2 = Core.norm(uniqueImage, currentImage, Core.NORM_L2)
            1 - (errorL2 / (uniqueImage.height() * uniqueImage.width()))
          })


          if (duplication.forall(_ < 0.5)) uniqueImages.concat(Seq((name, currentImage)))
          else uniqueImages
        }
      }


      }.map(_._1)

    listOfFileNames.filter(name => !deduplicatedListOfImages.contains(name))
      .foreach(name => new File(name).delete())


  }

  def getListOfFiles(dir: String): List[String] = {
    val file = new File(dir)
    file.listFiles.filter(_.isFile)
      .map(_.getPath).toList
  }

  def showImage(img: Mat): Unit = {

    val height = Math.min(img.height(), 1080)
    val width = Math.min(img.width(), 1920)


    Imgproc.resize(img, img, new Size(width, height))

    val mat = new MatOfByte
    Imgcodecs.imencode(".jpg", img, mat)
    val byteArray = mat.toArray
    val in = new ByteArrayInputStream(byteArray)
    val buf = ImageIO.read(in)
    val fr = new JFrame

    import javax.swing.{ImageIcon, JLabel}
    fr.add(new JLabel(new ImageIcon(buf)))
    fr.setSize(width + 50, height + 50)
    fr.setVisible(true)
  }

  def extractIcon(img: Mat, x: Int, y: Int): Mat = {
    img.submat(
      new Rect(x - iconWidth + digitAreaTemplateBorder,
        y + digitAreaTemplateBorder
        , iconWidth, iconHeight
      )
    )
  }


  def findNumberPositions(img: Mat, template: Mat) = {

    val matchResult = new Mat

    val resultCols = img.cols - template.cols + 1
    val resultRows = img.rows - template.rows + 1
    matchResult.create(resultRows, resultCols, CvType.CV_32F)

    Imgproc.matchTemplate(img, template, matchResult, Imgproc.TM_CCORR_NORMED)


    (for {
      x: Int <- 0 until resultCols
      y: Int <- 0 until resultRows
      value: Double = matchResult.get(y, x)(0)
      result = (x, y, value) if value > 0.75
    } yield result)
      .map { case (x, y, value) => ItemLocationDecider(new Point(x, y), List((x, y, value))) }
      .foldLeft(Map[Point, ItemLocationDecider]()) { case (map, currentLocation) =>
        val closeIconPoint = map.keys.find(point => currentLocation.squaredDistanceFrom(point) < closenessThreshold)
        closeIconPoint match {
          case None => map + (currentLocation.approximatePosition -> currentLocation)
          case Some(point) => map + (point -> map(point).addPotentialItems(currentLocation))
        }
      }
      .values
      .map(_.findBestFittingItem)
  }

  def findStockpileLocation(img: Mat): Mat = {
    val template = Imgcodecs.imread("resources/images/soldier_supplies_4k.png", Imgcodecs.IMREAD_GRAYSCALE)
    val matchResult = new Mat

    val resultCols = img.cols - template.cols + 1
    val resultRows = img.rows - template.rows + 1
    matchResult.create(resultRows, resultCols, CvType.CV_32F)
    Imgproc.matchTemplate(img, template, matchResult, Imgproc.TM_CCORR_NORMED)

    val minMaxLocation = Core.minMaxLoc(matchResult)

    val topLeft = minMaxLocation.maxLoc

    img.submat(new Rect(
      new Point(topLeft.x, topLeft.y - digitAreaTemplateBorder),
      new Point(topLeft.x + stockpileWidth, img.height())
    ))

  }

}


case class ItemLocationDecider(approximatePosition: Point, potentialItems: List[(Int, Int, Double)]) {

  def squaredDistanceFrom(point: Point): Double = {
    Math.sqrt(Math.pow(point.y - this.approximatePosition.y, 2) + Math.pow(point.x - this.approximatePosition.x, 2))
  }

  def addPotentialItems(itemLocationDecider: ItemLocationDecider) = {
    this.copy(approximatePosition, potentialItems ++ itemLocationDecider.potentialItems)
  }

  def findBestFittingItem: (Int, Int, Double) = {
    potentialItems.maxBy(_._3)
  }

}
