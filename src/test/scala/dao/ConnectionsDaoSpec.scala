package dao

import test.BaseSpec
import ConnectionsDaoInMemory.layer
import ConnectionsDao._
import zio._
import util.Implicits._
import model.model.ConnectionId

class ConnectionsDaoSpec extends BaseSpec {
  "ConnectionsDao" should "add and get pairs" in {
    val connectionsDao = makeConnectionsDao
    val id2 = (for {
      _  <- connectionsDao.setConnectionIdPair(ConnectionId(1), ConnectionId(2))
      id <- connectionsDao.getConnectionId(ConnectionId(1))
    } yield id).run

    id2 should be(ConnectionId(2))
  }

  "ConnectionsDao" should "get error when changing pair" in {
    val connectionsDao = makeConnectionsDao

    a[ConnectionIdShouldBeRemovedFirst] should be thrownBy {
      (for {
        _  <- connectionsDao.setConnectionIdPair(ConnectionId(1), ConnectionId(2))
        id <- connectionsDao.setConnectionIdPair(ConnectionId(1), ConnectionId(3))
      } yield id).run
    }
  }

  "ConnectionsDao" should "get error when can't find connectionId" in {
    val connectionsDao = makeConnectionsDao

    a[NotFoundConnectionId.type] should be thrownBy {
      connectionsDao.getConnectionId(ConnectionId(1)).run
    }
  }

  "ConnectionsDao" should "remove added pair" in {
    val connectionsDao = makeConnectionsDao

    a[NotFoundConnectionId.type] should be thrownBy {
      (for {
        _  <- connectionsDao.setConnectionIdPair(ConnectionId(1), ConnectionId(2))
        _  <- connectionsDao.removeConnectionIdPair(ConnectionId(2))
        id <- connectionsDao.getConnectionId(ConnectionId(1))
      } yield id).run
    }
  }

  "ConnectionsDao" should "get error in case of not removing pair" in {
    val connectionsDao = makeConnectionsDao

    a[NotFoundConnectionId.type] should be thrownBy {
      connectionsDao.removeConnectionIdPair(ConnectionId(2)).run
    }
  }

  private implicit val runtime = Runtime.default
  private def makeConnectionsDao =
    ZIO.service[ConnectionsDao].provideLayer(layer).run
}
