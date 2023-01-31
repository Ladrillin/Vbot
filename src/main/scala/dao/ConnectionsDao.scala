package dao

import zio.IO
import model.model.ConnectionId

trait ConnectionsDao {
  import ConnectionsDao._

  def getConnectionId(connectionId: ConnectionId): IO[ConnectionsDaoError, ConnectionId]

  def setConnectionIdPair(id1: ConnectionId, id2: ConnectionId): IO[ConnectionsDaoError, Unit]

  def removeConnectionIdPair(connectionId: ConnectionId): IO[ConnectionsDaoError, Unit]
}

object ConnectionsDao {
  abstract sealed class ConnectionsDaoError(msg: String) extends RuntimeException(msg)

  case object NotFoundConnectionId     extends ConnectionsDaoError("Not found connection id")
  case object ConnectionIdRemovedBySet extends ConnectionsDaoError("Illegal removal of connection id")
}
