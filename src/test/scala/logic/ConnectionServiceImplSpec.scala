package test.logic

import test.BaseSpec
import logic.ConnectionServiceImpl
import zio.ZIO
import logic.ConnectionService
import util.Implicits._
import model.model.UserId
import dao.ConnectionsDaoInMemory

class ConnectionServiceImplSpec extends BaseSpec {

  "ConnectionServiceImpl" should "add connectionId and get it after" in {
    val connectionService = makeConnectionService

    val (idNone, idSome, idNone2) = (for {
      idNone  <- connectionService.findConnection(UserId(1))
      idSome  <- connectionService.findConnection(UserId(2))
      idNone2 <- connectionService.findConnection(UserId(3))
    } yield ((idNone, idSome, idNone2))).run

    idNone should be(None)
    idNone2 should be(None)
    idSome should be(Some(UserId(1)))
  }

  "ConnectionServiceImpl" should "not get user to talk with himself" in {
    val connectionService = makeConnectionService

    a[ConnectionService.DuplicateError.type] should be thrownBy {
      (for {
        idNone  <- connectionService.findConnection(UserId(1))
        idNone2 <- connectionService.findConnection(UserId(1))
      } yield ((idNone, idNone2))).run
    }
  }

  implicit private val runtime = zio.Runtime.default
  private def makeConnectionService =
    ZIO.service[ConnectionService].provide(ConnectionsDaoInMemory.layer >>> ConnectionServiceImpl.layer).run
}
