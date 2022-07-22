package com.github.majestic.alys.processing

import ackcord.data.Message
import ackcord.requests.{CreateMessage, CreateMessageData}
import ackcord.{APIMessage, Cache, Requests}
import akka.NotUsed
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Flow, RunnableGraph, Sink}
import com.github.majestic.alys.{ALysConfig, DiscordConfig}
import com.github.majestic.alys.App.logger
import com.github.majestic.alys.db.DatabaseHandler
import com.github.majestic.alys.exceptions.StockNameException
import com.github.majestic.alys.googlesheet.SheetHandler
import com.github.majestic.alys.imgloading.ImgLoader
import com.github.majestic.alys.stockreading.StockReader

import scala.concurrent.duration.{Duration, SECONDS}
import scala.concurrent.{Await, ExecutionContext}
import scala.util.{Failure, Success, Try}

class ScreenshotProcessing(implicit imgLoader: ImgLoader, stockReader: StockReader, config: DiscordConfig, dbHandler: DatabaseHandler) {

  def getGraphForStockUpload(events: Cache, requests: Requests)(implicit executionContext: ExecutionContext): RunnableGraph[NotUsed] = {
    events
      .subscribeAPI
      .collectType[APIMessage.MessageCreate]
      .map(_.message)
      .filter(isMessageAUserUploadInStockUpdate)
      .buffer(1, OverflowStrategy.backpressure)
      .via(Flow.fromFunction(processImage))
      .to(Sink.foreach[CreateMessage](msg => requests.singleIgnore(msg)))

  }

  def processImage(message: Message)(implicit executionContext: ExecutionContext): CreateMessage = {
    logger.info("Begin processing: " + message.id.asString)

    val stockNamesList = Await.result(dbHandler.getStocks(), Duration(10, SECONDS)).map(_._1)
    val stocksProcessingAttempt: Try[Any] = for {
      stockName : String <- getStockName(message, stockNamesList)
      res <- ItemStocksProcessing.readStocksAndSendToSheet(message.attachments.head, stockName)
    } yield res

    val answerMessage = stocksProcessingAttempt match {
      case Success(_) =>
        CreateMessageData(
          s""":white_check_mark: Stocks have been updated with your intel!
             |""".stripMargin)
      case Failure(e: StockNameException) => CreateMessageData(e.message)
      case Failure(e) =>
        logger.error("Error during processing", e)
        CreateMessageData(
          s""":warning: An error was found when processing the image. I need my supervisor to come take a look: <@${config.adminUserID}>.
             |${e.getMessage}
             |""".stripMargin)
    }
    logger.info("End processing: " + message.id.asString)

    CreateMessage(message.channelId, answerMessage.copy(replyTo = Some(message.id)))
  }

  private def isMessageAUserUploadInStockUpdate(message: Message): Boolean = {
    message.channelId.asString == config.channel &&
      !message.authorUserId.map(_.toString).contains(config.selfID) &&
      message.attachments.nonEmpty
  }

  private def getStockName(message: Message, stockNamesList: Seq[String]): Try[String] = {
    if (stockNamesList.contains(message.content)) {
      Success(message.content)
    } else if (message.content.isEmpty) {
      Failure(StockNameException(s"Please specify the stockpile to update. It should match an existing stockpile name : ${stockNamesList.mkString(", ")}"))
    } else {
      Failure(StockNameException(s"Stockpile name unknown. It should match an existing stockpile name: ${stockNamesList.mkString(", ")}.\nOr create a new stockpile with the `/create` command"))
    }
  }


}

object ScreenshotProcessing {


  def apply(config: ALysConfig, dbHandler: DatabaseHandler): ScreenshotProcessing = {
    val imgLoader = ImgLoader()
    val stockReader = StockReader(config.imageProcessing)
    new ScreenshotProcessing()(imgLoader, stockReader, config.discord, dbHandler)
  }

}
