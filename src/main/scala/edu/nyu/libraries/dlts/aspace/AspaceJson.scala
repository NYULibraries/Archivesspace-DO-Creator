package edu.nyu.libraries.dlts.aspace

import org.json4s._
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods._

object AspaceJson {

  trait JsonSupport {

    def printPretty(json: JValue) = {
      println(pretty(render(json)))
    }

    def printCompact(json: JValue) = {
      println(compact(render(json)))
    }

    def getCompact(json: JValue) = {
      compact(render(json))
    }

    def addToJArray(json: JValue, key: String, value: String): JValue = { json.asInstanceOf[JObject] ~ (key -> value) }

    def getDORef(uri: String): JValue = { ("instance_type" -> "digital_object") ~
        ("jsonmodel_type" ->"instance") ~
        ("is_representative" -> false) ~
        ("digital_object" ->
          ("ref" -> uri))
    }

    def jsonDo(uri: String, title: String, doId: String): JValue = {

      val fileVersions = List[JValue](
        ("jsonmodel_type" -> "file_version") ~
        ("is_representative" -> false) ~
        ("file_uri" -> uri) ~
        ("use_statement" -> "video-service") ~
        ("xlink_acctuate_attribute" -> "onLoad") ~
        ("xlink_show_attribute" -> "new") ~
        ("publish" -> true)
      )

      val digital_object = ("jsonmodel_type" -> "digital_object") ~
          ("external_ids" -> List.empty[String]) ~
          ("linked_events" -> List.empty[String]) ~
          ("extents" -> List.empty[String]) ~
          ("dates" -> List.empty[String]) ~
          ("external_documents" -> List.empty[String]) ~
          ("rights_statements" -> List.empty[String]) ~
          ("linked_agents" -> List.empty[String]) ~
          ("file_versions" -> fileVersions) ~
          ("restrcitions" -> false) ~
          ("notes" -> List.empty[String]) ~
          ("linked_instances" -> List.empty[String]) ~
          ("title" -> title) ~
          ("digital_object_id" -> doId)
      
      digital_object
    }
  }
}
