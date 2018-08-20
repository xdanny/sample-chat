package config
import cats.effect.IO
import com.typesafe.config.ConfigFactory
import pureconfig.error.ConfigReaderException

object Configs {

  case class ServerConfig(host: String, port: Int)

  case class DatabaseConfig(driver: String, url: String, user: String, password: String)

  case class RedisConfig(host: String, port: Int)

  case class Config(server: ServerConfig, database: DatabaseConfig, redis: RedisConfig)

  object Config {
    import pureconfig._

    def load(configFile: String = "application.conf"): IO[Config] = {
      IO {
        loadConfig[Config](ConfigFactory.load(configFile))
      }.flatMap {
        case Left(e) => IO.raiseError[Config](new ConfigReaderException[Config](e))
        case Right(config) => IO.pure(config)
      }
    }
  }
}
