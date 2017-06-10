package murzinov.moneytransfer

import com.twitter.finagle.Http
import com.twitter.util.Await
import featherbed._
import featherbed.circe._
import featherbed.request.ErrorResponse
import io.circe.generic.auto._
import java.net.URL
import java.util.UUID
import org.scalatest.{ FlatSpec, Matchers }
import shapeless.Coproduct

class MoneyTransferSpec extends FlatSpec with Matchers {
  val port = 8765
  val defaultAmount: Double = 100
  val accountNotFoundError = "\\{\"message\":\"Account with id .* not found\"}"

  behavior of "Money transfer API"

  it should "not get non-existent account" in {
    val server = startServer

    try {
      val error = intercept[Exception] {
        getAccount(UUID.randomUUID)
      }
      error.getMessage should fullyMatch regex accountNotFoundError
    }
    finally server.close()
  }

  it should "create accounts and transfer correct amount of money" in {
    val server = startServer

    try {
      createAccountsAndTransfer(10)
    }
    finally server.close()
  }

  it should "not transfer money from non-existent account" in {
    val server = startServer

    try {
      val error = intercept[Exception] {
        transfer(UUID.randomUUID, UUID.randomUUID, 10)
      }
      error.getMessage should fullyMatch regex accountNotFoundError
    }
    finally server.close()
  }

  it should "not transfer incorrect amount of money" in {
    val server = startServer

    try {
      val notEnoughMoney = intercept[Exception] {
        createAccountsAndTransfer(200)
      }
      notEnoughMoney.getMessage should fullyMatch regex
        "\\{\"message\":\"Account with id .* doesn't have enough money to transfer to account with id .*\"}"

      val negativeAmount = intercept[Exception] {
        createAccountsAndTransfer(-100)
      }
      negativeAmount.getMessage shouldBe "{\"message\":\"Can't transfer non-positive amount of money\"}"
    }
    finally server.close()
  }

  def startServer = {
    val xa = DB.createInMemory.unsafeRun()
    val service = new MoneyTransferService(xa)
    val api = new MoneyTransferApi(service)
    Http.server.serve(s":$port", api.service)
  }

  def createAccountsAndTransfer(amount: Double): Unit = {
    val tom = createAccount("Tom", "Green")
    tom.firstName shouldBe "Tom"
    tom.lastName shouldBe "Green"

    val john = createAccount("John", "Smith")

    transfer(tom.id, john.id, amount)

    val newTom = getAccount(tom.id)
    newTom.amount shouldBe tom.amount - amount

    val newJohn = getAccount(john.id)
    newJohn.amount shouldBe john.amount + amount
  }

  val client = Client(new URL(s"http://localhost:$port/api/"))

  def createAccount(firstName: String, lastName: String): Account = {
    Await.result {
      client
        .put("account")
        .withContent(
          Account(UUID.randomUUID, firstName, lastName, defaultAmount),
          "application/json"
        )
        .accept[Coproduct.`"application/json"`.T]
        .send[Account]()
        .handle(handler)
    }
  }

  def getAccount(id: UUID): Account = {
    Await.result {
      client
        .get(s"account/$id")
        .accept[Coproduct.`"application/json"`.T]
        .send[Account]()
        .handle(handler)
    }
  }

  def transfer(from: UUID, to: UUID, amount: Double): Transaction = {
    Await.result {
      client
        .post("transaction")
        .withContent(
          Transaction(from, to, amount),
          "application/json"
        )
        .accept[Coproduct.`"application/json"`.T]
        .send[Transaction]()
        .handle(handler)
    }
  }

  def handler[A]: PartialFunction[Throwable,A] = {
    case ErrorResponse(request, response) =>
      throw new Exception(response.contentString)
  }
}
