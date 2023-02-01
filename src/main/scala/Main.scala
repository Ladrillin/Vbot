package test

import zio.ZIOAppDefault
import zio._
import com.typesafe.config.ConfigFactory
import logic.ConnectionService
import logic.ConnectionServiceImpl
import dao.ConnectionsDao
import dao.ConnectionsDaoInMemory

object Main extends ZIOAppDefault {

  val token = ZIO.attempt(ConfigFactory.parseResources("application.conf").getString("telegram_token"))
  def run = for {
    telegram_token <- token
    connectionService <-
      ZIO.service[ConnectionService].provideLayer(ConnectionsDaoInMemory.layer >>> ConnectionServiceImpl.layer)
    _ <- CommandsBot(telegram_token, connectionService).run()
  } yield ()

}
