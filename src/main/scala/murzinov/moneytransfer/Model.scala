package murzinov.moneytransfer

import java.util.UUID

case class Account(id: UUID, firstName: String, lastName: String, amount: Double)

case class AccountNotFound(id: UUID) extends Exception(s"Account with id '$id' not found")
