package edu.nyu.libraries.dlts.aspace

import org.json4s._
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods._

object AspaceJson {

  trait JsonSupport {

    def createDigitalObject(uri: String): JValue = {

      val fileVersions = List[JValue](
        ("jsonmodel_type" -> "file_version") ~
        ("is_representative" -> false) ~
        ("file_uri" -> uri) ~
        ("use_statement" -> "Video-Service") ~
        ("xlink_acctuate_attribute" -> "onLoad") ~
        ("xlink_show_attribute" -> "new") ~
        ("file_format_name" -> "mp4")

      )
      val digital_object =
        (
          ("jsonmodel_type" -> "digital_object") ~
          ("external_ids" -> List.empty[String]) ~
          ("linked_events" -> List.empty[String]) ~
          ("extents" -> List.empty[String]) ~
          ("dates" -> List.empty[String]) ~
          ("external_documents" -> List.empty[String]) ~
          ("rights_statements" -> List.empty[String]) ~
          ("linked_agents" -> List.empty[String]) ~
          ("file_versions" -> fileVersions)
        )

      digital_object
    }
  }

}



/*
{
	"jsonmodel_type": "digital_object",
	"external_ids": [],
	"subjects": [],
	"linked_events": [],
	"extents": [{
		"jsonmodel_type": "extent",
		"portion": "whole",
		"number": "16",
		"extent_type": "gigabytes",
		"dimensions": "FIC61U",
		"physical_details": "463995282485R"
	}],
	"dates": [],
	"external_documents": [],
	"rights_statements": [],
	"linked_agents": [],
	"file_versions": [{
		"jsonmodel_type": "file_version",
		"is_representative": false,
		"file_uri": "44X893459266",
		"use_statement": "application-pdf",
		"xlink_actuate_attribute": "none",
		"xlink_show_attribute": "new",
		"file_format_name": "tiff",
		"file_format_version": "LAQ758157",
		"file_size_bytes": 47,
		"checksum": "GA465466606",
		"checksum_method": "sha-384",
		"publish": true
	}],
	"restrictions": false,
	"notes": [],
	"linked_instances": [],
	"title": "Digital Object Title: 372",
	"language": "dzo",
	"digital_object_id": "WNJDI"
}
*/
