package model

package object model {
  case class ConnectionId(value: Long) extends AnyVal

  case class UserId(value: Long) extends AnyVal

  implicit class ConnectionIdOps(id: ConnectionId) {
    def toUserId = UserId(id.value)
  }

  implicit class UserIdOps(id: UserId) {
    def toConnectionId = ConnectionId(id.value)
  }
}
