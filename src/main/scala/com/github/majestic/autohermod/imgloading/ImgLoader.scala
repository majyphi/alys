package com.github.majestic.autohermod.imgloading

import nu.pattern.OpenCV
import org.opencv.core.{Mat, MatOfByte}
import org.opencv.imgcodecs.Imgcodecs

import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.net.{HttpURLConnection, URL}
import javax.imageio.ImageIO
import scala.util.Try


case class ImgLoader() {

  OpenCV.loadLocally()

  def loadImageFromUrl(url: String): Try[Mat] = {
    val imageUrl = new URL(url)

    for {
      httpcon <- prepareConnection(imageUrl)
      buffer <- Try(ImageIO.read(httpcon.getInputStream))
      format = extractFormat(url)
      result <- BufferedImage2Mat(buffer,format)
    } yield result
  }

  private def BufferedImage2Mat(image: BufferedImage, format: String): Try[Mat] = {
    Try {
      val byteArrayOutputStream = new ByteArrayOutputStream()
      ImageIO.write(image, format, byteArrayOutputStream)
      byteArrayOutputStream.flush()
      Imgcodecs.imdecode(new MatOfByte(byteArrayOutputStream.toByteArray: _*), Imgcodecs.IMREAD_GRAYSCALE)
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
    val resultingUrl = url.replaceAll("\\?.+","")

     val array =  resultingUrl.split("\\.")

    array.last
  }

}


