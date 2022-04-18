package com.github.majestic.alys.processing

import ackcord.data.Attachment
import com.github.majestic.alys.googlesheet.SheetHandler
import com.github.majestic.alys.imgloading.ImgLoader
import com.github.majestic.alys.stockreading.StockReader
import com.github.majestic.alys.model.ItemStock

import scala.util.{Failure, Success, Try}

object ItemStocksProcessing {

  def readStocksAndSendToSheet(attachment: Attachment, sheetName : String)(implicit imgLoader: ImgLoader, stockReader: StockReader, sheetHandler: SheetHandler): Try[Unit] = {
    for {
      img <- imgLoader.loadImageFromUrl(attachment.url)
      stocks = stockReader.extractStocksFromImage(img)
      refList <- sheetHandler.requestItemList(sheetName)
      stocksToExport <- mergeLists(stocks, refList)
      result <- sheetHandler.writeStocks(stocksToExport,sheetName)
    } yield result
  }

  def mergeLists(stocks: List[Try[ItemStock]], refList: List[String]): Try[List[ItemStock]] = {
    val foundStocks = stocks.filter(_.isSuccess).map(_.get)
    foundStocks match {
      case Seq() => Failure(new Exception("No Stock found ¯\\_(ツ)_/¯"))
      case seq => Success {
        val stocksMap = seq.map(item => (item.name, item.quantity)).toMap
        refList.map(name => {
          val value = stocksMap.getOrElse(name, 0)
          ItemStock(name, value)
        })
      }
    }
  }

  def readStocksFromAttachment(attachment: Attachment)(implicit imgLoader: ImgLoader, stockReader: StockReader): Try[String] = {
    for {
      img <- imgLoader.loadImageFromUrl(attachment.url)
      stocks = stockReader.extractStocksFromImage(img)
      result = formatAnswer(stocks)
    } yield result
  }

  def formatAnswer(stocks: Seq[Try[ItemStock]]): String = {
    val foundStocks = stocks.filter(_.isSuccess).map(_.get)
    ItemStock.formatStocks(foundStocks.toList)
  }

}
