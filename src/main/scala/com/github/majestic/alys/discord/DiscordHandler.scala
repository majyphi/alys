package com.github.majestic.alys.discord

import ackcord.APIMessage.{ChannelMessage, MessageCreate, TextChannelIdMessage}
import ackcord.data.{GuildGatewayMessage, Message}
import ackcord.{APIMessage, ClientSettings, DiscordClient, EventListener, EventListenerMessage, EventsController, OptFuture, Requests}
import akka.NotUsed
import com.github.majestic.alys.ALysConfig
import com.github.majestic.alys.App.logger
import com.github.majestic.alys.processing.MessageProcessing

import scala.concurrent.Await
import scala.concurrent.duration.Duration

case class DiscordHandler(client: DiscordClient, config: ALysConfig) {

  def runWith(messageProcessing: MessageProcessing): Unit = {

    val myListener = new MessageProcessingListener(client.requests, messageProcessing)
    client.registerListener(myListener.onLogin)
    client.registerListener(myListener.onStockUpload)
    client.login()

  }

}

class MessageProcessingListener(requests: Requests, messageProcessing: MessageProcessing) extends EventsController(requests) {
  val onLogin: EventListener[APIMessage.Ready, NotUsed] =
    Event.on[APIMessage.Ready].withSideEffects { _ =>
      logger.info("Login successful")
    }

  val onStockUpload: EventListener[MessageCreate, NotUsed] =
    Event.on[APIMessage.MessageCreate]
      .withRequestOpt(eventListener => messageProcessing.processMessageCreated(eventListener.event.message))


}

object DiscordHandler {

  def apply(config: ALysConfig): DiscordHandler = {
    val clientSettings = ClientSettings(config.token)
    val client: DiscordClient = Await.result(clientSettings.createClient(), Duration.Inf)

    new DiscordHandler(client,config)
  }

}
