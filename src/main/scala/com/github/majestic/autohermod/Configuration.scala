package com.github.majestic.autohermod

import pureconfig._
import pureconfig.generic.auto._

object Configuration {

  def getAutoHermodConfig(confPath : String) = {
    ConfigSource.file(confPath).load[AutoHermodConfig]
      match {
      case Right(value) => value
      case Left(value) => throw new Exception("Unable to load config", new Throwable(value.prettyPrint(1)))
    }
  }

}

case class AutoHermodConfig(
                           token : String,
                           channel : String,
                           selfID : String,
                           digitsImagesPath : String,
                           iconsImagesPath : String,
                           googleCredentialsPath : String,
                           googleTokenDirectory : String
                           )
