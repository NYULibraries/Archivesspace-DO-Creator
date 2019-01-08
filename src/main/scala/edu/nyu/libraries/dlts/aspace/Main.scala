package edu.nyu.libraries.dlts.aspace

import java.io.File
import java.net.URI
import java.nio.file.{Files, Paths}
import java.util.UUID

import org.json4s.JsonAST.{JArray, JString, JValue}

import scala.io.{BufferedSource, Source}
import AspaceClient._
import AspaceJson._
import CLI._

object Main extends App with AspaceSupport with JsonSupport with CLISupport {

  //get the sessions options
  val sessionInfo = getSessionOptions(args, conf)

  //get a token -- or exit
  val token = getToken(sessionInfo.username, sessionInfo.password, sessionInfo.uri).get

  val eadUri = "https://aeon.library.nyu.edu/Logon?Action=10&Form=31&Value=http://dlib.nyu.edu/findingaids/ead/fales/darinka.xml&view=xml" //this will need to be configurable somehow

  val lineCount = Files.lines(Paths.get(sessionInfo.tsv.getAbsolutePath)).count().toInt - 1
  val drop = sessionInfo.drop.getOrElse(1)
  val take = sessionInfo.take.getOrElse(lineCount)


  Source.fromFile(sessionInfo.tsv).getLines().slice(drop, take + 1).foreach { row =>
    val cols = row.split("\t").map(_.trim)
    val workOrderRow = new WorkOrderRow(cols(0), cols(1), cols(2), cols(3), cols(4), cols(5), cols(6), cols(7))
    processRow(workOrderRow)
  }

  //request the AO from Archivesspace
  private def processRow(woRow: WorkOrderRow): Unit = {
    val archivalObject = getAO(sessionInfo.uri, token, woRow.uri)

    archivalObject match {

      case Some(ao) =>
        //create a new digital object
        val digitalObject = getCompact(jsonDo(eadUri, woRow.title, "cuidTEST-" + UUID.randomUUID().toString))
        val postedDigital = postDO(sessionInfo.uri, token, sessionInfo.repositoryId, digitalObject)

        postedDigital match {
          case Some(dObj) => {
            dObj.statusCode match {
              case 200 => {
                val instances = getInstanceList(dObj, (ao \ "instances").extract[List[JValue]])
                val notes = removeAccessNote((ao \ "notes").extract[List[JValue]])
                val updatedAo = getCompact(updateAo(ao, instances, notes))
                val postedAo = postAO(sessionInfo.uri, token, woRow.uri, updatedAo)

                postedAo match {
                  case Some(aObj) => {
                    aObj.statusCode match {
                      case 200 => printPretty(aObj.json)
                      case _ => //log the error
                    }
                  }
                  case None => //log the error
                }

              }
              case _ => //log the error
            }
          }
          case None => //log the error
        }
      case None => //log the error
    }
  }

  private def getInstanceList(response: AspaceResponse, instances: List[JValue]): JArray = {
    val doUri = (response.json \ "uri").extract[String]
    val doRef = getDORef(doUri)
    JArray(instances ++ List(doRef))
  }

  private def removeAccessNote(notes: List[JValue]): JArray = {
    var updatedNotes = List.empty[JValue]

    notes.foreach { note =>
      note.children.contains(JString("Conditions Governing Access")) match {
        case true => //do nothing
        case false => updatedNotes = updatedNotes ++ List(note)
      }
    }
    JArray(updatedNotes)
  }

  private def updateAo(ao: JValue, instances: JArray, notes: JArray): JValue = {
    ao.mapField {
      case ("instances", JArray(x)) => ("instances", instances)
      case ("notes", JArray(arr)) => ("notes", notes)
      case otherwise => otherwise
    }
  }

}

