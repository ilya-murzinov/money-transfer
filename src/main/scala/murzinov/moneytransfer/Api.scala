package murzinov.moneytransfer

import io.circe.generic.auto._
import io.finch._
import io.finch.circe._
import java.util.UUID

class Api(db: DB) {
  val getAccountEndpoint: Endpoint[Account] = get("api" :: "account" :: uuid) { (id: UUID) =>
    db.getAccount(id).toFuture.map {
      case Some(a) => Ok(a)
      case None => NotFound(AccountNotFound(id))
    }
  }

  val endpoints = getAccountEndpoint

  val service = endpoints.toService
}
