package com.github.majestic.alys.discord

import ackcord.cachehandlers.CacheSnapshotBuilder
import ackcord.data.TextChannelId
import ackcord.data.raw.RawMessage
import ackcord.requests.{CreateMessage, CreateMessageData}
import ackcord.{APIMessage, CacheSnapshot, ClientSettings, DiscordClient, Events, MemoryCacheSnapshot, OptFuture}
import akka.actor.{ActorSystem, CoordinatedShutdown}
import com.github.majestic.alys.App.logger
import com.github.majestic.alys.ALysConfig
import com.github.majestic.alys.processing.MessageProcessing

import java.util.concurrent.TimeUnit
import scala.concurrent.Await
import scala.concurrent.duration.{Duration, FiniteDuration}

case class DiscordHandler(client: DiscordClient, config: ALysConfig) {

  def runWith(messageProcessing: MessageProcessing): Unit = {

    client.onEventSideEffects { implicit cache => {
      case APIMessage.Ready(_) =>
        OptFuture.pure(logger.info("Login successful. Attempting to notify channel..."))
        Await.result(client.requestsHelper.run(CreateMessage.mkContent(TextChannelId.apply(config.channel),"Ready and listening, Sir!")).value,FiniteDuration(15, TimeUnit.SECONDS))
        match {
          case Some(_) => OptFuture.pure(logger.info("Channel Notified. Waiting for stock information..."))
          case None => OptFuture.pure(logger.error("Could not notify channel of bot availability"))
        }

      case APIMessage.MessageCreate(_, message, _) => messageProcessing.processMessageCreated(message)(client, cache)
    }}

    sys.addShutdownHook{

    }

    client.login()

  }

}

object DiscordHandler {

  def apply(config: ALysConfig): DiscordHandler = {
    val clientSettings = ClientSettings(config.token)
    val client: DiscordClient = Await.result(clientSettings.createClient(), Duration.create(1, TimeUnit.MINUTES))

    new DiscordHandler(client,config)
  }

}
