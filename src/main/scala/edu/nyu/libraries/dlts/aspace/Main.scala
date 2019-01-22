package edu.nyu.libraries.dlts.aspace

import java.nio.file.{Files, Paths}

import org.json4s.JsonAST.{JArray, JString, JValue}

import scala.io.Source
import AspaceClient._
import AspaceJson._
import CLI._
import logging.Logger

object Main extends App with AspaceSupport with JsonSupport with CLISupport {
  println(s"* NYU Digital Object Creator ${conf.getString("app.version")} \n")

  //get the sessions options
  val sessionInfo = getSessionOptions(args, conf)

  //request a token
  val token = getToken(sessionInfo.username, sessionInfo.password, sessionInfo.uri).get

  //generate request url for collection
  val eadUri = s"https://aeon.library.nyu.edu/Logon?Action=10&Form=31&Value=http://dlib.nyu.edu/findingaids/ead/${sessionInfo.repositoryName}/${sessionInfo.findingAid}&view=xml" //this will need to be configurable somehow

  val lineCount = Files.lines(Paths.get(sessionInfo.tsv.getAbsolutePath)).count().toInt - 1
  val drop = sessionInfo.drop.getOrElse(0)
  val take = sessionInfo.take.getOrElse(lineCount - 1)

  val logger = new Logger()
  Source.fromFile(sessionInfo.tsv).getLines().drop(drop + 1).take(take).foreach { row =>
    val cols = row.split("\t").map(_.trim)
    val workOrderRow = new WorkOrderRow(cols(0), cols(1), cols(2), cols(3), cols(4), cols(5), cols(6), cols(7))
    println(s"* processing ${workOrderRow.refId} - ${workOrderRow.title}")
    processRow(workOrderRow)
  }

  close()

  println("\n* Exiting")

  System.exit(0)

  //request the AO from Archivesspace
  private def processRow(woRow: WorkOrderRow): Unit = {
    val archivalObject = getAO(sessionInfo.uri, token, woRow.uri)

    archivalObject match {

      case Some(ao) =>
        val title = (ao \ "title").extract[String]

        //create a new digital object
        val digitalObject = getCompact(jsonDo(eadUri, title, s"erecs-request-${woRow.refId}"))
        val postedDigital = postDO(sessionInfo.uri, token, sessionInfo.repositoryId, digitalObject)

        postedDigital match {
          case Some(dObj) => {
            dObj.statusCode match {
              case 200 => {
                val instances = getInstanceList(dObj, (ao \ "instances").extract[List[JValue]])
                val notes = removeAccessNote((ao \ "notes").extract[List[JValue]], sessionInfo.accessDeletion)
                val updatedAo = getCompact(updateAo(ao, instances, notes))
                val postedAo = postAO(sessionInfo.uri, token, woRow.uri, updatedAo)

                postedAo match {
                  case Some(aObj) => {
                    aObj.statusCode match {
                      case 200 => {
                        println(" ** SUCCESS")
                        logger.writeToLog(addToJArray(aObj.json, "title", title))
                      }
                      case _ => {
                        System.err.println(" ** AOpost did not return 200")
                        printPretty(aObj.json)
                      }
                    }
                  }
                  case None => System.err.println(" ** AOpost encountered an Error")
                }

              }
              case _ => {
                System.err.println(s" ** ERROR - DOpost returned ${dObj.statusCode}")
                var errors = List.empty[String]
                (dObj.json \ "error").children.foreach { i =>
                  errors = errors ++ List("digital_object " + i(0).extract[String])
                }

                logger.writeToLog(formatError("failed", woRow.refId, woRow.uri, title, errors))
              }
            }
          }
          case None => System.err.println(" ** DOpost encountered an error")
        }
      case None => System.err.println(s"** no ao found for ${woRow.uri} in ${sessionInfo.repositoryName} repository")
    }
  }

  private def getInstanceList(response: AspaceResponse, instances: List[JValue]): JArray = {
    val doUri = (response.json \ "uri").extract[String]
    val doRef = getDORef(doUri)
    JArray(instances ++ List(doRef))
  }

  private def removeAccessNote(notes: List[JValue], accessDelete: Boolean): JArray = {
    accessDelete match {
      case true => {
        var updatedNotes = List.empty[JValue]

        notes.foreach { note =>
          note.children.contains(JString("Conditions Governing Access")) match {
            case true => //do nothing
            case false => updatedNotes = updatedNotes ++ List(note)
          }
        }
        JArray(updatedNotes)
      }
      case false => JArray(notes)
    }
  }

  private def updateAo(ao: JValue, instances: JArray, notes: JArray): JValue = {
    ao.mapField {
      case ("instances", JArray(x)) => ("instances", instances)
      case ("notes", JArray(arr)) => ("notes", notes)
      case otherwise => otherwise
    }
  }

  private def close(): Unit = {
    logger.closeLogger()
  }

}

