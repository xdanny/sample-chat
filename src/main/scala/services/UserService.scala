package services

import cats.effect.{ConcurrentEffect, Effect}
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import repository.ChatRepository
import io.circe.parser.decode
import models.{Chat, Message, UserRequest}
import cats.implicits._

class UserService[F[_]: Effect](repo: ChatRepository[F]) extends Http4sDsl[F]{

  import models.Encodings._

  val service: HttpRoutes[F] = HttpRoutes.of[F] {
    case request @ POST -> Root / "users" =>
      request.decode[String]{ message =>
        decode[UserRequest](message).fold(
          error => BadRequest(),
          user => repo.insertUser(user).flatMap(Created(_))
        )
      }

//    case request @ GET -> Root / "users" =>


    case request @ POST -> Root / "users" / IntVar(userId) / "chats" =>
      request.decode[String]{ message =>
        decode[Chat](message).fold(
          error => BadRequest(),
          message => repo.insertChat(message).flatMap(Created(_))
        )
      }

//    case request @ GET -> Root / "users" / IntVar(userId) / "chats" =>

    case request @ POST -> Root / "users" / IntVar(userId) / "chats" / IntVar(chatId) / "messages" =>
      request.decode[String]{ message =>
        decode[Message](message).fold(
          error => BadRequest(),
          message => repo.insertMessage(chatId, message).flatMap(Created(_))
        )
      }

//    case request @ GET -> Root / "users" / IntVar(userId) / "chats" / IntVar(chatId) / "messages" / IntVar(messageId) =>

  }

}
