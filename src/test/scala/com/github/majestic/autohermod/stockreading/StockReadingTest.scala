package com.github.majestic.autohermod.stockreading

import com.github.majestic.autohermod.AutoHermodConfig
import com.github.majestic.autohermod.imgloading.ImgLoader
import com.github.majestic.autohermod.model.ItemStock
import org.scalatest.flatspec.AnyFlatSpec

import scala.collection.immutable


class StockReadingTest extends AnyFlatSpec {

  val stocksReader = StockReader(AutoHermodConfig(
    "",
    "",
    "",
    "resources/images/digits/",
    "resources/images/icons/",
    "",
    ""
  ))


  "StockReader" should "correctly read simple stocks" in {

    val imgTestPath = "src/test/resources/stock_1920x1080_0.jpg"

    val parsingResult = stocksReader.extractStocksFromPath(imgTestPath)

    assert(parsingResult.forall(_.isSuccess))

    val itemStocks = parsingResult.map(_.get)

    assert(itemStocks.contains(ItemStock("soldier_supplies", 0)))
    assert(itemStocks.contains(ItemStock("mortar_shells", 43)))
    assert(itemStocks.contains(ItemStock("9mm", 10)))
    assert(itemStocks.contains(ItemStock("bmat", 27)))

  }

  "StockReader" should "correctly read stocks with confusing numbers" in {

    val imgTestPath = "src/test/resources/stock_1920x1080_1.jpg"

    val parsingResult = stocksReader.extractStocksFromPath(imgTestPath)

    assert(parsingResult.forall(_.isSuccess))

    val itemStocks = parsingResult.map(_.get)

    assert(itemStocks.contains(ItemStock("soldier_supplies", 57)))
    assert(itemStocks.contains(ItemStock("9mm", 11)))
    assert(itemStocks.contains(ItemStock("bmat", 154)))

  }

  "StockReader" should "correctly read numerous stocks" in {

    val imgTestPath = "src/test/resources/stock_1920x1080_2.jpg"

    val parsingResult = stocksReader.extractStocksFromPath(imgTestPath)

    assert(parsingResult.forall(_.isSuccess))

    val itemStocks = parsingResult.map(_.get)

    assert(itemStocks.contains(ItemStock("bayonet", 16)))
    assert(itemStocks.contains(ItemStock("daucus", 15)))
    assert(itemStocks.contains(ItemStock("bomastone", 47)))
    assert(itemStocks.contains(ItemStock("petrol", 36)))
    assert(itemStocks.contains(ItemStock("emat", 258)))
    assert(itemStocks.contains(ItemStock("ash_grenade", 31)))
    assert(itemStocks.contains(ItemStock("first_aid_kit", 38)))
    assert(itemStocks.contains(ItemStock("wrench", 21)))
    assert(itemStocks.contains(ItemStock("rmat", 15)))
    assert(itemStocks.contains(ItemStock("garrison_supplies", 6)))
    assert(itemStocks.contains(ItemStock("blood_plasma", 50)))
    assert(itemStocks.contains(ItemStock("diesel", 3)))
    assert(itemStocks.contains(ItemStock("7.62mm", 55)))
    assert(itemStocks.contains(ItemStock("gas_filter", 65)))
    assert(itemStocks.contains(ItemStock("soldier_supplies", 61)))
    assert(itemStocks.contains(ItemStock("bunker_supplies", 2)))
    assert(itemStocks.contains(ItemStock("mortar_shells", 30)))
    assert(itemStocks.contains(ItemStock("radio", 20)))
    assert(itemStocks.contains(ItemStock("120mm", 59)))
    assert(itemStocks.contains(ItemStock("catara", 34)))
    assert(itemStocks.contains(ItemStock("smoke_grenade", 26)))
    assert(itemStocks.contains(ItemStock("pitch_gun", 11)))
    assert(itemStocks.contains(ItemStock("0.44", 5)))
    assert(itemStocks.contains(ItemStock("binoculars", 28)))
    assert(itemStocks.contains(ItemStock("14.5mm", 35)))
    assert(itemStocks.contains(ItemStock("argenti", 16)))
    assert(itemStocks.contains(ItemStock("flatbed", 5)))
    assert(itemStocks.contains(ItemStock("9mm", 10)))
    assert(itemStocks.contains(ItemStock("bandages", 40)))
    assert(itemStocks.contains(ItemStock("shovel", 21)))
    assert(itemStocks.contains(ItemStock("7.92mm", 58)))
    assert(itemStocks.contains(ItemStock("12.7mm", 28)))
    assert(itemStocks.contains(ItemStock("sticky_bomb", 32)))
    assert(itemStocks.contains(ItemStock("sledge_hammer", 4)))
    assert(itemStocks.contains(ItemStock("trauma_kit", 37)))
    assert(itemStocks.contains(ItemStock("bmat", 276)))
    assert(itemStocks.contains(ItemStock("mine", 23)))
    assert(itemStocks.contains(ItemStock("30mm", 31)))
    assert(itemStocks.contains(ItemStock("gas_mask", 3)))
    assert(itemStocks.contains(ItemStock("68mm", 34)))
    assert(itemStocks.contains(ItemStock("tripod", 17)))
    assert(itemStocks.contains(ItemStock("mammon", 44)))
    assert(itemStocks.contains(ItemStock("cometa", 2)))
    assert(itemStocks.contains(ItemStock("shrapnell_shells", 41)))
    assert(itemStocks.contains(ItemStock("flare_shells", 27)))
    assert(itemStocks.contains(ItemStock("40mm", 55)))
    assert(itemStocks.contains(ItemStock("250mm", 20)))
    assert(itemStocks.contains(ItemStock("150mm", 11)))
    assert(itemStocks.contains(ItemStock("rpg_shell", 7)))
    assert(itemStocks.contains(ItemStock("ignifist", 22)))
    assert(itemStocks.contains(ItemStock("dusk", 33)))
    assert(itemStocks.contains(ItemStock("13.5mm", 15)))
    assert(itemStocks.contains(ItemStock("atrpg_shell", 17)))

  }

  "StockReader" should "load image from url" in {
    assert(ImgLoader().loadImageFromUrl("https://cdn.discordapp.com/attachments/836306356990246942/838521644795953182/20210502005918_1.jpg").isSuccess)
  }

}