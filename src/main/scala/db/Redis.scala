package db

import cats.effect.IO
import com.redis.RedisClient
import config.Configs.RedisConfig

object Redis {

  def initRedis(config: RedisConfig): IO[RedisClient] = IO {
    println("Starting redis connection")
    new RedisClient(config.host, config.port)
  }
}
