package service.build

import akka.actor._
import model.Project
import service.build.MainBuilder.ProjectBuilderFactory

object MainBuilder {
    val name = "mainBuilder"

    type ProjectBuilderFactory = (String, ActorRefFactory) => ProjectBuilder
    val factory: ProjectBuilderFactory = (name: String, context: ActorRefFactory) => new ProjectBuilder(context)
}

class MainBuilder(factory: ProjectBuilderFactory = MainBuilder.factory) extends Actor with ActorLogging {
    def receive = {
        case LaunchBuild(project) => factory(project.name, context).ref ! LaunchBuild(project)
    }

    /*
    * TODO :
    *   en deça d'une limite établie par config,
    *       créé un ProjectBuilder actor pour builder un projet dès qu'il reçoit un message LaunchBuild
    *
    *   si limite est dépassée
    *       mettre les builds en attente
    * */
}

sealed case class LaunchBuild(project: Project)