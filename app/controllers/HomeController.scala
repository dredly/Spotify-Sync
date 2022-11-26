package controllers

import configuration.ApiVariables.{CLIENT_ID, REDIRECT_URI}
import play.api.libs.ws._
import play.api.mvc._

import javax.inject._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(val controllerComponents: ControllerComponents, ws: WSClient) extends BaseController {

  val AUTHORIZE_URL = "https://accounts.spotify.com/authorize"
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
    // TODO: redirect to error page if code not present
    val authCode = request.getQueryString("code")
    authCode match {
      case Some(code) =>
        println(s"The code is $code")
        Ok(views.html.callback())
      case None =>
        println(s"No code was returned!")
        NotFound(views.html.callback())
    }
  }
}
