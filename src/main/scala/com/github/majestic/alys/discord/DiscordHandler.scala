package com.github.majestic.alys.discord

import ackcord.data.GuildId
import ackcord.gateway.GatewaySettings
import ackcord.interactions.InteractionsRegistrar
import ackcord.{APIMessage, BotAuthentication, CacheSettings, ClientSettings, DiscordClient, DiscordShard, Events, RequestSettings, Requests}
import com.github.majestic.alys.App.logger
import com.github.majestic.alys.DiscordConfig
import com.github.majestic.alys.commands.{AlysCommands, ItemCommands, ObjectiveCommands, StockpileCommands}
import com.github.majestic.alys.db.DatabaseHandler
import com.github.majestic.alys.ocr.ScreenshotHandler

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}


case class DiscordHandler(client: DiscordClient, config: DiscordConfig) {

  def runWith(messageProcessing: ScreenshotHandler, dbHandler: DatabaseHandler): Unit = {

    import ackcord.requests.{Ratelimiter, RatelimiterActor, RequestSettings}
    import akka.actor.typed._
    import akka.actor.typed.scaladsl._
    import akka.util.Timeout

    import scala.concurrent.duration._

    implicit val system: ActorSystem[Nothing] = ActorSystem(Behaviors.ignore, "AckCord")
    import system.executionContext

    val cache = Events.create()
    val ratelimitActor = system.systemActorOf(RatelimiterActor(), "Ratelimiter")
    val guildID = GuildId(config.guildId)

    val allCommands = new AlysCommands(client, dbHandler, config).commands

    client.onEventSideEffectsIgnore {
      case msg: APIMessage.Ready =>
        logger.info("Ready. Registering Commands")
        InteractionsRegistrar.createGuildCommands(
          msg.applicationId,
          guildID,
          client.requests,
          replaceAll = true,
          commands = allCommands: _*
        ).onComplete {
          case Success(_) => logger.info("Success!")
          case Failure(e) => logger.error("Failure!", e)
        }
    }

    client.onEventSideEffectsIgnore { case msg: APIMessage.Ready =>
      client.runGatewayCommands(msg.applicationId.toString)(
        commands = allCommands: _*
      )
    }

    implicit val requests: Requests = {
      implicit val timeout: Timeout = 2.minutes //For the ratelimiter
      new Requests(
        RequestSettings(
          Some(BotAuthentication(config.token)),
          Ratelimiter.ofActor(ratelimitActor)
        )
      )
    }

    messageProcessing
      .getGraphForStockUpload(cache, requests)
      .run()

    messageProcessing
      .getBonjourGraph(cache, requests)
      .run()

    val gatewaySettings = GatewaySettings(config.token)
    DiscordShard.fetchWsGateway.foreach { wsUri =>
      val shard = system.systemActorOf(DiscordShard(wsUri, gatewaySettings, cache), "DiscordShard")
      shard ! DiscordShard.StartShard
    }
    client.login()

  }

}


object DiscordHandler {

  def apply(config: DiscordConfig): DiscordHandler = {
    val clientSettings = ClientSettings(
      token = config.token,
      cacheSettings = CacheSettings(parallelism = 1),
      requestSettings = RequestSettings(parallelism = 1)
    )
    val client: DiscordClient = Await.result(clientSettings.createClient(), Duration.Inf)

    new DiscordHandler(client, config)
  }

}
