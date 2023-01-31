package logic

import model.model.UserId
import zio.UIO
import model.model.ConnectionId
import zio.stm.TQueue
import zio.stm.ZSTM
import zio._

// Queue must be unbounded or it may cause an error on a lot of offering ids
// Actually, may be not, if the transaction is atomic
case class ConnectionServiceImpl(idQueue: TQueue[ConnectionId]) extends ConnectionService {

  override def findConnection(id: UserId): UIO[Option[UserId]] =
    (for {
      idOpt           <- idQueue.peekOption
      connectionIdOpt <- getOrSetConnectionId(idOpt, ConnectionId(id.value))
    } yield (connectionIdOpt)).map(_.map(id => UserId(id.value))).commit

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
  val layer: ULayer[ConnectionService] = ZLayer.fromZIO {
    (for {
      queue <- TQueue.unbounded[ConnectionId]
    } yield (ConnectionServiceImpl(queue))).commit
  }
}
