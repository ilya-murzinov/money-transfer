package murzinov.moneytransfer

import cats.data.OptionT
import doobie.imports._
import fs2.Task
import java.util.UUID

class MoneyTransferService(xa: Transactor[Task]) {
  def getAccount(id: UUID): Task[Option[Account]] =
    DB.getAccountQuery(id).transact(xa)

  def saveAccount(a: Account): Task[Account] =
    DB.saveAccountQuery(a).transact(xa)

  def transferMoney(fromId: UUID, toId: UUID, amount: Double): Task[Option[Transaction]] = {
    val action = for {
      from <- OptionT(DB.getAccountQuery(fromId))
      to <- OptionT(DB.getAccountQuery(toId))
      if from.amount > amount
      _ <- OptionT.liftF(DB.saveAccountQuery(from.copy(amount = from.amount - amount)))
      _ <- OptionT.liftF(DB.saveAccountQuery(to.copy(amount = to.amount + amount)))
      tr <- OptionT.liftF(DB.saveTransactionQuery(Transaction(fromId, toId, amount)))
    } yield tr
    action.value.transact(xa)
  }
}
