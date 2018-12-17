name := "DO-Create-AV"

version := "0.1"

scalaVersion := "2.12.8"

libraryDependencies ++= Seq(
  "org.apache.httpcomponents" % "httpclient" % "4.5.2",
  "org.json4s" %% "json4s-native" % "3.6.0",
  "com.typesafe" % "config" % "1.3.2",
  "org.rogach" %% "scallop" % "3.1.3",
  "org.scalatest" % "scalatest_2.12" % "3.0.5" % "test"
)