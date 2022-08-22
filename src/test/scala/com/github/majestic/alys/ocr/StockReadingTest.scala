package com.github.majestic.alys.ocr

import com.github.majestic.alys.ImageProcessing
import com.github.majestic.alys.ocr.imageloading.ImgLoader
import com.github.majestic.alys.ocr.model.ImageItemStock
import com.github.majestic.alys.ocr.stockreading.StockReader
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers


class StockReadingTest extends AnyFlatSpec with Matchers {

  val stocksReader = StockReader(
    ImageProcessing(
      "resources/images/digits/",
      "resources/images/icons/"
    )
  )

  "StockReader" should "correctly read simple Warden stocks" in {

    val imgTestPath = "src/test/resources/1k/War-Win64-Shipping 2022-03-01 16-07-57.png"

    val parsingResult = stocksReader.extractStocksFromPath(imgTestPath)

    assert(parsingResult.forall(_.isSuccess))

    val result = parsingResult.map(_.get)

    val expected = List(
      ImageItemStock("soldier_supplies", 102),
      ImageItemStock("garrison_supplies", 30),
      ImageItemStock("bunker_supplies", 4),
      ImageItemStock("bmat", 586),
      ImageItemStock("diesel", 427),
      ImageItemStock("emat", 361),
      ImageItemStock("12.7mm", 66),
      ImageItemStock("120mm", 65),
      ImageItemStock("radio", 63),
      ImageItemStock("ash_grenade", 62),
      ImageItemStock("7.92mm", 58),
      ImageItemStock("binoculars", 55),
      ImageItemStock("gas_filter", 55),
      ImageItemStock("uni_snow", 52),
      ImageItemStock("0.44mm", 47),
      ImageItemStock("40mm", 47),
      ImageItemStock("loughcaster", 47),
      ImageItemStock("7.62mm", 43),
      ImageItemStock("mammon", 36),
      ImageItemStock("hangman", 34),
      ImageItemStock("hemat", 33),
      ImageItemStock("uni_medic", 31),
      ImageItemStock("tripod", 26),
      ImageItemStock("gas_mask", 26),
      ImageItemStock("bayonet", 25),
      ImageItemStock("wrench", 25),
      ImageItemStock("first_aid_kit", 25),
      ImageItemStock("mortar_shells", 24),
      ImageItemStock("uni_recon", 22),
      ImageItemStock("mine", 18),
      ImageItemStock("cometa", 17),
      ImageItemStock("bandages", 16),
      ImageItemStock("flare_shells", 14),
      ImageItemStock("blood_plasma", 13),
      ImageItemStock("shovel", 11),
      ImageItemStock("rmat", 11),
      ImageItemStock("cutler_foebreaker", 11),
      ImageItemStock("bonesaw_mounted", 10),
      ImageItemStock("uni_tank", 9),
      ImageItemStock("trauma_kit", 8),
      ImageItemStock("uni_ammo", 7),
      ImageItemStock("30mm", 5),
      ImageItemStock("listening", 5),
      ImageItemStock("150mm", 3),
      ImageItemStock("20mm", 3),
      ImageItemStock("uni_inge", 2),
      ImageItemStock("68mm", 1),
      ImageItemStock("radio_backpack", 1),
      ImageItemStock("dunne_loadlugger", 6),
      ImageItemStock("dunne_landrunner", 5),
      ImageItemStock("flatbed", 4),
      ImageItemStock("sledge_hammer", 3),
      ImageItemStock("ironship", 3),
      ImageItemStock("crane", 3),
      ImageItemStock("dunne_fuelrunner", 3),
      ImageItemStock("dunne", 1),
      ImageItemStock("kivela", 1),
      ImageItemStock("spire_crate", 1),
      ImageItemStock("gallant_crate", 1),
      ImageItemStock("resources_container", 32),
      ImageItemStock("shipping_container", 1)
    )

    val toSpare = result.filterNot(expected.contains)
    val missing = expected.filterNot(result.contains)

    toSpare shouldBe empty
    missing shouldBe empty


  }


  "StockReader" should "correctly read stocks with badly cropped screenshot" in {

    val imgTestPath = "src/test/resources/1k/War-Win64-Shipping 2022-03-01 19-20-49.png"

    val parsingResult = stocksReader.extractStocksFromPath(imgTestPath)

    assert(parsingResult.forall(_.isSuccess))

    val result = parsingResult.map(_.get)

    val expected = List(
      ImageItemStock("diesel", 124),
      ImageItemStock("12.7mm", 123),
      ImageItemStock("uni_snow", 107),
      ImageItemStock("fiddler", 80),
      ImageItemStock("0.44mm", 78),
      ImageItemStock("7.62mm", 76),
      ImageItemStock("7.92mm", 65),
      ImageItemStock("dunne_loadlugger", 63),
      ImageItemStock("120mm", 58),
      ImageItemStock("hemat", 48),
      ImageItemStock("mine", 48),
      ImageItemStock("40mm", 40),
      ImageItemStock("loughcaster", 39),
      ImageItemStock("flatbed", 39),
      ImageItemStock("wrench", 33),
      ImageItemStock("shipping_container", 32),
      ImageItemStock("uni_inge", 29),
      ImageItemStock("gas_filter", 26),
      ImageItemStock("clancy_cinder", 23),
      ImageItemStock("dunne_crate", 22),
      ImageItemStock("9mm", 21),
      ImageItemStock("ht_mk1", 20),
      ImageItemStock("neville", 20),
      ImageItemStock("ash_grenade", 19),
      ImageItemStock("uni_ammo", 18),
      ImageItemStock("shipping_container_crate", 17),
      ImageItemStock("tripod", 15),
      ImageItemStock("binoculars", 15),
      ImageItemStock("pallet_beam_crate", 14),
      ImageItemStock("sledge_hammer", 14),
      ImageItemStock("atrpgin_shell", 14),
      ImageItemStock("radio", 14),
      ImageItemStock("gas_mask", 13),
      ImageItemStock("small_container_crate", 13),
      ImageItemStock("bayonet", 13),
      ImageItemStock("bandages", 13),
      ImageItemStock("dunne_landrunner", 9),
      ImageItemStock("cutler_foebreaker", 9),
      ImageItemStock("mammon", 8),
      ImageItemStock("cometa", 8),
      ImageItemStock("shovel", 8),
      ImageItemStock("ironship", 8),
      ImageItemStock("68mm", 8),
      ImageItemStock("pallet_barbed_crate", 7),
      ImageItemStock("dunne_landrunner_crate", 7),
      ImageItemStock("pallet_sandbag_crate", 7),
      ImageItemStock("mortar_shells", 6),
      ImageItemStock("dunne_fuelrunner", 6),
      ImageItemStock("cement", 6),
      ImageItemStock("caine", 5),
      ImageItemStock("petrol", 4),
      ImageItemStock("frag_grenade", 4),
      ImageItemStock("20mm", 3),
      ImageItemStock("obrien_crate", 3),
      ImageItemStock("ht_mk1_crate", 3),
      ImageItemStock("hangman", 3),
      ImageItemStock("barge_crate", 3),
      ImageItemStock("pallet_beam", 3),
      ImageItemStock("gallant_crate", 3),
      ImageItemStock("flare_shells", 2),
      ImageItemStock("barge", 2),
      ImageItemStock("dunne_fuelrunner_crate", 2),
      ImageItemStock("mulloy_lpc", 2),
      ImageItemStock("radio_backpack", 2),
      ImageItemStock("rmat", 2),
      ImageItemStock("buckshot", 2),
      ImageItemStock("drummond_loscann", 2),
      ImageItemStock("pallet_barbed", 2),
      ImageItemStock("150mm", 1),
      ImageItemStock("dunne_loadlugger_crate", 1),
      ImageItemStock("dunne", 1),
      ImageItemStock("kivela_crate", 1),
      ImageItemStock("uni_tank", 1),
      ImageItemStock("pallet_sandbag", 1),
      ImageItemStock("freeman_crate", 1),
      ImageItemStock("drummond_loscann_crate", 1),
      ImageItemStock("listening", 1),
      ImageItemStock("small_container", 1),
      ImageItemStock("resources_container", 113),
      ImageItemStock("resources_container_crate", 7),
    )


    val toSpare = result.filterNot(expected.contains)
    val missing = expected.filterNot(result.contains)

    toSpare shouldBe empty
    missing shouldBe empty


  }

  "StockReader" should "correctly read stocks with confusing numbers" in {

    val imgTestPath = "src/test/resources/1k/test_300.png"

    val parsingResult = stocksReader.extractStocksFromPath(imgTestPath)

    assert(parsingResult.forall(_.isSuccess))

    val result = parsingResult.map(_.get)

    result should contain(ImageItemStock("emat", 300))

  }

  "StockReader" should "load image from url with JPEG extension even though is it actually a PNG file" in {
    assert(ImgLoader().loadImageFromUrl("https://cdn.discordapp.com/attachments/946056768625201202/948681280596357121/stock_1920x1080_0.jpg").isSuccess)
  }


}