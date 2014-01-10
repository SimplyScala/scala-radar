package controllers

import play.api._
import play.api.mvc._

object ProjectBuildController extends Controller {

    def index = Action {
        Ok("build page")
    }

    def launch(projectId: String) = Action {

        /**
         * workflow de build
         *  lancer le workflow via 1 actor router (pour pouvoir lancer plusieurs build en même temps - 1 par défaut - Build Manager)  async
         *      launch metadata build
         *          when finished ping BuildManager ! OperationFinished
         *      launch scct build
         *          when finished ping BuildManager ! OperationFinished
         *      launch scala-style build
         *          when finished ping BuildManager ! OperationFinished
         *      quand les 3 sub-build sont finis alors indiquer à scala-radar où son les nouvaux rapports à récupérer (BDD ou/et filepath etc...)
         *      a chaque évènement de sub-build et à la fin mettre à jour la page de statut du build
         *
         *  afficher la page de statut du build mise à jour de façon asynchrone pour chaque sub-build
         */

        Ok(s"launch project build $projectId")
    }
}