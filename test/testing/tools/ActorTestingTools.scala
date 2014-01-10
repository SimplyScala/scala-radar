package testing.tools

import concurrent.Await
import akka.pattern.gracefulStop
import akka.actor.ActorSystem

trait ActorTestingTools {
    def closeDummyActors(actorsName: String*)(implicit system: ActorSystem) {
        import scala.concurrent.duration._

        actorsName.map( actorName => gracefulStop(system.actorFor(system / actorName), 1 seconds) )
            .map( Await.result(_, 1 seconds) )
    }
}