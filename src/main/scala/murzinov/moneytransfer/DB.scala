package murzinov.moneytransfer

import cats.data.OptionT
import doobie.imports._
import doobie.h2.imports._
import fs2.Task
import java.util.UUID

trait DB {
  def getAccount(id: UUID): Task[Option[Account]]
  def saveAccount(a: Account): Task[Account]
  def transferMoney(fromId: UUID, toId: UUID, amount: Double): Task[Option[Transaction]]
}

object DoobieDB {
  val createInMemory: Task[DoobieDB] = create("jdbc:h2:mem:test", "sa", "")
  val create: Task[DoobieDB] = create("jdbc:h2:./db/test", "sa", "")

  private[this] def create(
    connection: String,
    user: String,
    password: String
  ): Task[DoobieDB] =
    for {
      xa <- H2Transactor[Task](connection, user, password)
      _  <- xa.setMaxConnections(10)
      db  = new DoobieDB(xa)
      _  <- db.init
    } yield db
}

class DoobieDB(xa: Transactor[Task]) extends DB {
  val init: Task[Unit] = {
    val action = for {
      _ <- createAccountsTableQuery
      _ <- createTransactionsTableQuery
    } yield ()
    action.transact(xa)
  }

  def getAccount(id: UUID): Task[Option[Account]] =
    getAccountQuery(id).transact(xa)

  def saveAccount(a: Account): Task[Account] =
    saveAccountQuery(a).transact(xa)

  def transferMoney(fromId: UUID, toId: UUID, amount: Double): Task[Option[Transaction]] = {
    val action = for {
      from <- OptionT(getAccountQuery(fromId))
      to <- OptionT(getAccountQuery(toId))
      if from.amount > amount
      _ <- OptionT.liftF(saveAccountQuery(from.copy(amount = from.amount - amount)))
      _ <- OptionT.liftF(saveAccountQuery(to.copy(amount = to.amount + amount)))
      tr <- OptionT.liftF(saveTransactionQuery(Transaction(fromId, toId, amount)))
    } yield tr
    action.value.transact(xa)
  }

  lazy val createAccountsTableQuery: ConnectionIO[Int] =
    sql"""
      create table if not exists accounts(
        id uuid primary key,
        first_name varchar not null,
        last_name varchar not null,
        amount double not null
      );
    """.update.run

  lazy val createTransactionsTableQuery: ConnectionIO[Int] =
    sql"""
      create table if not exists transactions(
        id uuid primary key,
        from_account_id uuid not null,
        to_account_id uuid not null,
        amount double not null,
        date timestamp default CURRENT_TIMESTAMP,
        foreign key (from_account_id) references accounts(id),
        foreign key (to_account_id) references accounts(id)
      );
    """.update.run

  def getAccountQuery(id: UUID): ConnectionIO[Option[Account]] =
    sql"""
      select id, first_name, last_name, amount from accounts where id = $id
    """.query[Account].option

  def saveAccountQuery(a: Account): ConnectionIO[Account] =
    sql"""
      merge into accounts (id, first_name, last_name, amount) key (id)
      values (${a.id}, ${a.firstName}, ${a.lastName}, ${a.amount});
    """.update.run.map(_ => a)

  def saveTransactionQuery(tr: Transaction): ConnectionIO[Transaction] =
    sql"""
      merge into transactions (id, from_account_id, to_account_id, amount) key (id)
      values (RANDOM_UUID(), ${tr.fromId}, ${tr.toId}, ${tr.amount});
    """.update.run.map(_ => tr)
}
