package com.github.majestic.alys.imgloading

import nu.pattern.OpenCV
import org.opencv.core.{Mat, MatOfByte}
import org.opencv.imgcodecs.Imgcodecs

import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.net.{HttpURLConnection, URL}
import javax.imageio.ImageIO
import scala.util.Try


case class ImgLoader() {

  val DiscordImageStorageFormat = "png"

  OpenCV.loadLocally()

  def loadImageFromUrl(url: String): Try[Mat] = {
    val imageUrl = new URL(url)

    imageUrl.openConnection().connect()

    for {
      httpcon <- prepareConnection(imageUrl)
      buffer <- Try(ImageIO.read(httpcon.getInputStream))
      result <- BufferedImage2Mat(buffer, DiscordImageStorageFormat)
    } yield result
  }

  private def BufferedImage2Mat(image: BufferedImage, format: String): Try[Mat] = {
    Try {

      val byteArrayOutputStream: ByteArrayOutputStream = new ByteArrayOutputStream()

      val writeIsSuccessful = ImageIO.write(image, format, byteArrayOutputStream)
      if (!writeIsSuccessful) throw new Exception("Invalid Format. Could not extract data from image")

      val matOfByte = new MatOfByte(byteArrayOutputStream.toByteArray: _*)
      Imgcodecs.imdecode(matOfByte, Imgcodecs.IMREAD_GRAYSCALE)
    }

  }

  def prepareConnection(url: URL): Try[HttpURLConnection] = {
    Try {
      val httpcon = url.openConnection().asInstanceOf[HttpURLConnection]
      httpcon.addRequestProperty("User-Agent", "");
      httpcon
    }
  }

  def extractFormat(url: String): String = {
    val resultingUrl = url.replaceAll("\\?.+", "")

    val array = resultingUrl.split("\\.")

    array.last
  }

}


