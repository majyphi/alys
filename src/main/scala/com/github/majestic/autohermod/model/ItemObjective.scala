package com.github.majestic.autohermod.model

import com.github.majestic.autohermod.model.ItemObjective.constantSizedLine

case class ItemObjective(name: String, priorityLevel: String, objective: String, stock: String) {
  override def toString() = {
    constantSizedLine(name,priorityLevel,objective,stock)
  }
}

object ItemObjective {

  val nameSize = 15

  def formatObjectives(list: List[ItemObjective]): String = {
    s"""Objectifs :
       |```
       |$header
       |${list.mkString("\n")}
       |```
       |""".stripMargin
  }

  val constantSizedLine = (s1: String, s2: String, s3: String, s4: String) => {
    val builder = StringBuilder.newBuilder
    builder.setLength(60)
    builder.transform(_ => ' ')
    builder
      .insertAll(0, s1)
      .insertAll(20, s2)
      .insertAll(35, s3)
      .insertAll(50, s4)
      .result()
  }

  val header = constantSizedLine("Objet", "Priorité", "Objectif", "Stock")



}
