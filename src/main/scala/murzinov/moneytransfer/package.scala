package murzinov

import com.twitter.util.{ Future, Promise }
import fs2.Task

package object moneytransfer {
  implicit class TaskOps[A](task: Task[A]) {
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
