package com.github.majestic.alys

import pureconfig._
import pureconfig.generic.auto._

object Configuration {

  def getALysConfig(confPath : String) = {
    ConfigSource.file(confPath).load[ALysConfig]
      match {
      case Right(value) => value
      case Left(value) => throw new Exception("Unable to load config", new Throwable(value.prettyPrint(1)))
    }
  }

}

case class ALysConfig(
                           token : String,
                           channel : String,
                           selfID : String,
                           digitsImagesPath : String,
                           iconsImagesPath : String,
                           googleCredentialsPath : String,
                           googleTokenDirectory : String,
                           spreadsheetID : String,
                           adminUserID : String
                           )
