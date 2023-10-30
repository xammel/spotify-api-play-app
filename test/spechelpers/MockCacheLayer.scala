package spechelpers

import akka.Done
import play.api.cache.AsyncCacheApi

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.Duration
import scala.reflect.ClassTag

trait MockCacheLayer {

  def mockCache: AsyncCacheApi = new AsyncCacheApi {

    var cacheMap: Map[String, Any] = Map.empty[String, Any]

    override def set(key: String, value: Any, expiration: Duration): Future[Done] =
      Future {
        cacheMap = cacheMap.updated(key, value)
        Done
      }

    override def remove(key: String): Future[Done] =
      Future {
        cacheMap = cacheMap.removed(key)
        Done
      }

    override def getOrElseUpdate[A: ClassTag](key: String, expiration: Duration)(orElse: => Future[A]): Future[A] =
      get[A](key).flatMap {
        case None        => orElse
        case Some(value) => Future(value)
      }

    override def get[T: ClassTag](key: String): Future[Option[T]] =
      Future {
        cacheMap.get(key).map(_.asInstanceOf[T])
      }

    override def removeAll(): Future[Done] =
      Future {
        cacheMap = Map.empty[String, Any]
        Done
      }
  }

}
