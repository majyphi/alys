package com.github.majestic.alys.discord

import ackcord.{APIMessage, ClientSettings, DiscordClient, EventListener, EventsController, OptFuture, Requests}
import akka.NotUsed
import com.github.majestic.alys.ALysConfig
import com.github.majestic.alys.App.logger
import com.github.majestic.alys.processing.MessageProcessing

import java.util.concurrent.TimeUnit
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.{Failure, Try}

case class DiscordHandler(client: DiscordClient, config: ALysConfig) {

  def runWith(messageProcessing: MessageProcessing): Unit = {

    client.onEventSideEffects { implicit cache => {
      case APIMessage.Ready(_,_,_) => OptFuture.pure(logger.info("Login successful"))
      case APIMessage.MessageCreate(_, message, _,_) => messageProcessing.processMessageCreated(message)(client, cache)
      case _ => OptFuture.unit
    }}
    client.login()

  }

}

class MyListeners(requests: Requests) extends EventsController(requests) {
  val onLogin: EventListener[APIMessage.Ready, NotUsed] =
    Event.on[APIMessage.Ready].withSideEffects { _ =>
      println("Logged in.")
    }
}

object DiscordHandler {

  def apply(config: ALysConfig): DiscordHandler = {
    val clientSettings = ClientSettings(config.token)
    val client: DiscordClient = Await.result(clientSettings.createClient(), Duration.Inf)

    new DiscordHandler(client,config)
  }

}
