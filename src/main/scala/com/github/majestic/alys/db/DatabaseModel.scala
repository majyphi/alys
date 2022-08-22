package com.github.majestic.alys.db

import slick.jdbc.H2Profile.api._
import slick.lifted.PrimaryKey

object DatabaseModel {

  val logiStockpileType = "logi"
  val frontStockpileType = "front"
  val stockpileTypeChoices = Seq(logiStockpileType,frontStockpileType)

  class ItemStocksTable(tag: Tag) extends Table[(String, String, Int)](tag, "item_stocks") {
    def stockpileName = column[String]("STOCKPILE_NAME")
    def itemName = column[String]("ITEM_NAME")
    def value = column[Int]("VALUE")

    def pk = primaryKey("pk_item_stocks", (stockpileName, itemName))
    def fk_stockpile = foreignKey("fk_stockpile_name", stockpileName, stockpiles)(_.stockpileName, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)
    def fk_item = foreignKey("fk_item_stockpile_name", itemName, itemReferences)(_.itemName, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

    override def * = (stockpileName, itemName, value)
  }
  val itemStocks = TableQuery[ItemStocksTable]

  class GroupsTable(tag: Tag) extends Table[String](tag, "groups") {
    def stockpileGroup = column[String]("STOCKPILE_GROUP")
    def pk = primaryKey("pk_groups", stockpileGroup)

    override def * = stockpileGroup
  }
  val groups = TableQuery[GroupsTable]

  class StockpilesTable(tag: Tag) extends Table[(String, String, String)](tag, "stockpiles") {
    def stockpileName = column[String]("STOCK_NAME")
    def stockpileGroup = column[String]("STOCK_GROUP")
    def stockpileUnitType = column[String]("STOCK_UNIT_TYPE")

    def pk = primaryKey("pk_stockpiles", stockpileName)

    def fk_group = foreignKey("fk_group",stockpileGroup,groups)(_.stockpileGroup)

    override def * = (stockpileName, stockpileGroup, stockpileUnitType)
  }
  val stockpiles = TableQuery[StockpilesTable]

  class ItemObjectivesTable(tag: Tag) extends Table[(String, String, Int)](tag, "item_objectives") {
    def stockpileGroup = column[String]("STOCK_GROUP")
    def itemName = column[String]("ITEM_NAME")
    def value = column[Int]("VALUE")

    def pk: PrimaryKey = primaryKey("pk_item_objectives", (stockpileGroup,itemName))
    def fk_group = foreignKey("fk_group_name", stockpileGroup, groups)(_.stockpileGroup, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)
    def fk_item = foreignKey("fk_item_objective_name", itemName, itemReferences)(_.itemName, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

    override def * = (stockpileGroup, itemName, value)
  }

  val itemObjectives = TableQuery[ItemObjectivesTable]

  class ItemReferencesTable(tag: Tag) extends Table[(String,Option[String],Option[String],Option[String],String)](tag, "item_references") {
    def itemName = column[String]("ITEM_NAME")
    def queueCategory = column[Option[String]]("QUEUE_CATEGORY")
    def logiUnit = column[Option[String]]("LOGI_UNIT")
    def frontUnit = column[Option[String]]("FRONT_UNIT")
    def recipeVector = column[String]("RECIPE_VECTOR")

    def pk = primaryKey("pk_item_references", itemName)

    override def * = (itemName, queueCategory,logiUnit,frontUnit,recipeVector)
  }
  val itemReferences = TableQuery[ItemReferencesTable]



}



