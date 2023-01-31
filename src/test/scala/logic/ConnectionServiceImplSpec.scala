package test.logic

import test.BaseSpec
import logic.ConnectionServiceImpl
import zio.ZIO
import logic.ConnectionService
import util.Implicits._
import model.model.UserId

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

  implicit private val runtime = zio.Runtime.default
  private def makeConnectionService =
    ZIO.service[ConnectionService].provide(ConnectionServiceImpl.layer).run
}
