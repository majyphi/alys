package com.github.majestic.autohermod.stockreading
import com.github.majestic.autohermod.stockreading.model.ItemStock
import org.scalatest.flatspec.AnyFlatSpec



class StockReadingTest extends AnyFlatSpec {

  "StockReader" should "correctly read simple stocks" in {

    val imgTestPath = "src/test/resources/stock_1920x1080_0.jpg"

    val stocksReader = StockReader()



    val parsingResult = stocksReader.extractStocksFromPath(imgTestPath)

    assert(parsingResult.forall(_.isSuccess))

    val itemStocks = parsingResult.map(_.get)

    assert(itemStocks.contains(ItemStock("soldier_supplies",0)))
    assert(itemStocks.contains(ItemStock("mortar_shells",43)))
    assert(itemStocks.contains(ItemStock("9mm",10)))
    assert(itemStocks.contains(ItemStock("bmat",27)))

  }

  "StockReader" should "correctly read complex stocks" in {

    val imgTestPath = "src/test/resources/stock_1920x1080_1.jpg"

    val stocksReader = StockReader()

    val parsingResult = stocksReader.extractStocksFromPath(imgTestPath)

    assert(parsingResult.forall(_.isSuccess))

    val itemStocks = parsingResult.map(_.get)

    assert(itemStocks.contains(ItemStock("soldier_supplies",57)))
    assert(itemStocks.contains(ItemStock("9mm",11)))
    assert(itemStocks.contains(ItemStock("bmat",154)))

  }

}

