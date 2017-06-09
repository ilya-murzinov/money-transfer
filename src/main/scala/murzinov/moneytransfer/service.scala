package murzinov.moneytransfer

import cats.data.{ EitherT, OptionT }
import doobie.imports._
import cats.data._
import cats.implicits._
import fs2.Task
import java.util.UUID

class MoneyTransferService(xa: Transactor[Task]) {
  def getAccount(id: UUID): Task[Option[Account]] =
    DB.getAccountQuery(id).transact(xa)

  def saveAccount(a: Account): Task[Account] =
    DB.saveAccountQuery(a).transact(xa)

  def transferMoney(tr: Transaction): Task[Either[ApiException, Transaction]] = {
    val action = for {
      _ <- if (tr.amount > 0) dummy
           else EitherT.left[ConnectionIO, TransferError, Account](
             new TransferError("Can't transfer non-positive amount of money").pure[ConnectionIO]
           )
      from <- OptionT(DB.getAccountQuery(tr.fromId)).toRight(new AccountNotFound(tr.fromId))
      to <- OptionT(DB.getAccountQuery(tr.toId)).toRight(new AccountNotFound(tr.toId))
      _ <- if (from.amount > tr.amount) dummy
           else EitherT.left[ConnectionIO, TransferError, Account](
             new TransferError(s"Account with id ${tr.fromId} doesn't have enough money to transfer to account with id ${tr.toId}").pure[ConnectionIO]
           )
      _ <- EitherT.liftT[ConnectionIO, ApiException, Account](DB.saveAccountQuery(from.copy(amount = from.amount - tr.amount)))
      _ <- EitherT.liftT[ConnectionIO, ApiException, Account](DB.saveAccountQuery(to.copy(amount = to.amount + tr.amount)))
      out <- EitherT.liftT[ConnectionIO, ApiException, Transaction](DB.saveTransactionQuery(Transaction(tr.fromId, tr.toId, tr.amount)))
    } yield out
    action.value.transact(xa)
  }

  private[this] val dummy = EitherT.right(().pure[ConnectionIO])
}
