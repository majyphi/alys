package com.github.majestic.alys.ocr

import ackcord.data.Attachment
import com.github.majestic.alys.db.DatabaseHandler
import com.github.majestic.alys.ocr.imageloading.ImgLoader
import com.github.majestic.alys.ocr.model.ImageItemStock
import com.github.majestic.alys.ocr.stockreading.StockReader
import org.opencv.core.Mat

import scala.concurrent.duration.{Duration, SECONDS}
import scala.concurrent.{Await, ExecutionContext}
import scala.util.Try

object ItemStocksProcessing {

  def readStocksAndSendToSheet(attachment: Attachment, stockName: String)(implicit imgLoader: ImgLoader, stockReader: StockReader, dbHandler: DatabaseHandler, executionContext: ExecutionContext): Try[Any] = {
    for {
      img: Mat <- imgLoader.loadImageFromUrl(attachment.url)
      stocks: Seq[Try[ImageItemStock]] = stockReader.extractStocksFromImage(img)
      foundStocks: Seq[ImageItemStock] = stocks.flatMap(_.toOption)
      result <- Try(Await.result(dbHandler.writeItems(stockName, foundStocks), Duration(10, SECONDS)))
    } yield result
  }

}
