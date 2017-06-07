package murzinov.moneytransfer

import doobie.imports._
import doobie.h2.imports._
import fs2.Task
import java.util.UUID

trait DB {
  def getAccount(id: UUID): Task[Option[Account]]
}

object DoobieDB {
  val create: Task[DoobieDB] =
    for {
      t <- H2Transactor[Task]("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "sa", "")
      _  <- t.setMaxConnections(10)
      db  = new DoobieDB(t)
      _  <- db.init
    } yield db
}

class DoobieDB(t: Transactor[Task]) extends DB {
  val init: Task[Unit] = {
    val action = for {
      _ <- createTableQuery
      _ <- saveAccountQuery(Account(UUID.randomUUID, "asd", "asd", 42))
    } yield ()
    action.transact(t)
  }

  def getAccount(id: UUID): Task[Option[Account]] =
    getAccountQuery(id).transact(t)

  lazy val createTableQuery: ConnectionIO[Int] =
    sql"""
      create table accounts(
        id uuid primary key,
        first_name varchar not null,
        last_name varchar not null,
        amount double not null
      );
    """.update.run

  def getAccountQuery(id: UUID): ConnectionIO[Option[Account]] =
    sql"""
      select id, first_name, last_name, amount from accounts where id = $id
    """.query[Account].option

  def saveAccountQuery(a: Account): ConnectionIO[Account] =
  sql"""
    merge into accounts (id, first_name, last_name, amount) key (id)
    values (${a.id}, ${a.firstName}, ${a.lastName}, 0);
  """.update.run.map(_ => a)
}
