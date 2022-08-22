package com.github.majestic.alys

import pureconfig._
import pureconfig.generic.auto._

object Configuration {

  def getALysConfig(confPath: String) = {
    ConfigSource.file(confPath).load[ALysConfig]
    match {
      case Right(value) => value
      case Left(value) => throw new Exception("Unable to load config", new Throwable(value.prettyPrint(1)))
    }
  }

}

case class ALysConfig(imageProcessing: ImageProcessing,
                      discord: DiscordConfig,
                      db : DatabaseConfig
                     )

case class ImageProcessing(digitsImagesPath: String,
                           iconsImagesPath: String)

case class DiscordConfig(token: String,
                         guildId : String,
                         channel: String,
                         selfID: String,
                         adminUserID: String,
                         adminRoles : Array[String]
                        )

case class DatabaseConfig(dataPath : String, refPath : String)

