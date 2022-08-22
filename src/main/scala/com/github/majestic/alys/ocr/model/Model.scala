package com.github.majestic.alys.ocr.model

case class ImageItemStock(itemName: String, value: Int)

case class ItemStock(stockpileName: String, itemName: String, value: Int)

object ItemStock {
  def apply(tuple: (String, String, Int)): ItemStock = ItemStock(tuple._1, tuple._2, tuple._3)

  def formatStocks(list: List[ItemStock]): String = {
    s"""```
       |${list.size} trouvé(s) :
       |$header
       |${list.mkString("\n")}
       |```
       |""".stripMargin
  }

  val header = s"Objet\t\t\tQuantité\t\tObjectif\t\tStock"

}

case class Group(groupName: String)

case class Stockpile(stockpileName: String, groupName: String, stockpileType: String)

object Stockpile {
  def apply(tuple: (String, String, String)): Stockpile = Stockpile(tuple._1, tuple._2, tuple._3)
}

case class ItemObjective(groupName: String, itemName: String, value: Int)

object ItemObjective {
  def apply(tuple: (String, String, Int)): ItemObjective = ItemObjective(tuple._1, tuple._2, tuple._3)
}

case class ItemReference(itemName: String, queueCategory: Option[String], bmatCost: Int, ematCost: Int, rmatCost: Int, hematCost: Int)

object ItemReference {
  def apply(tuple: (String, Option[String], Int, Int, Int, Int)): ItemReference = ItemReference(tuple._1, tuple._2, tuple._3, tuple._4, tuple._5, tuple._6)
}

case class ProductionGoal(queueCategory: String, itemName: String, objective: Int, stock: Int)

object ProductionGoal {
  def apply(tuple: (String, String, Int, Int)): ProductionGoal = ProductionGoal(tuple._1, tuple._2, tuple._3, tuple._4)
}

case class SearchResult(itemName: String, stockpileName: String, groupName: String, value: Int)

object SearchResult {
  def apply(tuple: (String, String, String, Int)): SearchResult = SearchResult(tuple._1, tuple._2, tuple._3, tuple._4)
}
