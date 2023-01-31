package dao

import zio._
import zio.concurrent.ConcurrentMap
import model.model.ConnectionId
import ConnectionsDao._

final case class ConnectionsDaoInMemory(data: ConcurrentMap[ConnectionId, ConnectionId]) extends ConnectionsDao {

  override def getConnectionId(connectionId: ConnectionId): IO[ConnectionsDaoError, ConnectionId] =
    for {
      optionalId <- data.collectFirst {
                      case pair @ (id1, id2) if id1 == connectionId || id2 == connectionId =>
                        pair match {
                          case (id1, id2) if id1 == connectionId => id2
                          case (id1, id2) if id2 == connectionId => id1
                          case (_, _)                            => throw new Throwable("not possible")
                        }
                    }

      id <- ZIO.fromOption(optionalId).mapError(_ => NotFoundConnectionId)
    } yield (id)

  def setConnectionIdPair(id1: ConnectionId, id2: ConnectionId): IO[ConnectionsDaoError, Unit] =
    for {
      id <- data.put(id1, id2)
      _  <- ZIO.when(id.isDefined)(ZIO.fail(ConnectionIdRemovedBySet))
    } yield ()

  def removeConnectionIdPair(connectionId: ConnectionId): IO[ConnectionsDaoError, Unit] =
    for {
      value <- data.remove(connectionId)
      _     <- ZIO.when(value.isEmpty)(ZIO.fail(NotFoundConnectionId))
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
