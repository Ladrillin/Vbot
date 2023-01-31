package test

import zio.ZIOAppDefault
import zio._
import com.typesafe.config.ConfigFactory

object Main extends ZIOAppDefault {

  val token = ZIO.attempt(ConfigFactory.parseResources("application.conf").getString("telegram_token"))
  def run = for {
    telegram_token <- token
    _              <- CommandsBot(telegram_token).run()
  } yield ()

}
