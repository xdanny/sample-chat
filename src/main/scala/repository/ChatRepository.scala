package repository


import java.time.Instant

import cats.effect.Effect
import doobie.util.transactor.Transactor
import doobie.implicits._
import cats.implicits._
import models._

class ChatRepository[F[_]: Effect](transactor: Transactor[F]) {

  def insertUser(userRequest: UserRequest): F[UserRequest] = {
    sql"""INSERT INTO users (username) VALUES (${userRequest.userName})"""
      .update.withUniqueGeneratedKeys[Long]("id").transact(transactor)
      .map(e => UserRequest(Some(e), userRequest.userName))
  }

  def insertChat(chatRequest: Chat): F[Chat] = {
    val nowInstant = Instant.now()
    sql"""INSERT INTO chats (name, created) VALUES (${chatRequest.name}, $nowInstant)"""
      .update.withUniqueGeneratedKeys[Long]("id").transact(transactor)
      .map(e => Chat(Some(e), chatRequest.name, Some(nowInstant)))
  }

  def insertMessage(chatId: Long, messageRequest: Message): F[Message] = {
    val nowInstant = Instant.now()
    sql"""INSERT INTO messages (name, created, chat_id) VALUES (${messageRequest.name}, $nowInstant, $chatId)"""
      .update.withUniqueGeneratedKeys[Long]("id").transact(transactor)
      .map(e => Message(Some(e), messageRequest.name, Some(nowInstant), Some(chatId)))
  }

}
