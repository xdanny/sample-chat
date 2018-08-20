package services.pubsub
import akka.actor.{Actor, ActorSystem, Props}
import com.redis._

case class PublishMessage(channel: String, message: String)
case class SubscribeMessage(channels: List[String])
case class UnsubscribeMessage(channels: List[String])

class Pub(redisClient: RedisClient) extends Actor {
  println("starting publishing service ..")
  val system = ActorSystem("pub")
  val p = system.actorOf(Props(new Publisher(redisClient)))

  def receive = {
    case PublishMessage(ch, msg) => publish(ch, msg)

    case x => println("Got in Pub " + x)
  }

  def publish(channel: String, message: String) = {
    p ! Publish(channel, message)
  }
}


class Sub(redisClient: RedisClient) extends Actor {
  println("starting subscription service ..")
  val system = ActorSystem("sub")
  val s = system.actorOf(Props(new Subscriber(redisClient)))
  s ! Register(callback)

  def receive = {
    case SubscribeMessage(chs) => sub(chs)
    case UnsubscribeMessage(chs) => unsub(chs)

    case x => println("Got in Sub " + x)
  }

  def sub(channels: List[String]) = {
    s ! Subscribe(channels.toArray)
  }

  def unsub(channels: List[String]) = {
    s ! Unsubscribe(channels.toArray)
  }

  def callback(pubsub: PubSubMessage) = pubsub match {
    case E(exception) => println("Fatal error caused consumer dead. Please init new consumer reconnecting to master or connect to backup")
    case S(channel, no) => println("subscribed to " + channel + " and count = " + no)
    case U(channel, no) => println("unsubscribed from " + channel + " and count = " + no)
    case M(channel, msg) =>
      msg match {
        // exit will unsubscribe from all channels and stop subscription service
        case "exit" =>
          println("unsubscribe all ..")
          redisClient.unsubscribe

        // message "+x" will subscribe to channel x
        case x if x startsWith "+" =>
          val s: Seq[Char] = x
          s match {
            case Seq('+', rest @ _*) => redisClient.subscribe(rest.toString){ m => }
          }

        // message "-x" will unsubscribe from channel x
        case x if x startsWith "-" =>
          val s: Seq[Char] = x
          s match {
            case Seq('-', rest @ _*) => redisClient.unsubscribe(rest.toString)
          }

        // other message receive
        case x =>
          println("received message on channel " + channel + " as : " + x)
      }
  }
}

sealed trait Msg
case class Subscribe(channels: Array[String]) extends Msg
case class Register(callback: PubSubMessage => Any) extends Msg
case class Unsubscribe(channels: Array[String]) extends Msg
case object UnsubscribeAll extends Msg
case class Publish(channel: String, msg: String) extends Msg

class Subscriber(client: RedisClient) extends Actor {
  var callback: PubSubMessage => Any = { m => }

  def receive = {
    case Subscribe(channels) =>
      client.subscribe(channels.head, channels.tail: _*)(callback)
      true

    case Register(cb) =>
      callback = cb
      true

    case Unsubscribe(channels) =>
      client.unsubscribe(channels.head, channels.tail: _*)
      true

    case UnsubscribeAll =>
      client.unsubscribe
      true
  }
}

class Publisher(client: RedisClient) extends Actor {
  def receive = {
    case Publish(channel, message) =>
      client.publish(channel, message)
      true
  }
}