package logic

import zio._
import model.model._
import ConnectionService._

trait ConnectionService {
  def findConnection(id: UserId): IO[ConnectionServiceError, Option[UserId]]
  def getConnectedUserId(id: UserId): IO[ConnectionServiceError, Option[UserId]]
  def stopConnection(id: UserId): IO[ConnectionServiceError, Unit]
}

object ConnectionService {
  sealed abstract class ConnectionServiceError(msg: String) extends RuntimeException(msg)

  case object DuplicateError   extends ConnectionServiceError("Duplicate in connection occured")
  case object AlreadyConnected extends ConnectionServiceError("User is already connected to another user")
}
