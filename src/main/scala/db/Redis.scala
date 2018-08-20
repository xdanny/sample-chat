package db

import com.redis.RedisClient
import config.Configs.RedisConfig

object Redis {

  def initRedis(config: RedisConfig): RedisClient = {
    println("Starting redis connection")
    new RedisClient(config.host, config.port)
  }
}
