package com.github.majestic.alys.commands

import ackcord.data.{OutgoingEmbed, RoleId}
import ackcord.interactions.{CommandInteraction, DataInteractionTransformer, ResolvedCommandInteraction}
import ackcord.interactions.commands.{CacheApplicationCommandController, SlashCommand}
import ackcord.{OptFuture, Requests}
import akka.NotUsed
import cats.Id
import com.github.majestic.alys.App.logger
import com.github.majestic.alys.DiscordConfig
import com.github.majestic.alys.db.DatabaseHandler
import com.github.majestic.alys.db.DatabaseModel.stockpileTypeChoices
import com.github.majestic.alys.ocr.model.Stockpile

import scala.util.{Failure, Success}


class StockpileCommands(requests: Requests, dbHandler: DatabaseHandler, config: DiscordConfig) extends CacheApplicationCommandController(requests) {


  val createStockpile: SlashCommand[CommandInteraction, _] =
    SlashCommand
      .withTransformer(AlysCommands.needRole(config.adminRoles.toSeq.map(RoleId(_))))
      .withParams(string("name", "The stockpile name to create.").required
        ~ string("group", """The group to which the stockpile belongs.""")
        ~ string("type", """The type of stockpile you want to create: Logi or Front.""").withChoices(stockpileTypeChoices).required
      )
      .command("create_stockpile", "Create a stockpile, within an objective group") { implicit i =>
        async(implicit token => {
          val (stockName, stockGroup, stockType) = (i.args._1._1, i.args._1._2, i.args._2)
          val result =
            dbHandler.createStockpile(stockName, stockGroup, stockType)
              .map {
                case Success(_) => s"Created Stockpile '$stockName' in group '$stockGroup' of type '$stockType'"
                case Failure(t: Throwable) =>
                  logger.error("Error during insertion of new Stockpile", t)
                  s":warning: An error was found when processing the query. I need my supervisor to come take a look: <@${config.adminUserID}>."
              }
          OptFuture.fromFuture(result)
            .flatMap(msg => sendAsyncMessage(msg))
        })
      }

  val deleteStockpile: SlashCommand[CommandInteraction, _] =
    SlashCommand
      .withTransformer(AlysCommands.needRole(config.adminRoles.toSeq.map(RoleId(_))))
      .withParams(string("name", "The stockpile name to delete.").required)
      .command("delete_stockpile", "Delete a stockpile") { implicit i =>
        async(implicit token => {
          val stockName = i.args
          val result =
            dbHandler
              .deleteStockpile(stockName)
              .map {
                case Success(0) => s"'$stockName' not found. Not stock was deleted"
                case Success(_) => s"Deleted stockpile '$stockName'"
                case Failure(e: org.h2.jdbc.JdbcSQLIntegrityConstraintViolationException) => s"Stockpile '$stockName' already exists. Choose a different name"
                case Failure(t: Throwable) =>
                  logger.error("Error during deletion of stockpile", t)
                  s":warning: An error was found when processing the query. I need my supervisor to come take a look: <@${config.adminUserID}>."
              }
          OptFuture.fromFuture(result)
            .flatMap(msg => sendAsyncMessage(msg))
        })
      }

  val listStockpiles: SlashCommand[CommandInteraction, _] =
    SlashCommand
      .withTransformer(DataInteractionTransformer.identity)
      .command("list_stockpiles", "List all the created stockpiles") { implicit i =>
        async(implicit token => {
          val result =
            dbHandler.getStockpileList
              .map {
                case Seq() => OutgoingEmbed(title = Some("No stockpile is registered yet!"))
                case stockpiles: Seq[Stockpile] =>
                  val data = stockpiles
                    .map { case Stockpile(stockpileName, groupName, stockpileType) => Seq(stockpileName, groupName, stockpileType) }
                  val table = Printer.getTableString(data, Seq("Stockpile", "Group", "Type"))
                  OutgoingEmbed(description = Some(table))
              }
          OptFuture
            .fromFuture(result)
            .flatMap(embed => sendAsyncEmbed(Seq(embed)))
        })
      }





}
