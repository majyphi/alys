package com.github.majestic.alys.calculation

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class GoalsCalculatorTest extends AnyFlatSpec with Matchers {

  "RecipesExtractor" should "correctly give a GoalsCalculator with the correct recipe Matrix" in {
    RecipesExtractor.extract("src/test/resources/data/item_recipes.csv") should be a Symbol("Success")

  }


  "GoalsCalculator" should "correctly initialize a transformation Matrix from recipes" in {
    val matrix = RecipesExtractor.extract("src/test/resources/data/item_recipes.csv").get
      .transformationMatrix

    matrix(0,0) shouldBe 0 //BMAT requires 0 BMAT to be made
    matrix(0,12) shouldBe 200 //Cometa requires 200 BMAT to be made

  }

}
