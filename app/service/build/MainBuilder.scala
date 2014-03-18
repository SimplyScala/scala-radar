package service.build

import akka.actor._
import model.Project
import service.build.MainBuilder.ProjectBuilderFactory
import model.reactive.event.EventProducer
import dao.Db

object MainBuilder {
    val name = "mainBuilder"
    def props() = Props(new MainBuilder())

    type ProjectBuilderFactory = (String, sorm.Instance, ActorRefFactory) => ProjectBuilder
    // TODO delete name ? sert à quoi ?
    val factory: ProjectBuilderFactory = (name: String, db: sorm.Instance, context: ActorRefFactory) => new ProjectBuilder(context, db)
}

class MainBuilder(factory: ProjectBuilderFactory = MainBuilder.factory, db: sorm.Instance = new Db()) extends Actor with ActorLogging {

    def receive = {
        case LaunchBuild(project, eventProducer) =>
            factory(project.name, db, context).ref ! LaunchProjectBuild(project, eventProducer)
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

sealed case class LaunchBuild(project: Project, eventProducer: EventProducer[ProjectBuildEvent])