package com.github.majestic.alys.ocr

import ackcord.data.{Attachment, Message, UserId}
import ackcord.requests.{CreateMessage, CreateMessageData}
import ackcord.{APIMessage, Cache, Requests}
import akka.NotUsed
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Flow, RunnableGraph, Sink}
import com.github.majestic.alys.App.logger
import com.github.majestic.alys.db.DatabaseHandler
import com.github.majestic.alys.ocr.exceptions.StockNameException
import com.github.majestic.alys.ocr.imageloading.ImgLoader
import com.github.majestic.alys.ocr.model.ImageItemStock
import com.github.majestic.alys.ocr.stockreading.StockReader
import com.github.majestic.alys.{ALysConfig, DiscordConfig}
import org.opencv.core.Mat

import scala.concurrent.duration.{Duration, SECONDS}
import scala.concurrent.{Await, ExecutionContext}
import scala.util.{Failure, Success, Try}

class ScreenshotHandler(implicit imgLoader: ImgLoader, stockReader: StockReader, config: DiscordConfig, dbHandler: DatabaseHandler) {

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

  def getBonjourGraph(events: Cache, requests: Requests)(implicit executionContext: ExecutionContext): RunnableGraph[NotUsed] = {
    events
      .subscribeAPI
      .collectType[APIMessage.MessageCreate]
      .map(_.message)
      .filter(msg => msg.mentions.contains(UserId(config.selfID)) && msg.authorUserId.isDefined)
      .to(Sink.foreach[Message](msg => requests.singleIgnore(CreateMessage(msg.channelId,CreateMessageData(f"Bonjour <@${msg.authorUserId.get}> !")))))

  }

  def processImage(message: Message)(implicit executionContext: ExecutionContext): CreateMessage = {
    logger.info("Begin processing: " + message.id)

    val stockNamesList = Await.result(dbHandler.getStockpileList, Duration(10, SECONDS)).map(_.stockpileName)
    val stocksProcessingAttempt: Try[Any] = for {
      stockName : String <- getStockName(message, stockNamesList)
      res <- readStocksAndSendToDB(message.attachments.head, stockName)
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
    logger.info("End processing: " + message.id)

    CreateMessage(message.channelId, answerMessage.copy(replyTo = Some(message.id)))
  }

  private def isMessageAUserUploadInStockUpdate(message: Message): Boolean = {
    message.channelId.toString == config.channel &&
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

  def readStocksAndSendToDB(attachment: Attachment, stockName: String)(implicit executionContext: ExecutionContext): Try[Any] = {
    for {
      img: Mat <- imgLoader.loadImageFromUrl(attachment.url)
      stocks: Seq[Try[ImageItemStock]] = stockReader.extractStocksFromImage(img)
      foundStocks: Seq[ImageItemStock] = stocks.flatMap(_.toOption)
      result <- Try(Await.result(dbHandler.writeItems(stockName, foundStocks), Duration(10, SECONDS)))
    } yield result
  }


}

object ScreenshotHandler {



  def apply(config: ALysConfig, dbHandler: DatabaseHandler): ScreenshotHandler = {
    val imgLoader = ImgLoader()
    val stockReader = StockReader(config.imageProcessing)
    new ScreenshotHandler()(imgLoader, stockReader, config.discord, dbHandler)
  }


}
