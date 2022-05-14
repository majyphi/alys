package com.github.majestic.alys.discord

import ackcord.APIMessage.MessageCreate
import ackcord.data.Message
import ackcord.gateway.GatewaySettings
import ackcord.requests.CreateMessage
import ackcord.{APIMessage, BotAuthentication, CacheSettings, ClientSettings, DiscordClient, DiscordShard, EventListener, Events, EventsController, RequestSettings, Requests}
import akka.NotUsed
import akka.stream.QueueOfferResult
import akka.stream.scaladsl.{Keep, Source}
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

  def queueRunWith(messageProcessing: MessageProcessing): Unit = {

    import akka.actor.typed._
    import akka.actor.typed.scaladsl._
    import akka.stream.scaladsl.Sink
    import akka.util.Timeout
    import scala.concurrent.duration._
    import ackcord.requests.{Ratelimiter, RatelimiterActor, RequestSettings}

    implicit val system: ActorSystem[Nothing] = ActorSystem(Behaviors.ignore, "AckCord")
    import system.executionContext

    val cache =  Events.create(parallelism = 2)
    val ratelimitActor = system.systemActorOf(RatelimiterActor(), "Ratelimiter")

    implicit val requests = {
      implicit val timeout: Timeout = 2.minutes //For the ratelimiter
      new Requests(
        RequestSettings(
          Some(BotAuthentication(config.token)),
          Ratelimiter.ofActor(ratelimitActor)
        )
      )
    }

    cache
      .subscribeAPI
      .collectType[APIMessage.MessageCreate]
      .map(_.message)
      .map(x => {
        messageProcessing.queue.offer(x) match {
          case QueueOfferResult.Enqueued    => logger.debug(s"enqueued $x")
          case QueueOfferResult.Dropped     => logger.warn(s"dropped $x")
          case QueueOfferResult.Failure(ex) => logger.error(s"Offer failed ${ex.getMessage}")
          case QueueOfferResult.QueueClosed => logger.error(("Source Queue closed"))
        }
      })
      .runWith(Sink.ignore)

    val gatewaySettings = GatewaySettings(config.token)
    DiscordShard.fetchWsGateway.foreach { wsUri =>
      val shard = system.systemActorOf(DiscordShard(wsUri, gatewaySettings, cache), "DiscordShard")
      shard ! DiscordShard.StartShard
    }



  }

}

class MessageProcessingListener(requests: Requests, messageProcessing: MessageProcessing) extends EventsController(requests) {
  val onLogin: EventListener[APIMessage.Ready, NotUsed] =
    Event.on[APIMessage.Ready].withSideEffects { _ =>
      logger.info("Login successful")
    }

  val onStockUpload: EventListener[MessageCreate, NotUsed] =
    TextChannelEvent
      .on[APIMessage.MessageCreate]
      .withRequestOpt(eventListener => messageProcessing.processMessageCreated(eventListener.event.message))

}

object DiscordHandler {

  def apply(config: ALysConfig): DiscordHandler = {
    val clientSettings = ClientSettings(
      token = config.token,
      cacheSettings = CacheSettings(parallelism = 1),
      requestSettings = RequestSettings(parallelism = 1)
    )
    val client: DiscordClient = Await.result(clientSettings.createClient(), Duration.Inf)

    new DiscordHandler(client, config)
  }

}
