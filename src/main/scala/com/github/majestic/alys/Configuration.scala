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
                      sheets: GoogleSheetsConfig,
                      db : DatabaseConfig
                     )

case class ImageProcessing(digitsImagesPath: String,
                           iconsImagesPath: String)

case class DiscordConfig(token: String,
                         channel: String,
                         selfID: String,
                         adminUserID: String
                        )

case class GoogleSheetsConfig(googleCredentialsPath: String,
                              googleTokenDirectory: String,
                              spreadsheetID: String)

case class DatabaseConfig(dataPath : String)

