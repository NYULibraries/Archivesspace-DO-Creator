package edu.nyu.libraries.dlts.aspace

import com.typesafe.config.{Config, ConfigFactory}
import java.io.File
import java.net.{URI, URL}
import java.time.Instant

import org.rogach.scallop.exceptions.{Help, RequiredOptionNotFound, ScallopException}
import org.rogach.scallop.{ScallopConf, ScallopOption}

import scala.io.StdIn

case class SessionInfo(env: String, username: String, password: String, repositoryId: Int, uri: URI, source: Option[String], drop: Option[Int], take: Option[Int])

object CLI {

  trait CLISupport {

    val now: String = Instant.now().toString

    private def help(optionName: String) {
      println(s"Error: Missing required option $optionName")
      help()
    }

    private def error(message: String) {
      println(message)
      println(help())
    }

    private def help(): Unit = {
      println("usage: java -jar DOCreator.jar [options]")
      println("  options:")
      println("    -s, --source, required\tpath to csv file to be input")
      println("    -d, --drop, optional\tnumber of rows to skip from the beginning of csv file")
      println("    -t, --take, optional\tnumber of rows to process from csv file")
      println("    -h, --help\tprint this message")
      System.exit(0)
    }

    private class CLIConf(arguments: Seq[String]) extends ScallopConf(arguments) {
      val source: ScallopOption[String] = opt[String](required = true)
      val drop: ScallopOption[Int] = opt[Int](required = false)
      val take: ScallopOption[Int] = opt[Int](required = false)
      verify()
    }

    def getSessionOptions(args: Array[String], conf: Config): SessionInfo = {

      val cli = new CLIConf(args) {
        override def onError(e: Throwable): Unit = e match {
          case Help("") => help()
          case ScallopException(message) => error(message)
          case RequiredOptionNotFound(optionName) => help(optionName)
        }
      }

      val repository = getFromConsole("Enter repository - fales, tamwag, archvies").toLowerCase.trim
      val repoId =  conf.getInt(s"repositories.${repository}")
      val env = getFromConsole("Enter environment - dev, stage, prod: ").toLowerCase
      val usr = getFromConsole("username")
      val pswd = getFromConsole("password")
      val uri = new URI(conf.getString(s"env.$env"))

      SessionInfo(env, usr, pswd, repoId, uri, cli.source.toOption, cli.drop.toOption, cli.take.toOption)
    }


    private def getFromConsole(str: String): String = {
      print(s"Enter the $str, or q to quit: ")
      val in = StdIn.readLine.trim
      in == "q" match {
        case true => System.exit(0) //quit the program
        case false =>
      }
      in
    }

  }
}
