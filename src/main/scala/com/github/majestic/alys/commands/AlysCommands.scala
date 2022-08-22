package com.github.majestic.alys.commands

import ackcord.DiscordClient
import ackcord.data.{OutgoingEmbed, RoleId}
import ackcord.interactions.commands.{CacheApplicationCommandController, SlashCommand}
import ackcord.interactions.{CommandInteraction, DataInteractionTransformer, Interaction}
import com.github.majestic.alys.DiscordConfig
import com.github.majestic.alys.db.DatabaseHandler

class AlysCommands(client: DiscordClient, dbHandler: DatabaseHandler, config: DiscordConfig) extends CacheApplicationCommandController(client.requests) {

  private val stockpileCommands = new StockpileCommands(client.requests, dbHandler, config)
  private val objectiveCommands = new ObjectiveCommands(client.requests, dbHandler, config)
  private val itemCommands = new ItemCommands(client.requests, dbHandler, config)

  private val allCommands: Seq[SlashCommand[CommandInteraction, _]] = Seq(
    stockpileCommands.listStockpiles,
    stockpileCommands.createStockpile,
    stockpileCommands.deleteStockpile,
    itemCommands.listItemsInStockpile,
    itemCommands.listItemsInGroup,
    itemCommands.searchItem,
    objectiveCommands.listObjectives,
    objectiveCommands.setObjective,
    objectiveCommands.getProductionPriorities
  )

  val formattedHelp: String = allCommands
    .map(command => {
      s""" - ${command.description.getOrElse("")}
         |> `/${command.name} ${command.paramList.map(_.map(_.name).mkString("[", "] [", "]")).getOrElse("")}`
         |""".stripMargin
    }).mkString

  val helpCommand = SlashCommand.command("help", "Prints all the commands available with aLys") {
    implicit i => {
      val outgoingEmbed = OutgoingEmbed(title = Some("Help"), description = Some {
        s"""
           |Here is what you can do with aLys:
           |
           |$formattedHelp
           |""".stripMargin
      })
      sendEmbed(Seq(outgoingEmbed))
    }
  }

  val commands = Seq(helpCommand) ++ allCommands

}

object AlysCommands {
  def needRole[M[A] <: Interaction](
                                     roles: Seq[RoleId]
                                   ): DataInteractionTransformer[M, M] = new DataInteractionTransformer[M, M] {

    override def filter[A](from: M[A]): Either[Option[String], M[A]] = {
      Either.cond(
        from.optMember.get.roleIds.exists(roles.contains),
        from,
        Some("You don't have permission to use this command")
      )
    }
  }


}