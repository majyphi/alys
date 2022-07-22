package com.github.majestic.alys.db

import slick.jdbc.H2Profile
import slick.jdbc.H2Profile.api._

object DatabaseModel {
  class ItemStocks(tag: Tag) extends Table[(String, String, Int)](tag, "item_stocks") {
    def stockName = column[String]("STOCK_NAME")
    def itemName = column[String]("ITEM_NAME")
    def value = column[Int]("VALUE")

    def pk = primaryKey("pk_item_stocks", (stockName, itemName))
    def fk = foreignKey("fk_item_stocks", (stockName), stockpiles)(_.stockName)

    override def * = (stockName, itemName, value)
  }
  val itemStocks = TableQuery[ItemStocks]

  class Stockpiles(tag: Tag) extends Table[(String, String)](tag, "stockpiles") {
    def stockName = column[String]("STOCK_NAME")
    def stockGroup = column[String]("STOCK_GROUP")

    def pk = primaryKey("pk_stockpiles", (stockName))

    override def * = (stockName, stockGroup)
  }
  val stockpiles = TableQuery[Stockpiles]

  class ItemObjectives(tag: Tag) extends Table[(String, String, Int)](tag, "item_objectives") {
    def stockGroup = column[String]("STOCK_GROUP")
    def itemName = column[String]("ITEM_NAME")
    def value = column[Int]("VALUE")

    def pk = primaryKey("pk_item_objectives", (stockGroup,itemName))
    def fk = foreignKey("fk_item_objectives", (stockGroup), stockpiles)(_.stockGroup)

    override def * = (stockGroup, itemName, value)
  }

  val itemObjectives = TableQuery[ItemObjectives]



}
