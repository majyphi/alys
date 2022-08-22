package com.github.majestic.alys.commands

import de.vandermeer.asciitable.AsciiTable

object Printer {

  def getTableString(data: Seq[Seq[String]], header: Seq[String]) = {
    val at = new AsciiTable
    at.addRule()
    at.addRow(header: _*)
    at.addRule()
    data.foreach(line => {
      at.addRow(line: _*)
    })
    at.addRule()
    s"""```${at.render(Math.min(header.length * 20, 56))}```"""
  }


}
