package test

import sttp.client3.httpclient.zio.HttpClientZioBackend
import org.asynchttpclient.Dsl.asyncHttpClient

import com.bot4s.telegram.models.Message
import com.bot4s.telegram.api.declarative._
import com.bot4s.telegram.api.declarative.CommandFilterMagnet._
import com.bot4s.telegram.api.declarative.{ Commands, RegexCommands }
import com.bot4s.telegram.cats.Polling
import com.bot4s.telegram.cats.TelegramBot

import zio._
import zio.interop.catz._

import util.Implicits._

case class CommandsBot(token: String)
    extends TelegramBot[Task](token, HttpClientZioBackend().succes(Runtime.default))
    with Polling[Task]
    with Commands[Task]
    with RegexCommands[Task] {

  def secretIsValid(msg: Message) =
    ZIO.succeed(msg.text.fold(false)(_.split(" ").last == "password"))

  whenF[Task, Message](onMessage, secretIsValid) { implicit msg =>
    reply(s"${msg.chat.id}").ignore
  }

  onCommand("/metro") { implicit msg =>
    reply(s"${msg.chat.id}").ignore
  }

  onCommand("beer" | "beers") { implicit msg =>
    reply("Beer menu bla bla...").ignore
  }

  // withArgs extracts command arguments.
  onCommand("echo") { implicit msg =>
    withArgs { args =>
      reply(args.mkString(" ")).catchErr.ignore
    }
  }

  // Handles only /respect2@recipient commands
  onCommand("respect" & respectRecipient(Some("recipient"))) { implicit msg =>
    reply("Respectful command").ignore
  }

  // Handles only /respect@<current-botname> commands
  onCommand("respect2" & RespectRecipient) { implicit msg =>
    reply("Respectful command #2").ignore
  }
}
