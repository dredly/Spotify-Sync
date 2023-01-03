package controllers

import com.google.common.io.BaseEncoding.base64Url
import configuration.ApiVariables.{API_BASE_URL, CLIENT_ID, CLIENT_SECRET, REDIRECT_URI}
import forms.PlaylistIdForm.PlaylistIdFormData
import models.MyPlaylistsResponse
import models.Readers.myPlaylistsResponseReads
import play.api.libs.json.{JsDefined, JsError, JsSuccess, JsUndefined}
import play.api.libs.ws._
import play.api.mvc._

import javax.inject._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class HomeController @Inject()(cc: ControllerComponents, ws: WSClient) extends AbstractController(cc) {

  val AUTHORIZE_URL = "https://accounts.spotify.com/authorize"
  val TOKEN_ENDPOINT = "https://accounts.spotify.com/api/token"
  def index(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
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

  def callback(): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
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

    val futureResponse = ws.url(TOKEN_ENDPOINT)
      .withHttpHeaders(headers.toSeq: _*)
      .post(body)

    futureResponse.map{
        r => r.json \ "access_token" match {
          case JsDefined(accessToken) =>
            println(s"accessToken = ${accessToken.toString()}")
            Ok(views.html.index()).withSession(request.session + ("accessToken" -> accessToken.toString().init.tail))
          case JsUndefined() => throw new Exception("Could not find desired JSON field")
        }
    }
  }

  def playlists(): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    request.session.get("accessToken")
      .map { token =>
        val futureResponse = ws.url(API_BASE_URL + "me/playlists")
          .withHttpHeaders("Authorization" -> s"Bearer $token", "Content-Type" -> "application/json")
          .get()
        // Just use onComplete for now to check things work as intended
        futureResponse.map{
          r => r.json.validate[MyPlaylistsResponse] match {
            case JsSuccess(myPlaylistsResponse, _) =>
              val playlists = myPlaylistsResponse.items
              Ok(views.html.playlists(playlists))
            case JsError(err) => throw new Exception(err.toString())
          }
        }
      }
      .getOrElse { Future(Unauthorized(views.html.errorPage()))}
  }

  def syncSelectForm(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.playlistIdForm(PlaylistIdFormData.form))
  }

  def syncSelect(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index())
  }
}
