package logic

import model.model.UserId
import zio.UIO
import model.model.ConnectionId
import zio.stm.TQueue
import zio.stm.ZSTM
import zio._
import dao.ConnectionsDao
import dao.ConnectionsDao._

// Queue must be unbounded or it may cause an error on a lot of offering ids
// Actually, may be not, if the transaction is atomic
case class ConnectionServiceImpl(idQueue: TQueue[ConnectionId], connectionDao: ConnectionsDao)
    extends ConnectionService {

  override def findConnection(id: UserId): UIO[Option[UserId]] = {
    val connectionId1 = ConnectionId(id.value)

    val getIdIfAlreadyConnected = connectionDao.getConnectionId(connectionId1).option

    // Разматчиться по полученному из очереди id и проверить, что оно не равно id
    // Если равно, то вернуть None
    // Должен починиться тест
    val connectionId2Get = (for {
      idOpt           <- idQueue.peekOption
      connectionIdOpt <- getOrSetConnectionId(idOpt, connectionId1)
    } yield (connectionIdOpt)).commit

    for {
      alreadyConnectedTo <- getIdIfAlreadyConnected
      connectionId <- if (alreadyConnectedTo.isDefined) ZIO.succeed(alreadyConnectedTo)
                      else
                        for {
                          connectionId2 <- connectionId2Get
                          _ <- ZIO
                                 .when(connectionId2.isDefined)(
                                   connectionDao.setConnectionIdPair(connectionId1, connectionId2.get)
                                 )
                                 .catchAll { case err: ConnectionsDao.ConnectionsDaoError => ??? }
                        } yield connectionId2
    } yield (connectionId.map(id => UserId(id.value)))

  }

  private def getOrSetConnectionId(
    id: Option[ConnectionId],
    userId: ConnectionId
  ): ZSTM[Any, Nothing, Option[ConnectionId]] =
    id match {
      case Some(_) => idQueue.take.map(Some(_))
      case None    => idQueue.offer(userId).map(_ => None)
    }

}

object ConnectionServiceImpl {
  val layer: ZLayer[ConnectionsDao, Nothing, ConnectionService] = ZLayer.fromZIO {
    for {
      connectionsDao <- ZIO.service[ConnectionsDao]
      queue          <- TQueue.unbounded[ConnectionId].commit
    } yield (ConnectionServiceImpl(queue, connectionsDao))
  }
}
