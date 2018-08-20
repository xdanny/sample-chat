package models

import java.time.Instant

import cats.effect.Sync
import io.circe.{Decoder, Encoder}
import cats.syntax.either._
import org.http4s.{EntityDecoder, EntityEncoder}
import org.http4s.circe._

case class UserRequest(id: Option[Long], userName: String)

case class Chat(id: Option[Long], name: String, created: Option[Instant])

case class Message(id: Option[Long], name: String, created: Option[Instant], chatId: Option[Long])

object Encodings {

  implicit val encodeInstant: Encoder[Instant] = Encoder.encodeString.contramap[Instant](_.toString)

  implicit val decodeInstant: Decoder[Instant] = Decoder.decodeString.emap { str =>
    Either.catchNonFatal(Instant.parse(str)).leftMap(t => "Instant")
  }

  implicit val DecodeUserRequest: Decoder[UserRequest] =
    Decoder.forProduct2("id", "username")(UserRequest)

  implicit val EncodeUser: Encoder[UserRequest] =
    Encoder.forProduct2("id", "username")(e => (e.id, e.userName))

  implicit val DecodeChatRequest: Decoder[Chat] =
    Decoder.forProduct3("id", "name", "created")(Chat)

  implicit val EncodeChat: Encoder[Chat] =
    Encoder.forProduct3("id", "name", "created")(e => (e.id, e.name, e.created))

  implicit val DecodeMessageRequest: Decoder[Message] =
    Decoder.forProduct4("id", "name", "created", "chat_id")(Message)

  implicit val EncodeMessage: Encoder[Message] =
    Encoder.forProduct4("id", "name", "created", "chatId")(e => (e.id, e.name, e.created, e.chatId))

  implicit def userDecoder[F[_]: Sync]: EntityDecoder[F, UserRequest] = jsonOf[F, UserRequest]
  implicit def userEncoder[F[_]: Sync]: EntityEncoder[F, UserRequest] = jsonEncoderOf[F, UserRequest]

  implicit def chatDecoder[F[_]: Sync]: EntityDecoder[F, Chat] = jsonOf[F, Chat]
  implicit def chatEncoder[F[_]: Sync]: EntityEncoder[F, Chat] = jsonEncoderOf[F, Chat]

  implicit def messageDecoder[F[_]: Sync]: EntityDecoder[F, Message] = jsonOf[F, Message]
  implicit def messageEncoder[F[_]: Sync]: EntityEncoder[F, Message] = jsonEncoderOf[F, Message]
}