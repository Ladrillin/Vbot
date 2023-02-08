package dao

import zio._
import zio.concurrent.ConcurrentMap
import model.model.ConnectionId
import ConnectionsDao._

final case class ConnectionsDaoInMemory(data: ConcurrentMap[ConnectionId, ConnectionId]) extends ConnectionsDao {

  override def getConnectionId(connectionId: ConnectionId): IO[ConnectionsDaoError, ConnectionId] =
    for {
      optionalId <- data.get(connectionId)
      id         <- ZIO.fromOption(optionalId).mapError(_ => NotFoundConnectionId)
    } yield (id)

  def setConnectionIdPair(id1: ConnectionId, id2: ConnectionId): IO[ConnectionsDaoError, Unit] =
    for {
      potentialId  <- data.get(id1)
      potentialId2 <- data.get(id2)
      _            <- ZIO.when(potentialId.isDefined)(ZIO.fail(ConnectionIdShouldBeRemovedFirst(id1)))
      _            <- ZIO.when(potentialId2.isDefined)(ZIO.fail(ConnectionIdShouldBeRemovedFirst(id2)))
      _            <- data.put(id1, id2)
      _            <- data.put(id2, id1)
    } yield ()

  def removeConnectionIdPair(connectionId: ConnectionId): IO[ConnectionsDaoError, Unit] =
    for {
      id2 <- data.remove(connectionId)
      _   <- ZIO.when(id2.isEmpty)(ZIO.fail(NotFoundConnectionId))
      _   <- data.remove(id2.get)
    } yield ()
}

object ConnectionsDaoInMemory {
  val layer: ULayer[ConnectionsDao] = ZLayer.fromZIO {
    for {
      map    <- ConcurrentMap.empty[ConnectionId, ConnectionId]
      service = ConnectionsDaoInMemory(map)
    } yield (service)
  }

}
