package com.github.majestic.autohermod

import ackcord.requests.{CreateMessage, CreateMessageData}
import ackcord.{APIMessage, CacheSnapshot, ClientSettings, DiscordClient, OptFuture}
import com.github.majestic.autohermod.googlesheet.SheetHandler
import com.github.majestic.autohermod.imgloading.ImgLoader
import com.github.majestic.autohermod.processing.ItemStocksProcessing
import com.github.majestic.autohermod.stockreading.StockReader
import org.slf4j.LoggerFactory

import java.util.concurrent.TimeUnit
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}

object App {

  val logger = LoggerFactory.getLogger(this.getClass)



  def main(args: Array[String]): Unit = {

    val config: AutoHermodConfig = Configuration.getAutoHermodConfig(args(0))
    val clientSettings = ClientSettings(config.token)
    val client: DiscordClient = Await.result(clientSettings.createClient(), Duration.create(1, TimeUnit.MINUTES))

    client.onEventSideEffectsIgnore {
      case APIMessage.Ready(_) => logger.info("AutoHermod is Ready and Listening")
    }

    implicit val imgLoader = ImgLoader()
    implicit val stockReader = StockReader()
    implicit val sheetHandler = SheetHandler(config)

    client.onEventAsync {
      implicit cache: CacheSnapshot => {
        case APIMessage.MessageCreate(_, message, _) => {

          if (message.channelId.asString == config.channel
            && !message.authorUserId.map(_.toString).contains(config.selfID)
            && message.attachment.nonEmpty
          ) {
            import client.executionContext
            import client.requestsHelper._
            val answer = ItemStocksProcessing.readStocksAndSendToSheet(message.attachment.head) match {
              case Success(_) =>
                s""":white_check_mark: Stocks trouvés et envoyés sur Google Sheet!
                   | [PHASE TEST] Vérifier les résultats sur : https://docs.google.com/spreadsheets/d/1iE1aAd9YFnrUFCppdKk5aUebq1ziIHByjDxu-m3iRck/edit#gid=1753314088""".stripMargin
              case Failure(e) =>
                logger.error("Error during processing", e)
                s""":warning: Error when handling the image
                   |${e.getMessage}
                   |""".stripMargin
            }
            run(CreateMessage(message.channelId, CreateMessageData(answer)))
              .map(_ => ())
          } else {
            OptFuture.unit
          }

        }
      }
    }

    client.login()


    sys.addShutdownHook {
      logger.info("Shutdown hook called. Logging out and stopping... ")
      client.logout()
      client.shutdownAckCord()
      logger.info("AutoHermod is turned off")
    }


  }


}
