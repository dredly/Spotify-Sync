package configuration

object ApiVariables {
  val CLIENT_ID: String = sys.env("CLIENT_ID")
  val CLIENT_SECRET: String = sys.env("CLIENT_SECRET")
  val REDIRECT_URI: String = "http://localhost:9000/callback"
}
