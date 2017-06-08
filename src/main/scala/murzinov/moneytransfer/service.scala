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

  def transferMoney(fromId: UUID, toId: UUID, amount: Double): Task[Either[TransferError, Transaction]] = {
    val action = for {
      from <- OptionT(DB.getAccountQuery(fromId)).toRight(new TransferError(new AccountNotFound(fromId).getMessage))
      to <- OptionT(DB.getAccountQuery(toId)).toRight(new TransferError(new AccountNotFound(toId).getMessage))
      _ <- if (from.amount > amount) EitherT.right(().pure[ConnectionIO])
           else EitherT.left(
             new TransferError(s"Account with id $fromId doesn't have enough money to transfer to account with id $toId").pure[ConnectionIO]
           )
      _ <- EitherT.liftT[ConnectionIO, TransferError, Account](DB.saveAccountQuery(from.copy(amount = from.amount - amount)))
      _ <- EitherT.liftT[ConnectionIO, TransferError, Account](DB.saveAccountQuery(to.copy(amount = to.amount + amount)))
      tr <- EitherT.liftT[ConnectionIO, TransferError, Transaction](DB.saveTransactionQuery(Transaction(fromId, toId, amount)))
    } yield tr
    action.value.transact(xa)
  }
}
