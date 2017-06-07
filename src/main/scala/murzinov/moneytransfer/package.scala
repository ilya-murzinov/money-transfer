package murzinov

import com.twitter.util.{ Future, Promise }
import fs2.Task

package object moneytransfer {
  implicit class TaskOps[A](task: Task[A]) {
    //this is necessary for converting fs2.Tast from Doobie
    //to com.twitter.util.Future for Finch
    def toFuture: Future[A] = {
      val promise = Promise[A]
      task.unsafeRunAsync {
        case Left(e) => promise.setException(e)
        case Right(v) => promise.setValue(v)
      }
      promise
    }
  }
}
