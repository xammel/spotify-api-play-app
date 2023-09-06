
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

object index extends _root_.play.twirl.api.BaseScalaTemplate[play.twirl.api.HtmlFormat.Appendable,_root_.play.twirl.api.Format[play.twirl.api.HtmlFormat.Appendable]](play.twirl.api.HtmlFormat) with _root_.play.twirl.api.Template0[play.twirl.api.HtmlFormat.Appendable] {

  /**/
  def apply/*1.2*/():play.twirl.api.HtmlFormat.Appendable = {
    _display_ {
      {


Seq[Any](format.raw/*1.4*/("""

"""),_display_(/*3.2*/main("Welcome")/*3.17*/ {_display_(Seq[Any](format.raw/*3.19*/("""
"""),_display_(/*4.2*/defining(play.core.PlayVersion.current)/*4.41*/ { version =>_display_(Seq[Any](format.raw/*4.54*/("""

"""),format.raw/*6.1*/("""<section id="content">
  <div class="wrapper doc">
    <article>
      <h2>Welcome to the Hello World Tutorial!</h2>
      <p>
        If you'd like to see Max's shitty hello world test page you can see it here....
        <button onclick="location.href = '/hello?name=Unknown';">Come have a look!</button>
      </p>
      <p>This tutorial introduces Play Framework, describes how Play web applications work, and walks you through steps
        to create page that displays a Hello World greeting.</p>
      <p>If you loaded this page from the web server running on <code>localhost:9000</code>, congratulations! You have
        successfully built and run a Play application. If not, you likely opened the source <code>index.scala.html</code>
        file. Please follow the directions in the <code>README.md</code> file in the top-level project directory to run
        the tutorial.</p>

      <h3 id="introduction">Introduction to Play</h3>
      <p>As illustrated below, Play is a full-stack framework with all of the components you need to build a Web
        Application or a REST service, including:</p>
      <ul>
        <li>An integrated HTTP server</li>
        <li>Form handling</li>
        <li>Cross-Site Request Forgery (CSRF) protection</li>
        <li>A powerful routing mechanism</li>
        <li>I18n support, and more.</li>
      </ul>

      <img src="assets/images/play-stack.png" alt="Play Stack" class="small-5 medium-4 large-3" />

      <p>Play integrates with many object relational mapping (ORM) layers. It has out-of-the-box support for <a href="https://www.playframework.com/documentation/"""),_display_(/*34.164*/version),format.raw/*34.171*/("""/Anorm"
          target="_blank">Anorm</a>, <a href="https://www.playframework.com/documentation/"""),_display_(/*35.92*/version),format.raw/*35.99*/("""/JavaEbean" target="_blank">JavaEbean</a>,
        <a href="https://www.playframework.com/documentation/"""),_display_(/*36.63*/version),format.raw/*36.70*/("""/PlaySlick" target="_blank">PlaySlick</a>, and
        <a href="https://www.playframework.com/documentation/"""),_display_(/*37.63*/version),format.raw/*37.70*/("""/JavaJPA" target="_blank">JPA</a>. See <a href="https://www.playframework.com/documentation/"""),_display_(/*37.163*/version),format.raw/*37.170*/("""/JavaDatabase"
          target="_blank">Accessing an SQL Database</a> for more information. Many customers use NoSQL, other ORMs or
        even access data from a REST service.</p>


      <p>Play APIs are available in both Scala and Java. The Framework uses <a href="https://akka.io" target="_blank">Akka</a>
        and <a href="https://doc.akka.io/docs/akka-http/current/index.html" target="_blank">Akka HTTP</a> under the
        hood. This endows Play applications with a stateless, non-blocking, event-driven architecture that provides
        horizontal and vertical scalability and uses resources more efficiently.</p>

      <p>Here are just a few of the reasons developers love using Play Framework:</p>
      <ul>
        <li>Its Model-View-Controller (MVC) architecture is familiar and easy to learn.</li>
        <li>Direct support of common web development tasks and hot reloading saves precious development time.</li>
        <li>A large active community promotes knowledge sharing.</li>
        <li>Use of <a href="https://github.com/playframework/twirl">Twirl templates</a> to render pages. The Twirl
          template language is:
          <ol>
            <li>Easy to learn</li>
            <li>Requires no special editor</li>
            <li>Provides type safety</li>
            <li>Is compiled so that errors display in the browser</li>
          </ol>
        </li>
      </ul>

      <p>To learn more about Play's benefits, visit the <a href="https://www.playframework.com" target="_blank">Play
          website</a>.</p>

      <h3 id="next-steps">Next steps</h3>
      <p>Now, let's <a href=""""),_display_(/*67.31*/routes/*67.37*/.HomeController.explore()),format.raw/*67.62*/("""">explore the tutorial Play application</a>.</p>
    </article>
    <aside>
      """),_display_(/*70.8*/commonSidebar()),format.raw/*70.23*/("""
    """),format.raw/*71.5*/("""</aside>
  </div>
</section>
""")))}),format.raw/*74.2*/("""
""")))}))
      }
    }
  }

  def render(): play.twirl.api.HtmlFormat.Appendable = apply()

  def f:(() => play.twirl.api.HtmlFormat.Appendable) = () => apply()

  def ref: this.type = this

}


              /*
                  -- GENERATED --
                  DATE: 2023-09-06T22:09:45.725066
                  SOURCE: /Users/maxmelhuish/repos/SpotifyApiProject/PlayFrameworkSpotifyApi/play-samples-play-scala-hello-world-tutorial/app/views/index.scala.html
                  HASH: 8086b4e0df0dd6aa6fd2637ce42b128fbb75614b
                  MATRIX: 722->1|818->3|848->8|871->23|910->25|938->28|985->67|1035->80|1065->84|2743->1734|2772->1741|2899->1841|2927->1848|3060->1954|3088->1961|3225->2071|3253->2078|3374->2171|3403->2178|5083->3831|5098->3837|5144->3862|5256->3948|5292->3963|5325->3969|5388->4002
                  LINES: 21->1|26->1|28->3|28->3|28->3|29->4|29->4|29->4|31->6|59->34|59->34|60->35|60->35|61->36|61->36|62->37|62->37|62->37|62->37|92->67|92->67|92->67|95->70|95->70|96->71|99->74
                  -- GENERATED --
              */
          