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
      case APIMessage.Ready(_) => OptFuture.pure(logger.info("Login successful"))
      case APIMessage.MessageCreate(_, message, _) => messageProcessing.processMessageCreated(message)(client, cache)
    }}

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
