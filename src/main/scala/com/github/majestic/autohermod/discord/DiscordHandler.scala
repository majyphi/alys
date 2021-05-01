package com.github.majestic.autohermod.discord

import ackcord.{APIMessage, ClientSettings, DiscordClient, OptFuture}
import com.github.majestic.autohermod.App.logger
import com.github.majestic.autohermod.AutoHermodConfig
import com.github.majestic.autohermod.processing.MessageProcessing

import java.util.concurrent.TimeUnit
import scala.concurrent.Await
import scala.concurrent.duration.Duration

case class DiscordHandler(client: DiscordClient) {

  def runWith(messageProcessing: MessageProcessing): Unit = {

    client.onEventSideEffectsIgnore {
      case APIMessage.Ready(_) => logger.info("AutoHermod is Ready and Listening")
    }

    client.onEventAsync { implicit cache => {
      case APIMessage.MessageCreate(_, message, _) => messageProcessing.processMessageCreated(message)(client, cache)
    }}

    sys.addShutdownHook {
      logger.info("Shutdown hook called. Logging out and stopping... ")
      client.logout()
      client.shutdownAckCord()
      logger.info("AutoHermod is turned off")
    }

    client.login()

  }

}

object DiscordHandler {

  def apply(config: AutoHermodConfig): DiscordHandler = {
    val clientSettings = ClientSettings(config.token)
    val client: DiscordClient = Await.result(clientSettings.createClient(), Duration.create(1, TimeUnit.MINUTES))


    new DiscordHandler(client)
  }

}
