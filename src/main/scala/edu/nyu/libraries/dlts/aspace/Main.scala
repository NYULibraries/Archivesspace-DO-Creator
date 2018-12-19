package edu.nyu.libraries.dlts.aspace

import java.net.URI
import java.util.UUID

import edu.nyu.libraries.dlts.aspace.AspaceClient._
import edu.nyu.libraries.dlts.aspace.AspaceJson._
import org.json4s.JValue
import org.json4s.JsonAST.{JArray, JValue}
import org.json4s.native.JsonMethods._

import scala.io.Source

object Main extends App with AspaceSupport with JsonSupport {

  //set the environment
  val env = "dev"

  //get a token -- or exit
  val token = getToken(conf.getString("env.dev.username"), conf.getString("env.dev.password"), new URI(conf.getString("env.dev.uri"))).get

  val eadUri = "https://aeon.library.nyu.edu/Logon?Action=10&Form=31&Value=http://dlib.nyu.edu/findingaids/ead/fales/darinka.xml&view=xml"

  val repositoryId = 3

  //iterate through work order
  Source.fromFile("darinka.txt").getLines().drop(1).foreach { i =>
    val cols = i.split("\t")
    val woRow = new WorkOrderRow(cols(0), cols(1), cols(2), cols(3), cols(4), cols(5), cols(6), cols(7))
    val ao = getAO(env, token, woRow.uri).get
    val digitalObject = jsonDo(eadUri, woRow.title, "cuidTEST-" + UUID.randomUUID().toString)


    postDO(env, token, repositoryId, getCompact(digitalObject)) match {
      case Some(asResponse) => {
        asResponse.statusCode match {
          case 200 => {
            printPretty(asResponse.json)
            val doUri = (asResponse.json \ "uri").extract[String]
            val instances = (ao \ "instances").extract[List[JValue]]
            val doRef = getDORef(doUri)
            val updatedInstances = JArray(instances ++ List(doRef))
            val updatedAo = ao.mapField {
              case ("instances", JArray(x)) => ("instances", updatedInstances)
              case otherwise => otherwise
            }

            postAO(env, token, woRow.uri, getCompact(updatedAo)) match {
              case Some(aoResponse) => {
                aoResponse.statusCode match {
                  case 200 => printPretty(aoResponse.json)
                  case _ => {
                    println(aoResponse.statusCode)
                    printPretty(aoResponse.json)
                  }
                }
              }
              case None =>
            }

          }

          case _ => {
            println(asResponse.statusCode)
            printPretty(asResponse.json)
          }
        }
      }
      case None =>
    }

  }
}
