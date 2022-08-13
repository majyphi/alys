package com.github.majestic.alys.db

import com.github.majestic.alys.DatabaseConfig
import com.github.majestic.alys.db.DatabaseModel.{itemObjectives, itemStocks, stockpiles}
import com.github.majestic.alys.model.ItemStock
import slick.jdbc.H2Profile.api._
import slick.jdbc.meta.MTable

import java.io.File
import scala.concurrent.{ExecutionContext, Future}

class DatabaseHandler(config: DatabaseConfig) {


  val dbName = "alys"
  val databasePath = new File(config.dataPath).getAbsolutePath
  val dbConnectionUrl = s"jdbc:h2:file:$databasePath/$dbName;DB_CLOSE_ON_EXIT=FALSE;AUTO_RECONNECT=TRUE"
  val db = Database.forURL(dbConnectionUrl, driver = "org.h2.Driver", keepAliveConnection = true)

  val createTables = (stockpiles.schema ++ itemObjectives.schema ++ itemStocks.schema).createIfNotExists

  def initDB(implicit executionContext: ExecutionContext) = {
    for {
      tables: Seq[MTable] <- db.run(MTable.getTables)
      tableNames = tables.map(_.name.name)
      _ <- if (!tableNames.contains(stockpiles.baseTableRow.tableName)) db.run(createTables) else Future.unit
    } yield ()
  }

  def createStock(stockName: String, stockGroup: String) = {
    db.run((stockpiles += (stockName, stockGroup)).asTry)
  }

  def deleteStock(stockName: String) = {
    db.run(stockpiles.filter(_.stockName === stockName).delete.asTry)
  }


  def getStocks() = {
    db.run(stockpiles.result)
  }

  def writeStocks(stockName: String, foundStocks: Seq[ItemStock]) = {
    db.run((itemStocks ++= foundStocks.map(itemStock => (stockName, itemStock.name, itemStock.quantity))))
  }


}
