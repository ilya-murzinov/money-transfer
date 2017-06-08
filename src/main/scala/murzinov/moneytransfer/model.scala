package murzinov.moneytransfer

import java.util.UUID

class ApiException(message: String) extends Exception(message)

case class Account(id: UUID, firstName: String, lastName: String, amount: Double)
case class AccountNotFound(id: UUID) extends ApiException(s"Account with id '$id' not found")

case class Transaction(fromId: UUID, toId: UUID, amount: Double)

case class TransferError(description: String) extends ApiException(description)
