package murzinov.moneytransfer

import com.twitter.finagle.Http
import com.twitter.util.Await

object Main {
  def main(args: Array[String]): Unit = {
    //TODO: can it be wrapped in for-comprehension?
    val db = DoobieDB.create.unsafeRun()
    val api = new Api(db)
    val server = Http.server.serve(":8080", api.service)

    Await.ready(server)
  }
}
