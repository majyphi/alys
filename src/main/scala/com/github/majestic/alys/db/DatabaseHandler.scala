package com.github.majestic.alys.db

import com.github.majestic.alys.DatabaseConfig
import com.github.majestic.alys.db.DatabaseModel._
import com.github.majestic.alys.ocr.model._
import slick.dbio.DBIOAction
import slick.jdbc.H2Profile
import slick.jdbc.H2Profile.api._
import slick.jdbc.meta.MTable
import slick.lifted.CanBeQueryCondition

import java.io.File
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class DatabaseHandler(config: DatabaseConfig) {

  val dbName = "alys"
  val databasePath = new File(config.dataPath).getAbsolutePath
  val dbConnectionUrl = s"jdbc:h2:file:$databasePath/$dbName;DB_CLOSE_ON_EXIT=FALSE;AUTO_RECONNECT=TRUE"
  val db: H2Profile.backend.DatabaseDef = Database.forURL(dbConnectionUrl, driver = "org.h2.Driver", keepAliveConnection = true)

  val createTables = (groups.schema ++ stockpiles.schema ++ itemObjectives.schema ++ itemStocks.schema ++ itemReferences.schema).create

  def initDB(implicit executionContext: ExecutionContext) = {
    for {
      tables: Seq[MTable] <- db.run(MTable.getTables)
      tableNames = tables.map(_.name.name)
      _ <- if (!tableNames.contains(stockpiles.baseTableRow.tableName)) {
        db.run(DBIO.seq(
          createTables,
          sqlu"""INSERT INTO "#${itemReferences.baseTableRow.tableName}" SELECT * FROM CSVREAD('#${config.refPath}')"""
        ))
      } else Future.unit
    } yield ()
  }



  def createStockpile(stockName: String, stockGroup: String, stockpileType : String)(implicit executionContext: ExecutionContext) = {
    for {
      matchingGroupsSize <- db.run(groups.filter(_.stockpileGroup === stockGroup).length.result)
      _ = if (matchingGroupsSize > 1) Future.failed(new Exception("Primary key violation. Groups Table needs to be cleaned."))
      else if (matchingGroupsSize == 0) db.run(groups += (stockGroup))
      result <- db.run((stockpiles += (stockName, stockGroup, stockpileType)).asTry)
    } yield result
  }

  def deleteStockpile(stockName: String) = {
    db.run(stockpiles.filter(_.stockpileName === stockName).delete.asTry)
  }


  def getStockpileList(implicit executionContext: ExecutionContext) = {
    db.run(stockpiles.result).map(_.map(Stockpile.apply))
  }

  def writeItems(stockName: String, foundStocks: Seq[ImageItemStock]) = {
    db.run(DBIO.seq(
      itemStocks.filter(_.stockpileName === stockName).delete,
      itemStocks ++= foundStocks.map(itemStock => (stockName, itemStock.itemName, itemStock.value, "N/A"))
    ))
  }

  def listObjectives(stockGroup: String)(implicit executionContext: ExecutionContext) = {
    db.run((itemObjectives.result)).map(_.map(ItemObjective.apply))
  }

  def listItems(stockName: String)(implicit executionContext: ExecutionContext) = {
    db.run((itemStocks.filter(_.stockpileName === stockName).result)).map(_.map(ItemStock.apply))
  }

  def totalStocks(groupName: String, stockpileName: String)(implicit executionContext: ExecutionContext) = {

    for {
      totalStocks: Seq[(String, String, Int)] <- db.run {
        stockpiles
          .filter(stockpile => stockpile.stockpileGroup === groupName || stockpile.stockpileName === stockpileName)
          .join(itemStocks).on(_.stockpileName === _.stockpileName)
          .map(_._2)
          .groupBy(_.itemName)
          .map { case (itemName, css) => (itemName, css.map(_.value).sum) }
          .map { case (name, value) => (name, value.getOrElse(0)) }
          .join(itemReferences).on(_._1 === _.itemName)
          .map { case ((itemName, total), itemReference) => (itemReference.queueCategory.getOrElse("Other"), itemName, total) }
          .result
      }
      totalStocksPerCategory = totalStocks.groupBy(_._1)
        .map { case (category, seq) => (category, seq
          .map { case (_, itemName, value) => ItemStock("", itemName, value)
          })
        }
    } yield totalStocksPerCategory
  }


  def findItem(itemName: String)(implicit executionContext: ExecutionContext) = {
    db.run {
      itemStocks
        .filter(_.itemName like f"%%$itemName%%")
        .filter(_.value > 0)
        .joinLeft(stockpiles).on(_.stockpileName === _.stockpileName)
        .map { case (itemStock, stockpile) =>
          (
            itemStock.itemName,
            itemStock.stockpileName,
            stockpile.map(_.stockpileGroup).getOrElse(""),
            itemStock.value
          )
        }
        .result
    }.map(_.map(SearchResult.apply))
  }

  def setObjective(itemName: String, value: Int, groupName: String)(implicit executionContext: ExecutionContext): Future[Try[Int]] = {

    val existingEntries: Query[DatabaseModel.ItemObjectivesTable, (String, String, Int), Seq] = itemObjectives
      .filter(_.itemName === itemName)
      .filter(_.stockpileGroup === groupName)

    for {
      entriesLength: Int <- db.run(existingEntries.length.result)
      result: Try[Int] <- if (entriesLength > 1) Future.failed(new Exception("Primary key violation. Objectives Table needs to be cleaned."))
      else if (entriesLength == 1) db.run(existingEntries.update(groupName, itemName, value).asTry)
      else db.run((itemObjectives += (groupName, itemName, value)).asTry)
    } yield result

  }

  def checkItemExists(itemName: String)(implicit executionContext: ExecutionContext): Future[Boolean] = {
    db.run(itemReferences.filter(_.itemName === itemName).length.result).map(_ >= 1)
  }

  def checkGroupExists(groupName: String)(implicit executionContext: ExecutionContext): Future[Boolean] = {
    db.run(groups.filter(_.stockpileGroup === groupName).length.result).map(_ >= 1)
  }

  def getProductionGoals(groupName: String)(implicit executionContext: ExecutionContext) = {

    val totalStocksInGroup =
      stockpiles.filter(_.stockpileGroup === groupName)
        .join(itemStocks).on(_.stockpileName === _.stockpileName)
        .map(_._2)
        .groupBy(_.itemName)
        .map { case (itemName, css) => (itemName, css.map(_.value).sum) }
        .map { case (itemName, value) => (itemName, value.getOrElse(0)) }

    val objectivesAndStocksValues = {
      itemObjectives.filter(_.stockpileGroup === groupName)
        .joinLeft(totalStocksInGroup).on(_.itemName === _._1)
        .map { case (objectives, stocks) => (objectives.itemName, objectives.value, stocks.map(_._2).getOrElse(0)) }
    }

    val objectivesAndStocksValuesAndCategory = objectivesAndStocksValues
      .joinLeft(itemReferences).on(_._1 === _.itemName)
      .map { case (objective, itemReference) => (itemReference.flatMap(_.queueCategory).getOrElse("Other"), objective._1, objective._2, objective._3) }

    db.run(objectivesAndStocksValuesAndCategory.result)
      .map(_.map(ProductionGoal.apply))
      .map(_.groupBy(_.queueCategory))

  }


  def getProductionGoalsForMaterials(groupName: String)(implicit executionContext: ExecutionContext) = {

    //    val objectivesWithRecipe = {
    //      itemObjectives.filter(_.stockpileGroup === groupName)
    //        .joinLeft(itemReferences).on(_.itemName === _.itemName)
    //        .map { case (objective, reference) => (
    //          (objective.stockpileGroup),
    //          objective.value * reference.map(_.bmat_cost).getOrElse(0),
    //          objective.value * reference.map(_.emat_cost).getOrElse(0),
    //          objective.value * reference.map(_.hemat_cost).getOrElse(0),
    //          objective.value * reference.map(_.rmat_cost).getOrElse(0)
    //        )}.groupBy(_._1)
    //        .map{ case (_,css) => (css.map(_._2).sum,css.map(_._3).sum,css.map(_._4).sum,css.map(_._5).sum) }
    //        .result.head
    //      itemObjectives.filter(_.stockpileGroup === groupName)


  }

}
