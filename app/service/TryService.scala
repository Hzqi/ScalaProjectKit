package service

import com.jackywong.dynamic.DynamicParams
import com.jackywong.dynamic.service.DynamicParamQuery
import dao.UserDao
import entity.SUser
import javax.inject.{Inject, Singleton}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.{GetResult, JdbcProfile}
import slick.lifted.TableQuery

import scala.concurrent.{ExecutionContext, Future}
import scala.collection.JavaConverters._

/**
  * Created by huangziqi on 2020/3/3
  */
@Singleton
class TryService  @Inject()(protected val dbConfigProvider: DatabaseConfigProvider
                            , userDao: UserDao
                            , dynamicParamQuery: DynamicParamQuery
                            , serviceTraits: java.util.Set[TryServiceTrait]
                           ) (implicit executionContext: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile] {

  val serviceTmpVal = collection.mutable.Map[String,String]()
  def getValue(key: String): Option[String] = serviceTmpVal.get(key)
  def setValue(key:String, value:String): Option[String] = serviceTmpVal.put(key,value)

  def trySomething(dynamicParams: DynamicParams, withPage: Boolean):Future[Either[String,List[SUser]]] = {
    implicit val getSUserGet = GetResult(r => SUser(r.<<,r.<<,r.<<,r.<<))
    val queryEither = if(withPage) dynamicParamQuery.toQueryWithPage(classOf[SUser], dynamicParams)(userDao.Users)
    else dynamicParamQuery.toQuery(classOf[SUser], dynamicParams)(userDao.Users)
    queryEither match {
      case e@Left(msg) => Future.successful(Left(msg))
      case Right(value) => db.run(value.as[SUser]).map(v => Right(v.toList))
    }
  }

  def tryMulti = serviceTraits.asScala.foreach(i => println(i.name))
}
