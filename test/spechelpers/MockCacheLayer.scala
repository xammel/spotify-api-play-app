package spechelpers

import akka.Done
import play.Application
import play.api.cache.AsyncCacheApi
import play.api.cache.caffeine.CaffeineCacheApi
import play.api.test._
import play.inject.guice.GuiceApplicationBuilder
import spechelpers.SpecHelpers

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.Duration
import scala.reflect.ClassTag

trait MockCacheLayer {

  lazy val mockCache: AsyncCacheApi = new AsyncCacheApi {

    var cacheMap: Map[String, Any] = Map.empty[String, Any]

    override def set(key: String, value: Any, expiration: Duration): Future[Done] = Future {
      cacheMap = cacheMap.updated(key, value)
      Done
    }

    override def remove(key: String): Future[Done] = Future {
      cacheMap = cacheMap.removed(key)
      Done
    }

    //TODO this is a cheat, but this method is not used in my app
    override def getOrElseUpdate[A: ClassTag](key: String, expiration: Duration)(orElse: => Future[A]): Future[A] = orElse


    override def get[T: ClassTag](key: String): Future[Option[T]] = Future {
      cacheMap.get(key).map(_.asInstanceOf[T])
    }

    override def removeAll(): Future[Done] = Future {
      cacheMap = Map.empty[String, Any]
      Done
    }
  }

}
