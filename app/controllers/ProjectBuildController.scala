package controllers

import play.api._
import play.api.mvc._
import play.api.libs.iteratee.{Concurrent, Enumeratee}
import play.api.libs.concurrent.Akka
import play.api.libs.json.{Json, JsValue}
import play.api.libs.EventSource
import service.build._
import play.api.Play.current
import play.api.libs.EventSource.EventNameExtractor
import concurrent.ExecutionContext.Implicits.global
import model.reactive.event.{ProjectBuildEvent, EventProducer}
import model.Project


object ProjectBuildController extends Controller {

    private val buildEventProducer = EventProducer(Concurrent.broadcast[ProjectBuildEvent])
    private val mainBuilder = Akka.system.actorOf(MainBuilder.props(), MainBuilder.name)

    def index = Action { Ok("build page") }

    def launch(projectName: String) = Action {
        // TODO request project from his name
        val project = Project(projectName, "git@github.com:SimplyScala/scala-radar.git")

        mainBuilder ! LaunchBuild(project, buildEventProducer)

        Ok(views.html.build(projectName))
    }

    def test = Action {      // TODO build id pour filtrer les events suivant quel build
        implicit val eventNameExtractor = EventNameExtractor[JsValue]( (event) => event.\("event").asOpt[String] )

        Ok.feed(buildEventProducer.enumerator &> asJson.compose(EventSource())).as("text/event-stream")
    }

    private def asJson: Enumeratee[ProjectBuildEvent, JsValue] = Enumeratee.map[ProjectBuildEvent] { buildEvent =>
        Json.toJson(Map("event" -> Json.toJson(buildEvent.eventName)))
    }

    // http://mandubian.com/2013/09/22/play-actor-room/
    // http://www.touilleur-express.fr/2012/08/05/realtime-web-application-un-exemple-avec-play2/
}