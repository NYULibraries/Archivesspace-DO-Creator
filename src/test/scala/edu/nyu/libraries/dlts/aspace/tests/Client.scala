package edu.nyu.libraries.dlts.aspace.tests

import com.typesafe.config.ConfigFactory
import org.scalatest._

import collection.mutable.Stack

class ClientSpec extends UnitSpec {
  val conf = ConfigFactory.load()
  println(conf.getInt("client.timeout"))
  "A configuration" should "exist" in {
    conf.isEmpty != true
  }

  it should "contain a positive timeout" in {
    conf.getInt("client.timeout") match {
      case x if x > 0 => true
      case _ => false
    }
  }

}
