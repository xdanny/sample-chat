
import cats.effect.{IO, Timer}
import com.redis.RedisClient
import config.Configs.Config
import db.{Database, Redis}
import doobie.util.transactor.Transactor
import fs2.{Stream, StreamApp}
import fs2.StreamApp.ExitCode
import org.http4s.dsl.Http4sDsl
import org.http4s.server.blaze.BlazeBuilder
import repository.ChatRepository
import services.UserService

import scala.concurrent.ExecutionContext.Implicits.global

object ServerStream extends StreamApp[IO] with Http4sDsl[IO] {


  def services(transactor: Transactor[IO], redisClient: RedisClient) =
    new UserService[IO](new ChatRepository[IO](transactor), redisClient).service


  def stream(args: List[String], requestShutdown: IO[Unit]): Stream[IO, ExitCode]  = {
    implicit val timer = Timer.derive[IO]
    for {
      config <- Stream.eval(Config.load())
      transactor <- Stream.eval(Database.transactor(config.database))
      _ <- Stream.eval(Database.initialize(transactor))
      redisClient <- Stream.eval(Redis.initRedis(config.redis))
      exitCode <- BlazeBuilder[IO]
        .withWebSockets(true)
        .bindHttp(8080, "0.0.0.0")
        .mountService(services(transactor, redisClient), "/")
        .serve
    } yield exitCode
  }
}