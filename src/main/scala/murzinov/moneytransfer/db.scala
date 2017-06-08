package murzinov.moneytransfer

import doobie.imports._
import doobie.h2.imports._
import fs2.Task
import java.util.UUID

object DB {
  val createInMemory: Task[Transactor[Task]] = create("jdbc:h2:mem:test", "sa", "")
  val create: Task[Transactor[Task]] = create("jdbc:h2:./db/test", "sa", "")

  private[this] def create(
    connection: String,
    user: String,
    password: String
  ): Task[Transactor[Task]] =
    for {
      xa <- H2Transactor[Task](connection, user, password)
      _  <- xa.setMaxConnections(10)
      init = for {
        _ <- DB.createAccountsTableQuery
        _ <- DB.createTransactionsTableQuery
      } yield ()
      _ <- init.transact(xa)
    } yield xa

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
