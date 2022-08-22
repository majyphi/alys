package com.github.majestic.alys.commands

import ackcord.data.OutgoingEmbed
import ackcord.interactions.{CommandInteraction, DataInteractionTransformer}
import ackcord.interactions.commands.{CacheApplicationCommandController, SlashCommand}
import ackcord.{OptFuture, Requests}
import com.github.majestic.alys.DiscordConfig
import com.github.majestic.alys.db.DatabaseHandler
import com.github.majestic.alys.ocr.model.{ItemStock, SearchResult}


class ItemCommands(requests: Requests, dbHandler: DatabaseHandler, config: DiscordConfig) extends CacheApplicationCommandController(requests) {

  val listItemsInGroup: SlashCommand[CommandInteraction, _] =
    SlashCommand
      .withTransformer(DataInteractionTransformer.identity)
      .withParams(string("group_name", "The group name from which the items are listed").required)
      .command("get_stocks_in_group", "List the total stocks, within an objective group") { implicit i =>
        async(implicit token => {
          val groupName = i.args
          val result =
            dbHandler.totalStocks(groupName, "N/A")
              .map {
                case map if map.isEmpty => Seq(OutgoingEmbed(title = Some("No item found in group.")))
                case stocks: Map[String, Seq[ItemStock]] =>
                  stocks
                    .map { case (category, stocks) =>
                      val data: Seq[Seq[String]] = stocks.map(item => Seq(item.itemName, item.value.toString))
                      val table = Printer.getTableString(data, Seq("Item", "Current Stock"))
                      OutgoingEmbed(title = Some(category), description = Some(table))
                    }.toSeq
              }

          OptFuture
            .fromFuture(result)
            .flatMap(embeds => sendAsyncEmbed(embeds = embeds, content = s"Stocks found in group $groupName"))
        })
      }

  val listItemsInStockpile: SlashCommand[CommandInteraction, _] =
    SlashCommand
      .withTransformer(DataInteractionTransformer.identity)
      .withParams(string("stockpile_name", "The stockpile name from which the items are listed").required)
      .command("get_stocks_in_stockpile", "List the stocks of a stockpile") { implicit i =>
        async(implicit token => {
          val stockpileName = i.args
          val result =
            dbHandler.totalStocks("N/A", stockpileName)
              .map {
                case map if map.isEmpty => Seq(OutgoingEmbed(title = Some("No item found in stockpile.")))
                case stocks: Map[String, Seq[ItemStock]] =>
                  stocks
                    .map { case (category, stocks) =>
                      val data: Seq[Seq[String]] = stocks.map(item => Seq(item.itemName, item.value.toString))
                      val table = Printer.getTableString(data, Seq("Item", "Current Stock"))
                      OutgoingEmbed(title = Some(category), description = Some(table))
                    }.toSeq
              }

          OptFuture
            .fromFuture(result)
            .flatMap(embeds => sendAsyncEmbed(embeds = embeds, content = s"Stocks found in stockpile $stockpileName"))
        })
      }

  val searchItem: SlashCommand[CommandInteraction, _] =
    SlashCommand
      .withTransformer(DataInteractionTransformer.identity)
      .withParams(string("item", "The name of the item you are looking for").required)
      .command("search_item", "Search for a specific item across stockpiles") { implicit i =>
        async(implicit token => {
          val itemName = i.args
          val result =
            dbHandler.findItem(itemName)
              .map {
                case seq if seq.isEmpty => Seq(OutgoingEmbed(title = Some("No item found.")))
                case items: Seq[SearchResult] =>
                  val data: Seq[Seq[String]] = items.map(item => Seq(item.itemName, item.stockpileName, item.groupName, item.value.toString))
                  val table = Printer.getTableString(data, Seq("Item","Stockpile", "Group", "Value"))
                  Seq(OutgoingEmbed(description = Some(table)))
              }

          OptFuture
            .fromFuture(result)
            .flatMap(embeds => sendAsyncEmbed(embeds = embeds, content = s"Search result for $itemName"))
        })
      }

}
