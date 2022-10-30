package controllers

import javax.inject.Inject
import models.{ClubRepo}
import play.api.mvc._

import scala.concurrent.ExecutionContext

class Application @Inject()(implicit ec: ExecutionContext, clubRepo: ClubRepo, val controllerComponents: ControllerComponents)
                        extends BaseController {

  def listClubs = Action.async { implicit rs =>
    clubRepo.all
  }

  def createClub() = Action { implicit rs =>
    clubRepo.createClub(rs.body)
  }
}
