package com.github.majestic.alys.discord

import ackcord.data.{ApplicationCommand, GuildId}
import ackcord.gateway.GatewaySettings
import ackcord.interactions.InteractionsRegistrar
import ackcord.interactions.commands._
import ackcord.{APIMessage, BotAuthentication, CacheSettings, ClientSettings, DiscordClient, DiscordShard, Events, OptFuture, RequestSettings, Requests}
import com.github.majestic.alys.App.logger
import com.github.majestic.alys.db.DatabaseHandler
import com.github.majestic.alys.{ALysConfig, DiscordConfig}
import com.github.majestic.alys.processing.ScreenshotProcessing

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}



case class DiscordHandler(client: DiscordClient, config: DiscordConfig) {

  def runWith(messageProcessing: ScreenshotProcessing, dbHandler : DatabaseHandler): Unit = {

    import ackcord.requests.{Ratelimiter, RatelimiterActor, RequestSettings}
    import akka.actor.typed._
    import akka.actor.typed.scaladsl._
    import akka.util.Timeout

    import scala.concurrent.duration._

    implicit val system: ActorSystem[Nothing] = ActorSystem(Behaviors.ignore, "AckCord")
    import system.executionContext

    //    val cache =  Events.create()
    //    val ratelimitActor = system.systemActorOf(RatelimiterActor(), "Ratelimiter")

    val dbCommands = new DatabaseCommands(client.requests,dbHandler, config)

    val guildID = GuildId("859088476468150302")

    client.onEventSideEffectsIgnore {
      case msg: APIMessage.Ready =>
        // Create the commands in a specific discord.
        logger.info("Ready. Registering Commands")
        InteractionsRegistrar.createGuildCommands(
          msg.applicationId, // Client ID
          guildID, // Guild ID
          client.requests,
          replaceAll = true, // Boolean whether to replace all existing
          // CreatedGuildCommand*
          dbCommands.pongCommand,
          dbCommands.createStock,
          dbCommands.listStocks
        ).onComplete {
          case Success(result) => logger.info("Success!")
          case Failure(e) => logger.error("Failure!", e)
        }
    }

    client.onEventSideEffectsIgnore { case msg: APIMessage.Ready =>
      client.runGatewayCommands(msg.applicationId.asString)(
        dbCommands.pongCommand,
        dbCommands.createStock,
        dbCommands.listStocks
      )
    }




    //    implicit val requests: Requests = {
    //      implicit val timeout: Timeout = 2.minutes //For the ratelimiter
    //      new Requests(
    //        RequestSettings(
    //          Some(BotAuthentication(config.token)),
    //          Ratelimiter.ofActor(ratelimitActor)
    //        )
    //      )
    //    }
    //
    //    messageProcessing
    //      .getGraphForStockUpload(cache,requests)
    //      .run()
    //
    //    val gatewaySettings = GatewaySettings(config.token)
    //    DiscordShard.fetchWsGateway.foreach { wsUri =>
    //      val shard = system.systemActorOf(DiscordShard(wsUri, gatewaySettings, cache), "DiscordShard")
    //      shard ! DiscordShard.StartShard
    //    }

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
