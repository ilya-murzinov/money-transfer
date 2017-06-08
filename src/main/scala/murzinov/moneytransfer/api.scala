package murzinov.moneytransfer

import io.circe.generic.auto._
import io.finch._
import io.finch.circe._
import java.util.UUID

class MoneyTransferApi(mts: MoneyTransferService) {
  val createAcountEndpoint: Endpoint[Account] =
    put("api" :: "account" :: jsonBody[UUID => Account]) { (f: UUID => Account) =>
      mts.saveAccount(f(UUID.randomUUID)).toFuture.map(Ok(_))
    }

  val getAccountEndpoint: Endpoint[Account] =
    get("api" :: "account" :: uuid) { (id: UUID) =>
      mts.getAccount(id).toFuture.map {
        case Some(a) => Ok(a)
        case None => NotFound(AccountNotFound(id))
      }
    }

  val transferMoneyEndpoint: Endpoint[Transaction] =
    post("api" :: "transfer" :: jsonBody[Transaction]) { (tr: Transaction) =>
      mts.transferMoney(tr.fromId, tr.toId, tr.amount).toFuture.map {
        case Some(t) => Ok(t)
        case None => NotFound(
          TransferError(
            "Could not transfer money" //TODO: meaningful error
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
