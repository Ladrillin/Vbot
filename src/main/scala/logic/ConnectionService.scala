package logic

import zio._
import model.model._
import ConnectionService._

trait ConnectionService {
  def findConnection(id: UserId): IO[ConnectionServiceError, Option[UserId]]
}

object ConnectionService {
  sealed abstract class ConnectionServiceError(msg: String) extends RuntimeException(msg)

  case object DuplicateError extends ConnectionServiceError("Duplicate in connection occured")
}
