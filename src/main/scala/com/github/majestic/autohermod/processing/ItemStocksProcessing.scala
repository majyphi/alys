package com.github.majestic.autohermod.processing

import ackcord.data.Attachment
import com.github.majestic.autohermod.googlesheet.SheetHandler
import com.github.majestic.autohermod.imgloading.ImgLoader
import com.github.majestic.autohermod.stockreading.StockReader
import com.github.majestic.autohermod.stockreading.model.ItemStock

import scala.util.{Failure, Success, Try}

object ItemStocksProcessing {

  def readStocksAndSendToSheet(attachment: Attachment)(implicit imgLoader: ImgLoader, stockReader: StockReader, sheetHandler: SheetHandler): Try[Unit] = {
    for {
      img <- imgLoader.loadImageFromUrl(attachment.url)
      stocks = stockReader.extractStocksFromImage(img)
      refList <- sheetHandler.requestItemList()
      stocksToExport <- mergeLists(stocks, refList)
      result <- sheetHandler.writeStocks(stocksToExport)
    } yield result


  }

  def mergeLists(stocks: List[Try[ItemStock]], refList: List[String]): Try[List[ItemStock]] = {
    val foundStocks = stocks.filter(_.isSuccess).map(_.get)
    foundStocks match {
      case Seq() => Failure(new Exception("No Stock found ¯\\_(ツ)_/¯"))
      case seq => Success {
        val stocksMap = seq.map(item => (item.name, item.quantity)).toMap
        refList.map(name => {
          val value =  stocksMap.getOrElse(name, 0)
          ItemStock(name,value)
        })
      }
    }
  }

  def readStocksFromAttachment(attachment: Attachment)(implicit imgLoader: ImgLoader, stockReader: StockReader): Try[String] = {
    for {
      img <- imgLoader.loadImageFromUrl(attachment.url)
      stocks = stockReader.extractStocksFromImage(img)
      result <- formatAnswer(stocks)
    } yield result
  }

  def formatAnswer(stocks: Seq[Try[ItemStock]]): Try[String] = {
    val foundStocks = stocks.filter(_.isSuccess).map(_.get)
    foundStocks match {
      case Seq() => Failure(new Exception("No Stock found ¯\\_(ツ)_/¯"))
      case seq => Success(
        seq
          .map(item => {
            s"${item.name}\t${item.quantity}"
          }).mkString("```", "\n", "```")
      )
    }
  }

}
