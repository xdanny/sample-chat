package services

import cats.effect.{ConcurrentEffect, Effect, IO}
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import repository.ChatRepository
import io.circe.parser.decode
import models.{Chat, Message, UserRequest}
import cats.implicits._
import com.github.gvolpe.fs2redis.interpreter.connection.Fs2RedisClient
import com.github.gvolpe.fs2redis.interpreter.pubsub.Fs2PubSub
import com.github.gvolpe.fs2redis.model.{DefaultChannel, DefaultRedisCodec}
import com.redis.RedisClient
import fs2.Sink
import io.lettuce.core.RedisURI
import io.lettuce.core.codec.StringCodec
import org.http4s.server.websocket.WebSocketBuilder
import org.http4s.websocket.WebsocketBits.{Text, WebSocketFrame}

class UserService[F[_]: Effect](repo: ChatRepository[F], redisClient: RedisClient)(implicit F: ConcurrentEffect[F]) extends Http4sDsl[F]{

  import models.Encodings._

  private val redisURI    = RedisURI.create("redis://localhost")
  private val stringCodec = DefaultRedisCodec(StringCodec.UTF8)

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
          message => repo.insertMessage(chatId, message).flatMap{ insertedMessage =>
            redisClient.publish(chatId.toString, insertedMessage.name.toString)
            Created(insertedMessage)
          }
        )
      }

//    case request @ GET -> Root / "users" / IntVar(userId) / "chats" / IntVar(chatId) / "messages" / IntVar(messageId) =>

    case GET -> Root / "chats" / IntVar(chatId) / "ws" =>
      val echoReply: Sink[F, WebSocketFrame] = _.collect {
        case Text(msg, _) => println("You sent the server: " + msg)
        case _ => println("Something new")
      }

      val queue = for {
        client <- Fs2RedisClient.stream[F](redisURI)
        pubSub <- Fs2PubSub.mkPubSubConnection[F, String, String](client, stringCodec, redisURI)
        sub    <- pubSub.subscribe(DefaultChannel(chatId.toString)).map(s => Text(s))
      } yield sub

      WebSocketBuilder[F].build(queue, echoReply)
  }

  def sink(name: String): Sink[IO, String] = _.evalMap(x => IO(println(s"Subscriber: $name >> $x")))

}
