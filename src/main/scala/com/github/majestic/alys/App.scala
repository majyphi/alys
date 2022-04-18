package com.github.majestic.alys

import com.github.majestic.alys.discord.DiscordHandler
import com.github.majestic.alys.processing.MessageProcessing
import org.slf4j.LoggerFactory

object App {

  val logger = LoggerFactory.getLogger(this.getClass)

  def main(args: Array[String]): Unit = {

    val config: ALysConfig = Configuration.getALysConfig(args(0))

    val messageProcessing = MessageProcessing(config)

    DiscordHandler(config).runWith(messageProcessing)

  }

}
