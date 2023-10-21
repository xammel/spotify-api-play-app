package utils

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object NestedFutureHelpers {
  implicit class FutureEitherHelper[A, B, C](futureOfEither: Future[Either[A, B]]) {
    def preserveErrorsAndFlatMap(func: B => Future[Either[A, C]]): Future[Either[A, C]] =
      futureOfEither.flatMap {
        case Left(a)  => Future(Left(a))
        case Right(b) => func(b)
      }
  }
}
