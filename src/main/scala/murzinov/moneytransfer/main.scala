package murzinov.moneytransfer

import com.twitter.finagle.Http
import com.twitter.util.Await

object Main {
  def main(args: Array[String]): Unit = {
    //TODO: can it be wrapped in for-comprehension
    val xa = DB.create.unsafeRun()
    val service = new MoneyTransferService(xa)
    val api = new MoneyTransferApi(service)
    val server = Http.server.serve(":8080", api.service)

    Await.ready(server)
  }
}
