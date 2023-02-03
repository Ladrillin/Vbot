package vbot

import sttp.client3.httpclient.zio.HttpClientZioBackend
import com.bot4s.telegram.api.declarative.{Commands, RegexCommands}
import com.bot4s.telegram.cats.Polling
import com.bot4s.telegram.cats.TelegramBot
import com.bot4s.telegram.methods.SendMessage
import zio._
import zio.interop.catz._
import logic.ConnectionService
import util.Implicits._
import model.model.UserId
import model.model.ConnectionId
import dao.ConnectionsDao

case class CommandsBot(token: String, connectionService: ConnectionService, connectionDao: ConnectionsDao)
    extends TelegramBot[Task](token, HttpClientZioBackend().succes(Runtime.default))
    with Polling[Task]
    with Commands[Task]
    with RegexCommands[Task] {

  val commands = List("/start")

  onMessage { implicit msg =>
    val connectionId = ConnectionId(msg.chat.id)

    msg.text match {
      case Some(message) if commands.contains(message) => ZIO.unit
      case Some(message) =>
        connectionDao
          .getConnectionId(connectionId)
          .tapError(Console.printError(_))
          .flatMap { connectedUser =>
            request(SendMessage(connectedUser.value, message))
          }
          .ignore
      case None => ZIO.unit
    }
  }

  onCommand("/start") { implicit msg =>
    val userId = UserId(msg.chat.id)

    for {
      foundConnection <-
        connectionService.findConnection(userId).catchAll(_ =>
          ZIO.none)
      _ <- ZIO.when(foundConnection.isEmpty)(reply("Trying to find a person to speak with you"))
      _ <- ZIO.when(foundConnection.isDefined)(reply("Found a person to speak with you. Good luck!") *>
        request(SendMessage(foundConnection.get.value, "Found someone for you!")))
    } yield ()
  }
}
