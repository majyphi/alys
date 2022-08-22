package com.github.majestic.alys.commands

import ackcord.data.{OutgoingEmbed, RoleId}
import ackcord.interactions.commands.{CacheApplicationCommandController, SlashCommand}
import ackcord.interactions.{CommandInteraction, DataInteractionTransformer}
import ackcord.{OptFuture, Requests}
import com.github.majestic.alys.App.logger
import com.github.majestic.alys.DiscordConfig
import com.github.majestic.alys.db.DatabaseHandler
import com.github.majestic.alys.ocr.model.{ItemObjective, ProductionGoal}

import scala.concurrent.Await
import scala.concurrent.duration.{Duration, SECONDS}
import scala.util.{Failure, Success}


class ObjectiveCommands(requests: Requests, dbHandler: DatabaseHandler, config: DiscordConfig) extends CacheApplicationCommandController(requests) {

  val listObjectives: SlashCommand[CommandInteraction, _] =
    SlashCommand
      .withTransformer(DataInteractionTransformer.identity)
      .withParams(string("group", "The name of the group of objectives to list").required)
      .command("get_objectives", "List the existing objectives, within an objective group") { implicit i =>
        async(implicit token => {
          val groupName = i.args
          val result =
            dbHandler.listObjectives(groupName)
              .map {
                case Seq() => OutgoingEmbed(title = Some("No objective is set yet!"))
                case itemObjectives: Seq[ItemObjective] =>
                  val data = itemObjectives
                    .map { case ItemObjective(_, itemName, value) => Seq(itemName, value.toString) }
                  val table = Printer.getTableString(data, Seq("Item", "Objective"))
                  OutgoingEmbed(description = Some(table))
              }
          OptFuture
            .fromFuture(result)
            .flatMap(embed => sendAsyncEmbed(Seq(embed)))
        })
      }


  val setObjective: SlashCommand[CommandInteraction, _] =
    SlashCommand
      .withTransformer(AlysCommands.needRole(config.adminRoles.toSeq.map(RoleId(_))))
      .withParams(
        string("item", "The item on which to set an objective.").required ~
          int("value", "The objective to set on the item.").required ~
          string("group", """The group to which the objective is assigned.""")
      )
      .command("set_objective", "Define the objective for a specific item, within an objective group") { implicit i =>
        async(implicit token => {
          val (itemName, value, groupName) = (i.args._1._1, i.args._1._2, i.args._2)
          val result =
            dbHandler.setObjective(itemName, value, groupName)
              .map {
                case Success(0) => "Something went wrong. No objective was set"
                case Success(_) => s"`$groupName`: Objective for `$itemName` set to `$value`."
                case Failure(e: org.h2.jdbc.JdbcSQLIntegrityConstraintViolationException) =>
                  val itemExists = Await.result(dbHandler.checkItemExists(itemName), Duration(10, SECONDS))
                  val groupExists = Await.result(dbHandler.checkGroupExists(groupName), Duration(10, SECONDS))
                  if (!itemExists) s"Unknown Item `$itemName`. Please select a valid item."
                  else if (!groupExists) s"Unknown Group `$groupName`. Please make sure a stock with that group exists first."
                  else {
                    logger.error("Error during setup of objective", e)
                    s":warning: An error was found when processing the query. I need my supervisor to come take a look: <@${config.adminUserID}>."
                  }
                case Failure(t: Throwable) =>
                  logger.error("Error during setup of objective", t)
                  s":warning: An error was found when processing the query. I need my supervisor to come take a look: <@${config.adminUserID}>."
              }
          OptFuture
            .fromFuture(result)
            .flatMap(msg => sendAsyncMessage(msg))
        })
      }

  val getProductionPriorities: SlashCommand[CommandInteraction, _] =
    SlashCommand
      .withTransformer(DataInteractionTransformer.identity)
      .withParams(string("group", "The name of the group of objectives on which goals will be calculated").required)
      .command("get_production_priorities", "List the production goals, within an objective group") { implicit i =>
        async(implicit token => {
          val groupName = i.args
          val result =
            dbHandler.getProductionGoals(groupName)
              .map {
                case map if map.isEmpty => Seq(OutgoingEmbed(title = Some("No goal found in group. Are objectives set?")))
                case stocks: Map[String, Seq[ProductionGoal]] =>
                  stocks
                    .map { case (category, goals) =>
                      val data: Seq[Seq[String]] = goals
                        .sortBy(_.objective)
                        .map(goal => Seq(goal.itemName, goal.objective.toString, goal.stock.toString))
                      val table = Printer.getTableString(data, Seq("Item", "Current Objective", "Current Stock"))
                      OutgoingEmbed(title = Some(category), description = Some(table))
                    }.toSeq
              }

          OptFuture
            .fromFuture(result)
            .flatMap(embeds => sendAsyncEmbed(embeds = embeds, content = ""))
        })
      }


}
