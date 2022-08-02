package com.github.majestic.alys
import org.opencv.core.Mat
import org.opencv.core.MatOfByte
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc

import javax.imageio.ImageIO
import javax.swing.ImageIcon
import javax.swing.JFrame
import javax.swing.JLabel
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.InputStream

object Utils {

  def showImage(img : Mat) = {

    val mat = new MatOfByte
    Imgcodecs.imencode(".jpg", img, mat)
    val byteArray = mat.toArray
    val in = new ByteArrayInputStream(byteArray)
    val buf = ImageIO.read(in)
    val fr = new JFrame
    fr.getContentPane.add(new JLabel(new ImageIcon(buf)))
    fr.pack()
    fr.setVisible(true)

  }

}
