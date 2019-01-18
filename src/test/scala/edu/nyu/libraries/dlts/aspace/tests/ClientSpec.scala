package edu.nyu.libraries.dlts.aspace.tests

import java.net.URI

import com.typesafe.config.ConfigFactory
import edu.nyu.libraries.dlts.aspace.AspaceClient.AspaceSupport
import org.apache.http.impl.client.CloseableHttpClient
import org.scalatest._
import org.json4s.{DefaultFormats, JValue}

class ClientSpec extends UnitSpec with AspaceSupport {

  "A Client" should "an instance of a closeablehttpclient" in {
    client.isInstanceOf[CloseableHttpClient] should be(true)
  }

  it should "make a connection to archviesspace v2.5.1" in {
    val response = getServer(new URI(conf.getString("env.dev")))

    response match {
      case Some(i) => (i \ "archivesSpaceVersion").extract[String] should be ("v2.5.1")
      case None => fail()
    }
  }

  it should "login and get a session token" in {
    val token = getToken(conf.getString("test.username"), conf.getString("test.password"), new URI(conf.getString("env.dev")))
    token match {
      case Some(i) => i should not be empty
      case None => fail()
    }
  }

}
