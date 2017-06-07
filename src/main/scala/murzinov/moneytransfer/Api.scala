package murzinov.moneytransfer

import io.circe.generic.auto._
import io.finch._
import io.finch.circe._
import java.util.UUID

class Api(db: DB) {
  val createAcountEndpoint: Endpoint[Account] =
    put("api" :: "account" :: jsonBody[UUID => Account]) { (f: UUID => Account) =>
      db.saveAccount(f(UUID.randomUUID)).toFuture.map(Ok(_))
    }

  val getAccountEndpoint: Endpoint[Account] =
    get("api" :: "account" :: uuid) { (id: UUID) =>
      db.getAccount(id).toFuture.map {
        case Some(a) => Ok(a)
        case None => NotFound(AccountNotFound(id))
      }
    }

  val transferMoneyEndpoint: Endpoint[Transaction] =
    post("api" :: "transfer" :: jsonBody[Transaction]) { (tr: Transaction) =>
      db.transferMoney(tr.fromId, tr.toId, tr.amount).toFuture.map {
        case Some(t) => Ok(t)
        case None => NotFound(
          TransferError(
            tr.fromId,
            tr.toId,
            tr.amount,
            "Error occurred" //TODO: meaningful error
          )
        )
      }
    }

  val endpoints =
    getAccountEndpoint :+:
    createAcountEndpoint :+:
    transferMoneyEndpoint

  val service = endpoints.toService
}
