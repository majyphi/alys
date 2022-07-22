package com.github.majestic.alys.processing

import ackcord.data.Attachment
import com.github.majestic.alys.imgloading.ImgLoader
import com.github.majestic.alys.stockreading.StockReader

import scala.concurrent.duration.{Duration, SECONDS}
import scala.concurrent.{Await, ExecutionContext}
import scala.util.Try
import com.github.majestic.alys.db.DatabaseHandler

object ItemStocksProcessing {

  def readStocksAndSendToSheet(attachment: Attachment, stockName : String)(implicit imgLoader: ImgLoader, stockReader: StockReader, dbHandler: DatabaseHandler, executionContext: ExecutionContext): Try[Any] = {
    for {
      img: Mat <- imgLoader.loadImageFromUrl(attachment.url)
      stocks: Seq[Try[ItemStock]] = stockReader.extractStocksFromImage(img)
      foundStocks: Seq[ItemStock] = stocks.flatMap(_.toOption)
      result <- Try(Await.result(dbHandler.writeStocks(stockName, foundStocks),Duration(10,SECONDS)))
    } yield result
  }

}
