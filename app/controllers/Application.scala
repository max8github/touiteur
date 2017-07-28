package controllers

import javax.inject.Inject

import akka.NotUsed
import akka.stream.scaladsl.{Source, _}
import akka.util._
import play.api.Configuration
import play.api.libs.EventSource
import play.api.libs.json._
import play.api.libs.ws.WSClient
import play.api.mvc._

import scala.concurrent.ExecutionContext


case class TweetInfo(searchQuery: String, message: String, author: String)

object TweetInfo {
  implicit val tweetInfoFormat = Json.format[TweetInfo]
}

class Application @Inject()(wsClient: WSClient, configuration: Configuration)(implicit ec: ExecutionContext) extends Controller {

  private val tweeterUrl = configuration.getString("tweeter.url").get

  def index = Action {
    //a default search
    Redirect(routes.Application.liveTouits(List("java", "ruby")))
  }

  def liveTouits(queries: List[String]) = Action {
    Ok(views.html.index(queries))
  }

  def mixedStream(queryString: String) = {

    def createSourceFromQuery(keyword: String): Source[JsValue, NotUsed] = {

      val request = wsClient.url(tweeterUrl).withQueryString("keyword" -> keyword)

      Source.fromFuture(request.stream()).flatMapConcat(streamedResponse => streamedResponse.body)
        // The Twitter service may send several messages in a single chunk, so we need to split them on line breaks.
        // It could send chunks with incomplete messages. In this case the messages need to be saved in a buffer until
        // we reach a line break. Fortunately, the Framing object does all the job for us. We just need to provide a
        // separator (line break) and a max frame length for the source elements.
        // The result of this operation is a new source that can be transformed into the new desired format.
        .via(Framing.delimiter(ByteString("\n"), maximumFrameLength = 100, allowTruncation = true))
        // The search query is in the response to ease filtering on the client side (TweetInfo case class).
        .map { byteString =>
          val json = Json.parse(byteString.utf8String)
          val tweetInfo = TweetInfo(keyword, (json \ "message").as[String], (json \ "author").as[String])
          Json.toJson(tweetInfo)
        }
    }

    Action {
      val keywordSources = Source(queryString.split(",").toList)
      val responses = keywordSources.flatMapMerge(10, createSourceFromQuery)
      // Play’s EventSource.flow method helps us format the messages into the Server Sent Events form... and the stream can flow
      Ok.chunked(responses via EventSource.flow)
    }
  }

}
