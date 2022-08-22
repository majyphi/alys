package com.github.majestic.alys.calculation

import au.com.bytecode.opencsv.CSVReader
import breeze.linalg.{DenseMatrix, DenseVector, any}

import java.io.FileReader
import scala.jdk.CollectionConverters._
import scala.util.Try

case class GoalsCalculator(recipes: Seq[Recipe]) {

  val transformationMatrix: DenseMatrix[Int] = {
    val vector: Seq[DenseMatrix[Int]] = recipes
      .sortBy(_.id)
      .map(_.recipeVector)
      .map(vector => DenseVector[Int](vector).asDenseMatrix)
    DenseMatrix.vertcat(vector: _*).t
  }

  val fullTransformationMatrix = calculateFullTransformationMatrix()

  def calculateFullTransformationMatrix(agg: DenseMatrix[Int] = transformationMatrix): DenseMatrix[Int] = {
    if (any(agg)) {
      calculateFullTransformationMatrix(agg * transformationMatrix)
    } else agg
  }
}

case class Recipe(id: Int, itemName: String, recipeVector: Array[Int])

object RecipesExtractor {

  def extract(filePath: String): Try[GoalsCalculator] = {
    Try {
      val fileReader = new FileReader(filePath)
      val csvParser = new CSVReader(fileReader)
      val recipes = csvParser.readAll().asScala
        .flatMap {
          case Array("id", "name", "queueCategory", "recipeVector") => None
          case Array(id, itemName, _, recipeVectorString) =>
            Some(
              Recipe(id.toInt,
                itemName,
                recipeVectorString.split(",").map(_.toInt))
            )
          case array => throw new Exception("Can't parse line: " + array.mkString(","))
        }.toSeq

      GoalsCalculator(recipes)
    }
  }

}
