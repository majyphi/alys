package com.github.majestic.autohermod.model

import com.github.majestic.autohermod.model.ItemObjective.constantSizedLine

trait ItemObjective

case class NotFoundItemObjective() extends ItemObjective {
  override def toString() = ""
}

case class FoundItemObjective(name: String, priorityLevel: String, objective: String, stock: String) extends ItemObjective {
  override def toString() = {
    constantSizedLine(name, priorityLevel, objective, stock)
  }
}

object ItemObjective {

  val nameSize = 15

  def formatObjectives(objectives: Objectives): List[String] = {
    val armesLegeres = formatObjectivesCatgoery("Armes Légères", objectives.armesLegeres)
    val armesLourdes = formatObjectivesCatgoery("Armes Lourdes", objectives.armesLourdes)
    val utilitaires = formatObjectivesCatgoery("Utilitaires", objectives.utilitaires)
    val soins = formatObjectivesCatgoery("Soins", objectives.soins)
    val supplies = formatObjectivesCatgoery("Supplies", objectives.supplies)
    val materiaux = formatObjectivesCatgoery("Matériaux", objectives.materiaux)

    List(armesLegeres, armesLourdes, utilitaires, soins, supplies, materiaux)
  }

  def formatObjectivesCatgoery(category: String, objectives: List[ItemObjective]): String = {
    s"""Objectifs $category :
       |```$header
       |${objectives.mkString("\n")}```
    """.stripMargin
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
      .replaceAll("""\s+$""", "")
  }

  val header = constantSizedLine("", "Priorité", "Objectif", "Stock")


}

case class Objectives(armesLegeres: List[ItemObjective], armesLourdes: List[ItemObjective], utilitaires: List[ItemObjective], soins: List[ItemObjective], supplies: List[ItemObjective], materiaux: List[ItemObjective])
