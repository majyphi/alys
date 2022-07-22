package com.github.majestic.alys.model

case class ItemStock(name: String, quantity: Int) {
  def tuple = (name,quantity)
}

object ItemStock {

  def formatStocks(list : List[ItemStock]) : String = {
    s"""```
       |${list.size} trouvé(s) :
       |$header
       |${list.mkString("\n")}
       |```
       |""".stripMargin
  }

  val header = s"Objet\t\t\tQuantité\t\tObjectif\t\tStock"

}
