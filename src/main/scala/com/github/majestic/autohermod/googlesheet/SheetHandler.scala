package com.github.majestic.autohermod.googlesheet

import com.github.majestic.autohermod.AutoHermodConfig
import com.github.majestic.autohermod.googlesheet.SheetHandler.{ArmesLegeresObjectivesRange, ArmesLourdesObjectivesRange, MaterialsObjectivesRange, SoinsObjectivesRange, SuppliesObjectivesRange, UtilitairesObjectivesRange, extractObjectives}
import com.github.majestic.autohermod.model.{FoundItemObjective, ItemObjective, ItemStock, NotFoundItemObjective, Objectives}
import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.{GoogleAuthorizationCodeFlow, GoogleClientSecrets}
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.sheets.v4.model.ValueRange
import com.google.api.services.sheets.v4.{Sheets, SheetsScopes}

import java.io.{File, FileNotFoundException, FileReader}
import java.util.Collections
import scala.collection.JavaConverters._
import scala.util.Try

case class SheetHandler(service: Sheets) {

  def writeStocks(stocksToExport: List[ItemStock], sheetName: String): Try[String] = {

    val datalist = stocksToExport
      .map(item => {
        List(item.quantity.asInstanceOf[AnyRef])
          .asJava
      })
      .asJava

    val data = new ValueRange()
      .setValues(datalist)

    val response = service
      .spreadsheets
      .values()
      .update(SheetHandler.spreadsheetId, SheetHandler.rangeItemValuesOf(sheetName), data)
      .setValueInputOption(SheetHandler.INPUT_VALUE_OPTION)

    Try {
      response
        .execute()
        .getUpdatedRange

    }
  }


  def requestItemList(sheetName: String): Try[List[String]] = {
    Try {
      service.spreadsheets.values.get(SheetHandler.spreadsheetId, SheetHandler.rangeItemNamesOf(sheetName)).execute
        .getValues
        .asScala.toList
        .map(_.get(0).asInstanceOf[String])
    }

  }

  def readItemsObjectives(): Try[Objectives] = {
    Try {

      val armesLegeres = extractObjectives(ArmesLegeresObjectivesRange)(service)
      val armesLourdes = extractObjectives(ArmesLourdesObjectivesRange)(service)
      val utilitaires = extractObjectives(UtilitairesObjectivesRange)(service)
      val soins = extractObjectives(SoinsObjectivesRange)(service)
      val supplies = extractObjectives(SuppliesObjectivesRange)(service)
      val materials = extractObjectives(MaterialsObjectivesRange)(service)

      Objectives(armesLegeres, armesLourdes, utilitaires, soins, supplies, materials)
    }
  }

}

object SheetHandler {

  private val APPLICATION_NAME = "Google Sheets API Java Quickstart"
  private val JSON_FACTORY = GsonFactory.getDefaultInstance
  private val TOKENS_DIRECTORY_PATH = "tokens"

  private val SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS)

  private val INPUT_VALUE_OPTION = "USER_ENTERED"

  val spreadsheetId = "1iE1aAd9YFnrUFCppdKk5aUebq1ziIHByjDxu-m3iRck"

  val ArmesLegeresObjectivesRange = "Objectifs!C13:F19"
  val ArmesLourdesObjectivesRange = "Objectifs!H13:K19"
  val UtilitairesObjectivesRange = "Objectifs!M13:P19"
  val SoinsObjectivesRange = "Objectifs!C26:F32"
  val SuppliesObjectivesRange = "Objectifs!H26:K32"
  val MaterialsObjectivesRange = "Objectifs!M26:P29"

  val acceptedValuesNonOfficer = List("Stock1", "Stock2")

  val acceptedValuesOfficer = acceptedValuesNonOfficer ++ List("Hermod", "debug")

  val rangeItemNamesOf: String => String = (sheetName: String) => s"$sheetName!A:A"
  val rangeItemValuesOf: String => String = (sheetName: String) => s"$sheetName!B:B"


  def apply(implicit config: AutoHermodConfig): SheetHandler = {
    val HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport
    val credentials = getCredentials(HTTP_TRANSPORT, config.googleCredentialsPath, config.googleTokenDirectory)
    val service: Sheets = new Sheets
    .Builder(HTTP_TRANSPORT, JSON_FACTORY, credentials)
      .setApplicationName(APPLICATION_NAME)
      .build

    SheetHandler(service)
  }

  private def getCredentials(HTTP_TRANSPORT: NetHttpTransport, credentialsPath: String, tokensDirectoryPath : String): Credential = { // Load client secrets.
    val fileReader = new FileReader(credentialsPath)
    if (fileReader == null) throw new FileNotFoundException("Resource not found: " + credentialsPath)
    val clientSecrets: GoogleClientSecrets = GoogleClientSecrets.load(JSON_FACTORY, fileReader)
    // Build flow and trigger user authorization request.

    val flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
      .setDataStoreFactory(new FileDataStoreFactory(new File(tokensDirectoryPath)))
      .setAccessType("offline")
      .build

    val receiver = new LocalServerReceiver.Builder().setPort(8888).build

    new AuthorizationCodeInstalledApp(flow, receiver).authorize("AutoHermod")
  }

  def extractObjectives(range: String)(service: Sheets): List[ItemObjective] = {
    service.spreadsheets.values.get(SheetHandler.spreadsheetId, range).execute
      .getValues
      .asScala.toList
      .map(list => {
        Try {
          FoundItemObjective(list.get(0).asInstanceOf[String],
            list.get(1).asInstanceOf[String],
            list.get(2).asInstanceOf[String],
            list.get(3).asInstanceOf[String])
        }
          .getOrElse(NotFoundItemObjective())
      })
  }

}
