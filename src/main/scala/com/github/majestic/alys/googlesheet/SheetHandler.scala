package com.github.majestic.alys.googlesheet

import com.github.majestic.alys.ALysConfig
import com.github.majestic.alys.model.ItemStock
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
import scala.jdk.CollectionConverters._
import scala.util.Try

case class SheetHandler(service: Sheets, spreadsheetID : String) {

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
      .update(spreadsheetID, SheetHandler.rangeItemValuesOf(sheetName), data)
      .setValueInputOption(SheetHandler.INPUT_VALUE_OPTION)

    Try {
      response
        .execute()
        .getUpdatedRange

    }
  }


  def requestItemList(sheetName: String): Try[List[String]] = {
    Try {
      service.spreadsheets.values.get(spreadsheetID, SheetHandler.rangeItemNamesOf(sheetName)).execute
        .getValues
        .asScala.toList
        .map(_.get(0).asInstanceOf[String])
    }

  }

  def getURL() = "https://docs.google.com/spreadsheets/d/"+spreadsheetID

}

object SheetHandler {

  private val APPLICATION_NAME = "Google Sheets API Java Quickstart"
  private val JSON_FACTORY = GsonFactory.getDefaultInstance

  private val SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS)

  private val INPUT_VALUE_OPTION = "USER_ENTERED"

  val acceptedValuesNonOfficer = List("LYS1", "LYS2")

  val rangeItemNamesOf: String => String = (sheetName: String) => s"$sheetName!A:A"
  val rangeItemValuesOf: String => String = (sheetName: String) => s"$sheetName!B:B"

  def apply(implicit config: ALysConfig): SheetHandler = {
    val HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport
    val credentials = getCredentials(HTTP_TRANSPORT, config.googleCredentialsPath, config.googleTokenDirectory)
    val service: Sheets = new Sheets
    .Builder(HTTP_TRANSPORT, JSON_FACTORY, credentials)
      .setApplicationName(APPLICATION_NAME)
      .build

    SheetHandler(service, config.spreadsheetID)
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

    new AuthorizationCodeInstalledApp(flow, receiver).authorize("aLys")
  }


}