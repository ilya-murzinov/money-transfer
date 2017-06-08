package murzinov.moneytransfer

import java.util.UUID

case class Account(id: UUID, firstName: String, lastName: String, amount: Double)
case class AccountNotFound(id: UUID) extends Exception(s"Account with id '$id' not found")

case class Transaction(fromId: UUID, toId: UUID, amount: Double)

case class TransferError(description: String) extends Exception(description)
