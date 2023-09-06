
package views.html

import _root_.play.twirl.api.TwirlFeatureImports._
import _root_.play.twirl.api.TwirlHelperImports._
import _root_.play.twirl.api.Html
import _root_.play.twirl.api.JavaScript
import _root_.play.twirl.api.Txt
import _root_.play.twirl.api.Xml
import models._
import controllers._
import play.api.i18n._
import views.html._
import play.api.templates.PlayMagic._
import play.api.mvc._
import play.api.data._

object search extends _root_.play.twirl.api.BaseScalaTemplate[play.twirl.api.HtmlFormat.Appendable,_root_.play.twirl.api.Format[play.twirl.api.HtmlFormat.Appendable]](play.twirl.api.HtmlFormat) with _root_.play.twirl.api.Template1[String,play.twirl.api.HtmlFormat.Appendable] {

  /**/
  def apply/*1.2*/(futureResponse: String):play.twirl.api.HtmlFormat.Appendable = {
    _display_ {
      {


Seq[Any](_display_(/*2.2*/main("Hello")/*2.15*/ {_display_(Seq[Any](format.raw/*2.17*/("""
"""),format.raw/*3.1*/("""<section id="content">
    <div class="wrapper doc">
        <article>
            <h1>Hello</h1>
            <p>"""),_display_(/*7.17*/futureResponse),format.raw/*7.31*/("""</p>
            <p>
                <button onclick="location.href = '/';"> Now let's get out of this shit page shall we and go back home</button>
            </p>
        </article>
        <aside>
            """),_display_(/*13.14*/commonSidebar()),format.raw/*13.29*/("""
        """),format.raw/*14.9*/("""</aside>
    </div>
</section>
""")))}))
      }
    }
  }

  def render(futureResponse:String): play.twirl.api.HtmlFormat.Appendable = apply(futureResponse)

  def f:((String) => play.twirl.api.HtmlFormat.Appendable) = (futureResponse) => apply(futureResponse)

  def ref: this.type = this

}


              /*
                  -- GENERATED --
                  DATE: 2023-09-07T00:17:43.365048
                  SOURCE: /Users/maxmelhuish/repos/SpotifyApiProject/PlayFrameworkSpotifyApi/play-samples-play-scala-hello-world-tutorial/app/views/search.scala.html
                  HASH: 6976a038d532324c6322f0ce7405cab6f3d0ec71
                  MATRIX: 730->1|848->27|869->40|908->42|935->43|1075->157|1109->171|1349->384|1385->399|1421->408
                  LINES: 21->1|26->2|26->2|26->2|27->3|31->7|31->7|37->13|37->13|38->14
                  -- GENERATED --
              */
          