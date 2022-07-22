package com.github.majestic.alys

import com.github.majestic.alys.db.DatabaseHandler
import com.github.majestic.alys.discord.DiscordHandler
import com.github.majestic.alys.processing.ScreenshotProcessing
import org.slf4j.LoggerFactory

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.{Duration, SECONDS}

object App {

  val logger = LoggerFactory.getLogger(this.getClass)

  def main(args: Array[String]): Unit = {

    val config: ALysConfig = Configuration.getALysConfig(args(0))

    val dbHandler = new DatabaseHandler(config.db)
    val result = Await.result(dbHandler.initDB, Duration(15, SECONDS))

    val messageProcessing = ScreenshotProcessing(config, dbHandler)
    DiscordHandler(config.discord).runWith(messageProcessing, dbHandler)

  }

}
