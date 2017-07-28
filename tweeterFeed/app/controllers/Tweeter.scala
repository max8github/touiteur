package controllers

import javax.inject.Inject

import akka.stream.scaladsl.{Source, _}
import java.util.Random
import play.api.libs.json._
import play.api.libs.ws.WSClient
import play.api.mvc._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class Tweeter @Inject()(wsClient: WSClient)(implicit ec: ExecutionContext) extends Controller {

  private val text = List("Tweet about", "Just heard about", "I love", "What the heck is this", "Again", "According to us, the best is")
  private val authors = List("Bob", "Joe", "John", "Sandy", "Gnarl", "Davis", "Anne", "Samantha", "Courtney")
  private val rand = new Random()

  /**
    * Fake twitter feed emitting tweets containing the given search keyword.
    * @param keyword
    * @return
    */
  def tweets(keyword: String) = Action {
    // The tick method creates a source emitting messages delta (=2s) seconds apart.
    // We can then transform this source using map in order to craft the message we want to generate ad hoc (with keyword).
    // We are limiting the feed to 100 tweets.
    val source = Source.tick(initialDelay = 0.second, interval = 2.second, tick = "fake tick").map { tick =>
      val randomText = s"${text(rand.nextInt(text.length))} $keyword"
      val author = authors(rand.nextInt(authors.length))
      Json.obj("message" -> randomText, "author" -> author).toString + "\n"
    }.limit(100)

    Ok.chunked(source)
  }
}
