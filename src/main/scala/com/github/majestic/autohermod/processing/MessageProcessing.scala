package com.github.majestic.autohermod.processing

import ackcord.data.Message
import ackcord.requests.{CreateMessage, CreateMessageData}
import ackcord.{CacheSnapshot, DiscordClient, OptFuture}
import com.github.majestic.autohermod.App.logger
import com.github.majestic.autohermod.AutoHermodConfig
import com.github.majestic.autohermod.googlesheet.SheetHandler
import com.github.majestic.autohermod.imgloading.ImgLoader
import com.github.majestic.autohermod.model.ItemObjective
import com.github.majestic.autohermod.processing.MessageProcessing.{acceptedSheetsNonOfficer, getRandomInsult}
import com.github.majestic.autohermod.stockreading.StockReader

import scala.util.{Failure, Success, Try}

class MessageProcessing(implicit imgLoader: ImgLoader, stockReader: StockReader, sheetHandler: SheetHandler, config: AutoHermodConfig) {


  def processMessageCreated(message: Message)(implicit client: DiscordClient, cache: CacheSnapshot): OptFuture[Unit] = {

    if (isMessageAUserUploadInStockUpdate(message)) {
      import client.executionContext
      import client.requestsHelper._

      val processingTentative : Try[Unit] = for{
        stockName <- getSheetToFill(message)
        res <- ItemStocksProcessing.readStocksAndSendToSheet(message.attachment.head,stockName)
      } yield res

      val baseAnswer = generateSuccessFailureAnswer(processingTentative)
      val listOfObjectives = generateObjectivesAnswer()
      val answerToSend = personnalizeAnswer(message.authorUserId.map(_.toString))(baseAnswer,listOfObjectives)

      run(CreateMessage(message.channelId, CreateMessageData(answerToSend)))
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

  def getSheetToFill(message: Message) : Try[String] = {
    if(acceptedSheetsNonOfficer.contains(message.content)){
      Success(message.content)
    } else if(message.content.isEmpty){
      Failure(new Exception(s"Veuillez renseigner dans le message de l'image le stock à remplir. Valeurs acceptées : ${acceptedSheetsNonOfficer.mkString(", ")}"))
    } else {
      Failure(new Exception(s"Stock indiqué inconnu. Valeurs acceptées : ${acceptedSheetsNonOfficer.mkString(", ")}"))
    }
  }

  def personnalizeAnswer(userId: Option[String])(baseAnswer: String, objectives : String): String = {
    userId match {
      case Some(MessageProcessing.idMajestic) => {
        s"""L'Entité Créatrice demande. AutoHermod s'exécute.
           |${baseAnswer}
           |${objectives}
           |""".stripMargin
      }
      case Some(MessageProcessing.idBeignet) => {
        s"Mais ferme-la, ${getRandomInsult()} !"
      }
      case Some(MessageProcessing.idXiost) => {
        s"""Je t'aime Xiost <3
           |${baseAnswer}
           |${objectives}
           |""".stripMargin
      }
      case Some(MessageProcessing.idSymory) => {
        s"""Bien Maître.
           |${baseAnswer}
           |${objectives}
           |""".stripMargin
      }
      case _ =>  s"""${baseAnswer}
                    |${objectives}
                    |""".stripMargin

    }
  }

  def generateSuccessFailureAnswer(processingTentative : Try[Unit]): String = {
    processingTentative match {
      case Success(_) =>
        s""":white_check_mark: Stocks trouvés et envoyés sur Google Sheet !""".stripMargin
      case Failure(e) =>
        logger.error("Error during processing", e)
        s""":warning: Erreur rencontrée dans le traitement de l'image.
           |${e.getMessage}
           |""".stripMargin
    }
  }

  def generateObjectivesAnswer() : String = {
    sheetHandler.readObjectives() match {
      case Success(list) => ItemObjective.formatObjectives(list)
      case Failure(e) => {
        logger.error("Error during objectives reading", e)
        s""":warning: Erreur rencontrée lors de la récupération des objectifs.
           |${e.getMessage}
           |""".stripMargin
      }
    }
  }
}

object MessageProcessing {

  val idBeignet = "689900878810972226"
  val idXiost = "221209880328011776"
  val idMajestic = "243951995709292544"
  val idSymory = "269144294747668482"

  val officerRoleId = "820753805242925089"

  val insultList = List("coureuse de rempart","puterelle", "orchidoclaste", "nodocéphale", "coprolithe", "alburostre")

  def getRandomInsult() = {
    val index = Math.floor(Math.random()*insultList.size).toInt
    insultList(index)
  }

  val acceptedSheetsNonOfficer = List("Stock1", "Stock2", "Victa1")

  val acceptedSheetsOfficer = acceptedSheetsNonOfficer ++ List("Hermod", "debug")

  def apply(config: AutoHermodConfig): MessageProcessing = {
    val imgLoader = ImgLoader()
    val stockReader = StockReader(config : AutoHermodConfig)
    val sheetHandler = SheetHandler(config)

    new MessageProcessing()(imgLoader, stockReader, sheetHandler, config)
  }


}
