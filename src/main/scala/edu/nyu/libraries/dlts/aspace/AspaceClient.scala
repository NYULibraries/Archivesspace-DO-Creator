package edu.nyu.libraries.dlts.aspace

import java.net.{URI, URL}

import com.typesafe.config.ConfigFactory
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.{HttpGet, HttpPost}
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.{CloseableHttpClient, HttpClientBuilder}
import org.apache.http.util.EntityUtils
import org.json4s.native.JsonMethods.parse
import org.json4s.{DefaultFormats, JValue}

import scala.io.Source

case class WorkOrderRow (resourceId: String, refId: String,	uri: String, indicator1: String, 	indicator2: String, indicator3: String,	title: String, 	componentId: String)

object AspaceClient {

  trait AspaceSupport {

    implicit val formats: DefaultFormats = DefaultFormats

    val conf = ConfigFactory.load()
    val header = "X-ArchivesSpace-Session"
    val client = getClient(conf.getInt("client.timeout"))

    def getClient(timeout: Int): CloseableHttpClient = {
      val config = RequestConfig.custom()
        .setConnectTimeout(timeout * 1000)
        .setConnectionRequestTimeout(timeout * 1000)
        .setSocketTimeout(timeout * 1000).build()
      HttpClientBuilder.create().setDefaultRequestConfig(config).build()
    }

    def getServer(env: String): Option[JValue] = {
      try {
        val httpGet = new HttpGet(conf.getString(s"env.$env.uri"))
        val response = client.execute(httpGet)
        val entity = response.getEntity
        val content = entity.getContent
        val data = scala.io.Source.fromInputStream(content).mkString
        EntityUtils.consume(entity)
        response.close()
        Some(parse(data))
      } catch {
        case e: Exception =>  None
      }
    }

    def getToken(user: String, password: String, uri: URI): Option[String] = {
      try {
        val tokenRequest = new HttpPost(uri + s"/users/$user/login?password=$password")
        val response = client.execute(tokenRequest)
        response.getStatusLine.getStatusCode match {
          case 200 => {
            val entity =response.getEntity
            val content = entity.getContent
            val data = Source.fromInputStream(content).mkString
            val json = parse(data)
            val token = (json \ "session").extract[String]
            EntityUtils.consume(entity)
            response.close()
            Some(token)
          }
          case _ => None
        }
      } catch {
        case e: Exception => None
      }
    }

    def getAO(env: String, aspace_url: String, token: String): Option[JValue] = {
      try {
        val httpGet = new HttpGet(conf.getString(s"env.$env.uri") + aspace_url)
        httpGet.addHeader(header, token)
        val response = client.execute(httpGet)
        val entity = response.getEntity
        val content = entity.getContent
        val data = scala.io.Source.fromInputStream(content).mkString
        EntityUtils.consume(entity)
        response.close()
        Some(parse(data))
      } catch {
        case e: Exception =>  None
      }
    }

    def get(env: String, uri: String, token: String): Option[JValue] = {
      try {
        val httpGet = new HttpGet(conf.getString(s"env.$env.uri") + uri)
        httpGet.addHeader(header, token)
        val response = client.execute(httpGet)
        val entity = response.getEntity
        val content = entity.getContent
        val data = scala.io.Source.fromInputStream(content).mkString
        EntityUtils.consume(entity)
        response.close()
        Some(parse(data))
      } catch {
        case e: Exception =>  None
      }
    }

  }


}
