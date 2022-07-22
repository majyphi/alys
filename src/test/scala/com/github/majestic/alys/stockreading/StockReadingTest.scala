package com.github.majestic.alys.stockreading

import com.github.majestic.alys.{ALysConfig, DatabaseConfig, DiscordConfig, GoogleSheetsConfig, ImageProcessing, model}
import com.github.majestic.alys.imgloading.ImgLoader
import com.github.majestic.alys.model.ItemStock
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
      ItemStock("soldier_supplies", 102),
      ItemStock("garrison_supplies", 30),
      ItemStock("bunker_supplies", 4),
      ItemStock("bmat", 586),
      ItemStock("diesel", 427),
      ItemStock("emat", 361),
      ItemStock("12.7mm", 66),
      ItemStock("120mm", 65),
      ItemStock("radio", 63),
      ItemStock("ash_grenade", 62),
      ItemStock("7.92mm", 58),
      ItemStock("binoculars", 55),
      ItemStock("gas_filter", 55),
      ItemStock("uni_snow", 52),
      ItemStock("0.44mm", 47),
      ItemStock("40mm", 47),
      ItemStock("loughcaster", 47),
      ItemStock("7.62mm", 43),
      ItemStock("mammon", 36),
      ItemStock("hangman", 34),
      ItemStock("hemat", 33),
      ItemStock("uni_medic", 31),
      ItemStock("tripod", 26),
      ItemStock("gas_mask", 26),
      ItemStock("bayonet", 25),
      ItemStock("wrench", 25),
      ItemStock("first_aid_kit", 25),
      ItemStock("mortar_shells", 24),
      ItemStock("uni_recon", 22),
      ItemStock("mine", 18),
      ItemStock("cometa", 17),
      ItemStock("bandages", 16),
      ItemStock("flare_shells", 14),
      ItemStock("blood_plasma", 13),
      ItemStock("shovel", 11),
      ItemStock("rmat", 11),
      ItemStock("cutler_foebreaker", 11),
      ItemStock("bonesaw_mounted", 10),
      ItemStock("uni_tank", 9),
      ItemStock("trauma_kit", 8),
      ItemStock("uni_ammo", 7),
      ItemStock("30mm", 5),
      ItemStock("listening", 5),
      ItemStock("150mm", 3),
      ItemStock("20mm", 3),
      ItemStock("uni_inge", 2),
      ItemStock("68mm", 1),
      ItemStock("radio_backpack", 1),
      ItemStock("dunne_loadlugger", 6),
      ItemStock("dunne_landrunner", 5),
      ItemStock("flatbed", 4),
      ItemStock("sledge_hammer", 3),
      ItemStock("ironship", 3),
      ItemStock("crane", 3),
      ItemStock("dunne_fuelrunner", 3),
      ItemStock("dunne", 1),
      ItemStock("kivela", 1),
      ItemStock("spire_crate", 1),
      ItemStock("gallant_crate", 1),
      ItemStock("resources_container", 32),
      ItemStock("shipping_container", 1)
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
      ItemStock("diesel", 124),
      ItemStock("12.7mm", 123),
      ItemStock("uni_snow", 107),
      ItemStock("fiddler", 80),
      ItemStock("0.44mm", 78),
      ItemStock("7.62mm", 76),
      ItemStock("7.92mm", 65),
      ItemStock("dunne_loadlugger", 63),
      ItemStock("120mm", 58),
      ItemStock("hemat", 48),
      ItemStock("mine", 48),
      ItemStock("40mm", 40),
      ItemStock("loughcaster", 39),
      ItemStock("flatbed", 39),
      ItemStock("wrench", 33),
      ItemStock("shipping_container", 32),
      ItemStock("uni_inge", 29),
      ItemStock("gas_filter", 26),
      ItemStock("clancy_cinder", 23),
      ItemStock("dunne_crate", 22),
      ItemStock("9mm", 21),
      ItemStock("ht_mk1", 20),
      ItemStock("neville", 20),
      ItemStock("ash_grenade", 19),
      ItemStock("uni_ammo", 18),
      ItemStock("shipping_container_crate", 17),
      ItemStock("tripod", 15),
      ItemStock("binoculars", 15),
      ItemStock("pallet_beam_crate", 14),
      ItemStock("sledge_hammer", 14),
      ItemStock("atrpgin_shell", 14),
      ItemStock("radio", 14),
      ItemStock("gas_mask", 13),
      ItemStock("small_container_crate", 13),
      ItemStock("bayonet", 13),
      ItemStock("bandages", 13),
      ItemStock("dunne_landrunner", 9),
      ItemStock("cutler_foebreaker", 9),
      ItemStock("mammon", 8),
      ItemStock("cometa", 8),
      ItemStock("shovel", 8),
      ItemStock("ironship", 8),
      ItemStock("68mm", 8),
      ItemStock("pallet_barbed_crate", 7),
      ItemStock("dunne_landrunner_crate", 7),
      ItemStock("pallet_sandbag_crate", 7),
      ItemStock("mortar_shells", 6),
      ItemStock("dunne_fuelrunner", 6),
      ItemStock("cement", 6),
      ItemStock("caine", 5),
      ItemStock("petrol", 4),
      ItemStock("frag_grenade", 4),
      ItemStock("20mm", 3),
      ItemStock("obrien_crate", 3),
      ItemStock("ht_mk1_crate", 3),
      ItemStock("hangman", 3),
      ItemStock("barge_crate", 3),
      ItemStock("pallet_beam", 3),
      ItemStock("gallant_crate", 3),
      ItemStock("flare_shells", 2),
      ItemStock("barge", 2),
      ItemStock("dunne_fuelrunner_crate", 2),
      ItemStock("mulloy_lpc", 2),
      ItemStock("radio_backpack", 2),
      ItemStock("rmat", 2),
      ItemStock("buckshot", 2),
      ItemStock("drummond_loscann", 2),
      ItemStock("pallet_barbed", 2),
      ItemStock("150mm", 1),
      ItemStock("dunne_loadlugger_crate", 1),
      ItemStock("dunne", 1),
      ItemStock("kivela_crate", 1),
      ItemStock("uni_tank", 1),
      ItemStock("pallet_sandbag", 1),
      ItemStock("freeman_crate", 1),
      ItemStock("drummond_loscann_crate", 1),
      ItemStock("listening", 1),
      ItemStock("small_container", 1),
      ItemStock("resources_container", 113),
      ItemStock("resources_container_crate", 7),
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

    result should contain(ItemStock("emat", 300))

  }

  "StockReader" should "load image from url with JPEG extension even though is it actually a PNG file" in {
    assert(ImgLoader().loadImageFromUrl("https://cdn.discordapp.com/attachments/946056768625201202/948681280596357121/stock_1920x1080_0.jpg").isSuccess)
  }


}