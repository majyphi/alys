package com.github.majestic.alys.discord

import ackcord.{OptFuture, Requests}
import ackcord.interactions.commands.{CacheApplicationCommandController, ParamList}
import cats.Id
import com.github.majestic.alys.{ALysConfig, DiscordConfig}
import com.github.majestic.alys.App.logger
import com.github.majestic.alys.db.DatabaseHandler

import scala.util.{Failure, Success}


class DatabaseCommands(requests: Requests, dbHandler: DatabaseHandler, config: DiscordConfig) extends CacheApplicationCommandController(requests) {


  val pongCommand = SlashCommand.command("ping", "Check if the bot is alive") { _ =>
    sendMessage("Pong")
  }

  val createStock =
    SlashCommand
      .withParams(string("name", "The stockpile name to create.").required ~ string("group", """The group to which the stockpile belongs. Default will be "global".""").notRequired)
      .command("create", "create a stock with an assignedGroup") { implicit i =>
        async(implicit token => {
          val (stockName, stockGroup) = (i.args._1, i.args._2.getOrElse("global"))
          val result =
            dbHandler.createStock(stockName, stockGroup)
              .map {
                case Success(_) => s"Created stock '$stockName' in group '$stockGroup'"
                case Failure(e: org.h2.jdbc.JdbcSQLIntegrityConstraintViolationException) => s"Stock '$stockName' already exists. Choose a different name"
                case Failure(e: Exception) =>
                  logger.error("Error during insertion of new stock", e)
                  s":warning: An error was found when processing the image. I need my supervisor to come take a look: <@${config.adminUserID}>."
              }
          OptFuture.fromFuture(result)
            .flatMap(msg => sendAsyncMessage(msg))
        })
      }

  val listStocks =
    SlashCommand
      .command("list", "list the created stockpiled") { implicit i =>
        async(implicit token => {
          val result =
            dbHandler.getStocks()
              .map {
                case Seq() => "No stock was registered yet!"
                case stocks: Seq[(String, String)] => stocks
                  .map { case (stockName, groupName) => s"Stock: $stockName - Group: $groupName" }
                  .mkString("\n")
              }
          OptFuture
            .fromFuture(result)
            .flatMap(msg => sendAsyncMessage(msg))
        })
      }

}
