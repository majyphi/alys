package com.github.majestic.autohermod.processing

import ackcord.data.{Attachment, Message}
import ackcord.requests.{CreateMessage, CreateMessageData}
import ackcord.{CacheSnapshot, DiscordClient, OptFuture}
import com.github.majestic.autohermod.App.logger
import com.github.majestic.autohermod.AutoHermodConfig
import com.github.majestic.autohermod.googlesheet.SheetHandler
import com.github.majestic.autohermod.imgloading.ImgLoader
import com.github.majestic.autohermod.stockreading.StockReader

import scala.util.{Failure, Success}

class MessageProcessing(implicit imgLoader: ImgLoader, stockReader: StockReader, sheetHandler: SheetHandler, config: AutoHermodConfig) {


  def processMessageCreated(message: Message)(implicit client: DiscordClient, cache: CacheSnapshot): OptFuture[Unit] = {

    if (isMessageAUserUploadInStockUpdate(message)) {
      import client.executionContext
      import client.requestsHelper._

      val answer = personnalizeAnswer(message.authorUserId.map(_.toString))(generateBaseAnswer(message.attachment.head))

      run(CreateMessage(message.channelId, CreateMessageData(answer)))
        .map(_ => ())
    } else {
      OptFuture.unit
    }
  }

  def isMessageAUserUploadInStockUpdate(message: Message): Boolean = {
    message.channelId.asString == config.channel &&
      !message.authorUserId.map(_.toString).contains(config.selfID) &&
      message.attachment.nonEmpty
  }

  def personnalizeAnswer(userId: Option[String])(baseAnswer: String): String = {
    userId match {
      case Some(MessageProcessing.idMajestic) => {
        s"""L'Entité Créatrice demande. AutoHermod s'exécute
           |${baseAnswer}
           |""".stripMargin
      }
      case Some(MessageProcessing.idBeignet) => {
        "Mais ferme-la, toi."
      }
      case Some(MessageProcessing.idXiost) => {
        s"""Je t'aime Xiost <3
           |${baseAnswer}
           |""".stripMargin
      }
      case _ => baseAnswer

    }
  }

  def generateBaseAnswer(attachment: Attachment): String = {
    ItemStocksProcessing.readStocksAndSendToSheet(attachment) match {
      case Success(_) =>
        s""":white_check_mark: Stocks trouvés et envoyés sur Google Sheet !""".stripMargin
      case Failure(e) =>
        logger.error("Error during processing", e)
        s""":warning: Erreur rencontrée dans le traitement de l'image.
           |${e.getMessage}
           |""".stripMargin
    }
  }
}

object MessageProcessing {

  val idBeignet = "689900878810972226"
  val idXiost = "221209880328011776"
  val idMajestic = "243951995709292544"


  def apply(config: AutoHermodConfig): MessageProcessing = {
    val imgLoader = ImgLoader()
    val stockReader = StockReader()
    val sheetHandler = SheetHandler(config)

    new MessageProcessing()(imgLoader, stockReader, sheetHandler, config)
  }


}
