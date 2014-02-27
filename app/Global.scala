import play.api._
import play.api.libs.concurrent._
import play.api.Play.current

object Global extends GlobalSettings {

    override def onStart(app: Application) {
        //Akka.system.actorOf(ProjectBuildManager.props, ProjectBuildManager.name)
    }
}