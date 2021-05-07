package com.github.majestic.autohermod

import com.github.majestic.autohermod.discord.DiscordHandler
import com.github.majestic.autohermod.processing.MessageProcessing
import org.slf4j.LoggerFactory

object App {

  val logger = LoggerFactory.getLogger(this.getClass)

  def main(args: Array[String]): Unit = {

    val config: AutoHermodConfig = Configuration.getAutoHermodConfig(args(0))

    val messageProcessing = MessageProcessing(config)

    DiscordHandler(config).runWith(messageProcessing)

  }

}
