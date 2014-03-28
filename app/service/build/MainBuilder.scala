package service.build

import akka.actor._
import model.Project
import service.build.MainBuilder.ProjectBuilderFactory
import model.reactive.event.EventProducer

object MainBuilder {
    val name = "mainBuilder"
    def props() = Props(new MainBuilder())

    type ProjectBuilderFactory = (String, ActorRefFactory) => ProjectBuilder
    // TODO name du project à builder
    val factory: ProjectBuilderFactory = (name: String, context: ActorRefFactory) => new ProjectBuilder(context)
}

class MainBuilder(factory: ProjectBuilderFactory = MainBuilder.factory) extends Actor with ActorLogging {

    def receive = {
        case LaunchBuild(project, eventProducer) =>
            factory(project.name, context).ref ! LaunchProjectBuild(project, eventProducer)
    }

    /*
    * TODO :
    *   en deça d'une limite établie par config,
    *       créé un ProjectBuilder actor pour builder un projet dès qu'il reçoit un message LaunchBuild
    *
    *   si limite est dépassée
    *       mettre les builds en attente
    *
    *   si un build d'un projet donné est déjà en cours, mettre le prochain build en attente
    * */
}

sealed case class LaunchBuild(project: Project, eventProducer: EventProducer[ProjectBuildEvent])