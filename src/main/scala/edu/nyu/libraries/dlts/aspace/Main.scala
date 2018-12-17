package edu.nyu.libraries.dlts.aspace

import java.net.URI

import edu.nyu.libraries.dlts.aspace.AspaceClient._
import edu.nyu.libraries.dlts.aspace.AspaceJson._

import org.json4s.native.JsonMethods._
import scala.io.Source

object Main extends App with AspaceSupport with JsonSupport {

  val token = getToken(conf.getString("env.dev.username"), conf.getString("env.dev.password"), new URI(conf.getString("env.dev.uri"))).get
  val env = "dev"

  println(pretty(render(createDigitalObject("https://aeon.library.nyu.edu/Logon?Action=10&Form=31&Value=http://dlib.nyu.edu/findingaids/ead/fales/darinka.xml&view=xml"))))


  /*
  Source.fromFile("darinka.txt").getLines().drop(1).take(1).foreach { i =>
    val cols = i.split("\t")
    val woRow = new WorkOrderRow(cols(0), cols(1), cols(2), cols(3), cols(4), cols(5), cols(6), cols(7))
    println(pretty(render(getAO("dev", woRow.uri, token).get)))
  }
  */



}
