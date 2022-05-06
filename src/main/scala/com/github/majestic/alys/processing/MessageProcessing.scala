package com.github.majestic.alys.processing

import ackcord.data.Message
import ackcord.requests.{CreateMessage, CreateMessageData}
import ackcord.{CacheSnapshot, DiscordClient, OptFuture}
import com.github.majestic.alys.App.logger
import com.github.majestic.alys.ALysConfig
import com.github.majestic.alys.exceptions.StockNameException
import com.github.majestic.alys.googlesheet.SheetHandler
import com.github.majestic.alys.imgloading.ImgLoader
import com.github.majestic.alys.processing.MessageProcessing.acceptedSheetsNonOfficer
import com.github.majestic.alys.stockreading.StockReader

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success, Try}

class MessageProcessing(implicit imgLoader: ImgLoader, stockReader: StockReader, sheetHandler: SheetHandler, config: ALysConfig) {

  def processMessageCreated(message: Message)(implicit client: DiscordClient, cache: CacheSnapshot): OptFuture[Unit] = {
    if (isMessageAUserUploadInStockUpdate(message)) {
      import client.requestsHelper._

      val stocksProcessingAttempt = for {
        stockName <- getSheetToFill(message)
        res <- ItemStocksProcessing.readStocksAndSendToSheet(message.attachments.head, stockName)
      } yield res

      val answerMessage = stocksProcessingAttempt match {
        case Success(_) =>
          CreateMessageData(
            s""":white_check_mark: Stocks have been updated with your intel!
               |Please check new objectives on: ${sheetHandler.getURL()}
               |""".stripMargin)
        case Failure(e: StockNameException) => CreateMessageData(e.message)
        case Failure(e) =>
          logger.error("Error during processing", e)
          CreateMessageData(
            s""":warning: An error was found when processing the image. I need my supervisor to come take a look: <@${config.adminUserID}>.
               |${e.getMessage}
               |""".stripMargin)
      }

      Await.result(run(CreateMessage(message.channelId, answerMessage)).value, Duration.Inf)
    }
    OptFuture.unit
  }

  def isMessageAUserUploadInStockUpdate(message: Message): Boolean = {
    message.channelId.asString == config.channel &&
      !message.authorUserId.map(_.toString).contains(config.selfID) &&
      message.attachments.nonEmpty
  }

  def getSheetToFill(message: Message): Try[String] = {
    if (acceptedSheetsNonOfficer.contains(message.content)) {
      Success(message.content)
    } else if (message.content.isEmpty) {
      Failure(StockNameException(s"Please specify the stockpile to update : ${acceptedSheetsNonOfficer.mkString(", ")}"))
    } else {
      Failure(StockNameException(s"Stockpile name unknown. Accepted values are : ${acceptedSheetsNonOfficer.mkString(", ")}"))
    }
  }

  def generateSuccessFailureAnswer(processingTentative: Try[Unit]): String = {
    processingTentative match {
      case Success(_) =>
        s""":white_check_mark: Stocks have been updated with your intel!""".stripMargin
      case Failure(e) =>
        s""":warning: An error was found when processing the image. I need my supervisor to come take a look: @${config.adminUserID}.
           |${e.getMessage}
           |""".stripMargin
    }
  }

}

object MessageProcessing {

  val acceptedSheetsNonOfficer = List("LYS1", "LYS2")

  def apply(config: ALysConfig): MessageProcessing = {
    val imgLoader = ImgLoader()
    val stockReader = StockReader(config: ALysConfig)
    val sheetHandler = SheetHandler(config)

    new MessageProcessing()(imgLoader, stockReader, sheetHandler, config)
  }

}
