package com.github.majestic.autohermod.googlesheet

import com.github.majestic.autohermod.AutoHermodConfig
import com.github.majestic.autohermod.stockreading.model.ItemStock
import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.{GoogleAuthorizationCodeFlow, GoogleClientSecrets}
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.sheets.v4.model.{BatchUpdateValuesRequest, BatchUpdateValuesResponse, UpdateValuesResponse, ValueRange}
import com.google.api.services.sheets.v4.{Sheets, SheetsScopes}

import java.io.{File, FileNotFoundException, FileReader}
import java.util.Collections
import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

case class SheetHandler(service: Sheets) {

  def writeStocks(stocksToExport: List[ItemStock]): Try[String] = {

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
      .update(SheetHandler.spreadsheetId,SheetHandler.rangeItemValues,data)
      .setValueInputOption(SheetHandler.INPUT_VALUE_OPTION)

    Try {
      response
        .execute()
        .getUpdatedRange

    }

  }

  def requestItemList(): Try[List[String]] = {
    Try {
      service.spreadsheets.values.get(SheetHandler.spreadsheetId, SheetHandler.rangeItemNames).execute
        .getValues
        .asScala.toList
        .map(_.get(0).asInstanceOf[String])
    }

  }
}

object SheetHandler {

  private val APPLICATION_NAME = "Google Sheets API Java Quickstart"
  private val JSON_FACTORY = GsonFactory.getDefaultInstance
  private val TOKENS_DIRECTORY_PATH = "tokens"

  private val SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS)
  private val CREDENTIALS_FILE_PATH = "src/main/resources/google_credentials.json"

  private val INPUT_VALUE_OPTION = "USER_ENTERED"

  val spreadsheetId = "1iE1aAd9YFnrUFCppdKk5aUebq1ziIHByjDxu-m3iRck"
  val rangeItemNames = "Stock1!A:A"
  val rangeItemValues = "Stock1!B:B"

  def apply(implicit config : AutoHermodConfig): SheetHandler = {
    val HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport
    val credentials = getCredentials(HTTP_TRANSPORT, config.googleCredentialsPath)
    val service: Sheets = new Sheets
    .Builder(HTTP_TRANSPORT, JSON_FACTORY, credentials)
      .setApplicationName(APPLICATION_NAME)
      .build

    SheetHandler(service)
  }

  private def getCredentials(HTTP_TRANSPORT: NetHttpTransport, credentialsPath : String): Credential = { // Load client secrets.
    val fileReader = new FileReader(credentialsPath)
    if (fileReader == null) throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH)
    val clientSecrets: GoogleClientSecrets = GoogleClientSecrets.load(JSON_FACTORY, fileReader)
    // Build flow and trigger user authorization request.
    val flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
      .setDataStoreFactory(new FileDataStoreFactory(new File(TOKENS_DIRECTORY_PATH)))
      .setAccessType("offline")
      .build
    val receiver = new LocalServerReceiver.Builder().setPort(8888).build
    new AuthorizationCodeInstalledApp(flow, receiver).authorize("AutoHermod")

  }

}
