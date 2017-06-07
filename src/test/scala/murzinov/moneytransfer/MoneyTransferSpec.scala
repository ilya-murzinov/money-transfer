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

  behavior of "Money transfer API"

  it should "create accounts and transfer money" in {
    val db = DoobieDB.createInMemory.unsafeRun()
    val api = new Api(db)
    val server = Http.server.serve(s":$port", api.service)

    try {
      val tom = createAccount("Tom", "Green")
      tom.firstName shouldBe "Tom"
      tom.lastName shouldBe "Green"

      val john = createAccount("John", "Smith")

      transfer(tom, john, 10)

      val newTom = getAccount(tom.id)
      newTom.amount shouldBe tom.amount - 10

      val newJohn = getAccount(john.id)
      newJohn.amount shouldBe john.amount + 10
    }
    finally server.close()
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
    }
  }

  def getAccount(id: UUID): Account = {
    Await.result {
      client
        .get(s"account/$id")
        .accept[Coproduct.`"application/json"`.T]
        .send[Account]()
    }
  }

  def transfer(from: Account, to: Account, amount: Double): Transaction = {
    Await.result {
      client
        .post("transfer")
        .withContent(
          Transaction(from.id, to.id, amount),
          "application/json"
        )
        .accept[Coproduct.`"application/json"`.T]
        .send[Transaction]()
        .handle {
          case ErrorResponse(request, response) =>
            throw new Exception(
              s"""
                Error response $response: ${response.contentString}
                      to request $request: ${request.contentString}
              """
            )
        }
    }
  }
}
