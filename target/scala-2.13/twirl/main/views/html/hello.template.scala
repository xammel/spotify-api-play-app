
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

object hello extends _root_.play.twirl.api.BaseScalaTemplate[play.twirl.api.HtmlFormat.Appendable,_root_.play.twirl.api.Format[play.twirl.api.HtmlFormat.Appendable]](play.twirl.api.HtmlFormat) with _root_.play.twirl.api.Template1[String,play.twirl.api.HtmlFormat.Appendable] {

  /**/
  def apply/*1.2*/(name: String):play.twirl.api.HtmlFormat.Appendable = {
    _display_ {
      {


Seq[Any](_display_(/*2.2*/main("Hello")/*2.15*/ {_display_(Seq[Any](format.raw/*2.17*/("""
"""),format.raw/*3.1*/("""<section id="content">
    <div class="wrapper doc">
        <article>
            <h1>Hello """),_display_(/*6.24*/name),format.raw/*6.28*/("""!</h1>
            <p>(pssst if you want to see you're name, replace 'Unknown' in the URL with your name...)</p>
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

  def render(name:String): play.twirl.api.HtmlFormat.Appendable = apply(name)

  def f:((String) => play.twirl.api.HtmlFormat.Appendable) = (name) => apply(name)

  def ref: this.type = this

}


              /*
                  -- GENERATED --
                  DATE: 2021-02-13T16:17:33.908261
                  SOURCE: /Users/maxmelhuish/Documents/Scala_projects/SpotifyApiProject/PlayFrameworkSpotifyApi/play-samples-play-scala-hello-world-tutorial/app/views/hello.scala.html
                  HASH: af048a2e8885cd5b245582bf32bcbd905d14a1d6
                  MATRIX: 729->1|837->17|858->30|897->32|924->33|1044->127|1068->131|1416->452|1452->467|1488->476
                  LINES: 21->1|26->2|26->2|26->2|27->3|30->6|30->6|37->13|37->13|38->14
                  -- GENERATED --
              */
          