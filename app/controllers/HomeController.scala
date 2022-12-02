package controllers

import com.google.common.io.BaseEncoding.base64Url
import configuration.ApiVariables.{CLIENT_ID, CLIENT_SECRET, REDIRECT_URI}
import play.api.libs.json.{JsDefined, JsUndefined}
import play.api.libs.ws._
import play.api.mvc._

import javax.inject._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}


@Singleton
class HomeController @Inject()(val controllerComponents: ControllerComponents, ws: WSClient) extends BaseController {

  val AUTHORIZE_URL = "https://accounts.spotify.com/authorize"
  val TOKEN_ENDPOINT = "https://accounts.spotify.com/api/token"
  def index(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    val params = Map(
      "response_type" -> "code",
      "client_id" -> CLIENT_ID,
      "redirect_uri" -> REDIRECT_URI,
      "state" -> "randomstuff"
    )

    val request: WSRequest = ws.url(AUTHORIZE_URL).withQueryStringParameters(params.toSeq:_*)
    val futureResult: Future[request.Response] = request.get()
    futureResult.onComplete(fr => {
      if (fr.isSuccess) {
        println(fr.get.body)
      }
    })
    Ok(views.html.index())
  }

  def about(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.about())
  }

  def login(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    val params = Map(
      "response_type" -> Seq("code"),
      "client_id" -> Seq(CLIENT_ID),
      "redirect_uri" -> Seq(REDIRECT_URI),
      "state" -> Seq("randomstuff")
    )
    Redirect(AUTHORIZE_URL, params)
  }

  def callback(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    val authCode = request.getQueryString("code")
    if (authCode.isEmpty) {
      throw new Exception("Missing query param 'code'")
    }
    val body = Map(
      "grant_type" -> "authorization_code",
      "code" -> authCode.get,
      "redirect_uri" -> REDIRECT_URI
    )

    val authString = "Basic " + base64Url.encode(s"$CLIENT_ID:$CLIENT_SECRET".getBytes)

    println(s"authString = $authString")

    val headers = Map(
      "Authorization" -> authString,
      "Content-Type" -> "application/x-www-form-urlencoded"
    )

    ws.url(TOKEN_ENDPOINT)
      .withHttpHeaders(headers.toSeq: _*)
      .post(body)
      .onComplete {
        case Success(r) =>
          r.json \ "access_token" match {
            // TODO: Improve error handling here to look for actual error in the JSON
            case JsDefined(accessToken) => println(s"accessToken = ${accessToken.toString()}")
            case JsUndefined() => throw new Exception("Could not find desired JSON field")
          }
        case Failure(exception) => throw exception
      }
    Ok(views.html.about())
  }
}
