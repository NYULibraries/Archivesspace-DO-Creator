package edu.nyu.libraries.dlts.aspace

import java.io.{File, FileWriter}
import java.time.Instant

import org.json4s.DefaultFormats
import org.json4s.JsonAST.{JNull, JValue}

object Logger {

  private implicit val formats: DefaultFormats = DefaultFormats

  trait LoggingSupport {
    private val now: String = Instant.now().toString
    private val logFile = new File(s"DO-Creator-$now.tsv")
    private val logWriter = new FileWriter(logFile)

    logWriter.write("id\tstatus\turi\ttitle\twarnings\n")
    logWriter.flush()

    def closeLogger(): Unit = {
      logWriter.flush()
      logWriter.close()
    }

    def writeToLog(result: JValue): Unit = {
      val status = (result \ "status").extract[String]
      val id = (result \ "id").extract[String]
      val uri = (result \ "uri").extract[String]
      val title = (result \ "title").extract[String]
      val warnings = (result \ "warnings")
      warnings match {
        case JNull => logWriter.write(s"$id\t$status\t$uri\t$title\t\n")
        case _ => logWriter.write(s"$id\t$status\t$uri\t$title\t${warnings.extract[List[String]].mkString("; ")}\n")
      }
    }
  }

}
