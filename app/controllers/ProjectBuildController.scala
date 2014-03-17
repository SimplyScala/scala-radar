package controllers

import play.api._
import play.api.mvc._
import play.api.libs.iteratee.{Concurrent, Enumeratee}
import play.api.libs.concurrent.Akka
import play.api.libs.json.{Json, JsValue}
import play.api.libs.EventSource
import service.build._
import scalax.file.Path
import play.api.Play.current
import play.api.libs.EventSource.EventNameExtractor
import concurrent.ExecutionContext.Implicits.global
import model.reactive.event.EventProducer
import model.Project


object ProjectBuildController extends Controller {

    private val buildEventProducer = EventProducer(Concurrent.broadcast[ProjectBuildEvent])
    private val mainBuilder = Akka.system.actorOf(MainBuilder.props(), MainBuilder.name)

    def index = Action {
        Ok("build page")
    }

    def launch(projectName: String) = Action {

        /** TODO
         * workflow de build
         *  lancer le workflow via 1 actor router (pour pouvoir lancer plusieurs build en même temps - 1 par défaut - Build Manager)  async
         *      launch metadata build     KO    use this in controller
         *          when finished ping BuildManager ! OperationFinished
         *      launch scct build
         *          when finished ping BuildManager ! OperationFinished
         *      launch scala-style build
         *          when finished ping BuildManager ! OperationFinished
         *      quand les 3 sub-build sont finis alors indiquer à scala-radar où son les nouveaux rapports à récupérer (BDD ou/et filepath etc...)
         *      a chaque évènement de sub-build et à la fin mettre à jour la page de statut du build
         *
         *  afficher la page de statut du build mise à jour de façon asynchrone pour chaque sub-build
         */

        val project = Project(projectName, "git@github.com:SimplyScala/scala-radar.git", Path(""))
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