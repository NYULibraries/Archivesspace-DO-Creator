package edu.nyu.libraries.dlts.aspace

import com.typesafe.config.ConfigFactory

object Main extends App {
  val conf = ConfigFactory.load()
  println(conf.getInt("client.timeout"))
}
