package murzinov.moneytransfer

import io.circe.generic.auto._
import io.finch._
import io.finch.circe._
import java.util.UUID

class MoneyTransferApi(mts: MoneyTransferService) {
  private[this] val createAcountEndpoint: Endpoint[Account] =
    put("api" :: "account" :: jsonBody[UUID => Account]) { (f: UUID => Account) =>
      mts.saveAccount(f(UUID.randomUUID)).toFuture.map(Ok(_))
    }

  private[this] val getAccountEndpoint: Endpoint[Account] =
    get("api" :: "account" :: path[UUID]) { (id: UUID) =>
      mts.getAccount(id).toFuture.map {
        case Some(a) => Ok(a)
        case None => NotFound(AccountNotFound(id))
      }
    }

  private[this] val transferMoneyEndpoint: Endpoint[Transaction] =
    post("api" :: "transaction" :: jsonBody[Transaction]) { (tr: Transaction) =>
      mts.transferMoney(tr).toFuture.map {
        case Right(t) => Ok(t)
        case Left(e) => e match {
          case a@AccountNotFound(m) => NotFound(a)
          case t@TransferError(m) => BadRequest(t)
        }
      }
    }

  private[this] val endpoints =
    getAccountEndpoint :+:
    createAcountEndpoint :+:
    transferMoneyEndpoint

  val service = endpoints.toService
}
