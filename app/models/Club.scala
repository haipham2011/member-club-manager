package models

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Sink, Source}
import play.api
import play.api.{Logger}

import scala.concurrent.{Await, Future}
import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.Json
import play.api.mvc.{AnyContent}
import play.api.mvc.Results.{BadRequest, Ok}
import slick.jdbc
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

case class Club(id: Long, name: String, shortcut: String) {
  def patch(name: Option[String], shortcut: Option[String]): Club =
    this.copy(name = name.getOrElse(this.name),
      shortcut = shortcut.getOrElse(this.shortcut))
}

case class User(id: Long, name: String, clubId: Long) {
  def patch(name: Option[String], clubId: Option[Long]): User =
    this.copy(name = name.getOrElse(this.name),
      clubId = clubId.getOrElse(this.clubId))
}

case class Message(code: Int, content: String)

class ClubRepo @Inject()()(protected val dbConfigProvider: DatabaseConfigProvider) {
  implicit val system = ActorSystem("ClubRepo")
  val logger: Logger = Logger(this.getClass())

  val dbConfig = dbConfigProvider.get[JdbcProfile]
  val db = dbConfig.db

  import dbConfig.profile.api._

  private[models] val Clubs = TableQuery[ClubsTable]
  private[models] val Users = TableQuery[UsersTable]

  def findClubByName(name: String): Future[List[Club]] = {
    val query = Clubs.filter(_.name === name).to[List].result
    val source = Source.fromPublisher(db.stream(query))
    val result: Future[List[Club]] = source.runWith(Sink.collection)
    result
  }

  def allClubs: Future[List[Club]] = {
    val query = Clubs.to[List].result
    val source = Source.fromPublisher(db.stream(query))
    val result: Future[List[Club]] = source.runWith(Sink.collection)
    result
  }

  def allUsers: Future[List[User]] = {
    val query = Users.to[List].result
    val source = Source.fromPublisher(db.stream(query))
    val result: Future[List[User]] = source.runWith(Sink.collection)
    result
  }

  def all() = {
    for {
      users <- allUsers
      clubs <- allClubs
    } yield {
      Ok(Json.obj(
        "clubs" -> clubs.map(clubs => Json.obj(
          "id" -> clubs.id,
          "name" -> clubs.name,
          "shortcut" -> clubs.shortcut,
          "members" -> users.filter(user => user.clubId == clubs.id).map(user => Json.obj(
            "id" -> user.id,
            "name" -> user.name
          ))))
      ))
    }
  }

  private def createResponse(statusCode: Int, message: String) = Json.obj(
    "status" -> statusCode,
    "message" -> message
  )

  def createClub(requestBody: AnyContent) = {
    requestBody.asJson.map {
      json => {
        val clubName = (json \ "name").as[String].trim
        val members = (json \ "members").as[List[String]]

        if (clubName.isEmpty || clubName == null || members.size == 0 || members == null) {
          BadRequest(createResponse(403, "Club name is empty or there is no member in the club"))
        } else {
          val findClubFuture = (for {
            clubs <- findClubByName(clubName)
          } yield {
            if (clubs.size == 1) {
              BadRequest(createResponse(403, "Club name has already created"))
            } else {
              val words = clubName.split("\\s+")
              val shortcut = words.map(word => {
                val end = words.size match {
                  case 1 => 3
                  case 2 => 2
                  case _ => 1
                }
                word.toUpperCase.substring(0, end)
              }
              ).reduce((a, b) => a + b)

              val newClub = Club(0, clubName, shortcut)

              val action: DBIO[(Long, List[Long])] = (for {
                i <- Clubs returning Clubs.map(_.id) += newClub
                j <- DBIO.sequence(members.map(mem => Users returning Users.map(_.id) += User(0, mem, i)))
              } yield (i, j)).transactionally

              db.run(action)
              Ok(createResponse(200, "Save club successfully"))
            }
          })

          // Set max timeout time for finding Club with name
          Await.result(findClubFuture, 1 minutes)
        }
      }
    } getOrElse {
      BadRequest(createResponse(500, "Bad request"))
    }
  }

  private[models] class ClubsTable(tag: Tag) extends Table[Club](tag, "CLUBS") {

    def id = column[Long]("ID", O.AutoInc, O.PrimaryKey)

    def name = column[String]("NAME")

    def shortcut = column[String]("SHORTCUT")

    def * = (id, name, shortcut) <> (Club.tupled, Club.unapply)

    def ? = (id.?, name.?, shortcut.?).shaped.<>({ r => import r._; _1.map(_ => Club.tupled((_1.get, _2.get, _3.get))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))
  }

  private[models] class UsersTable(tag: Tag) extends Table[User](tag, "USERS") {

    def id = column[Long]("ID", O.AutoInc, O.PrimaryKey)

    def name = column[String]("NAME")

    def clubId = column[Long]("CLUB_ID")

    def * = (id, name, clubId) <> (User.tupled, User.unapply)

    def ? = (id.?, name.?, clubId.?).shaped.<>({ r => import r._; _1.map(_ => User.tupled((_1.get, _2.get, _3.get))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))
  }
}
