package service

import com.jackywong.dynamic.DynamicParams
import com.jackywong.dynamic.service.DynamicParamQuery
import play.api.db.slick.HasDatabaseConfigProvider
import play.api.libs.json._
import slick.dbio.{Effect, NoStream}
import slick.jdbc.PostgresProfile.api._
import slick.jdbc.{GetResult, JdbcProfile, SQLActionBuilder}
import slick.lifted.TableQuery
import slick.sql.FixedSqlAction
import utils.String2Value

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect._
/**
  * Created by huangziqi on 2020/3/9
  */
abstract class IDynamicCrudService[TableEntity : ClassTag, IdType : ClassTag](implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] { self =>

  //需要实现的类型，表映射类型 (extends Table[TableEntity](tag:Tag, _tableName) 的 )
  type TableMapper <: slick.lifted.AbstractTable[TableEntity]

  def getModelKey: String

  //需要实现的方法，获取表映射对象的
  def getQueryTable : TableQuery[TableMapper]

  // 获取动态查询参数，这里因为是被其他模块写成了依赖注入，而这个文件又是一个trait，所以需要这个方法去获取出来
  def getDynamicParamQuery: DynamicParamQuery

  implicit def getResult: GetResult[TableEntity]

  //json 的写隐式模型
  implicit def tableEntityWrites: OWrites[TableEntity]
  //json 的读隐式模型
  implicit def tableEntityReads: Reads[TableEntity]

  // 增
  protected def addAction(entity: TableEntity): FixedSqlAction[Int, NoStream, Effect.Write]

  // 改
  protected def updateAction(entity: TableEntity): FixedSqlAction[Int, NoStream, Effect.Write]

  // 删
  protected def deleteAction(id: IdType):FixedSqlAction[Int, NoStream, Effect.Write]

  // 查
  protected def dynamicQuery(dynamicParams: DynamicParams): Either[String, SQLActionBuilder] =
    getDynamicParamQuery.toQuery(classTag[TableEntity].runtimeClass, dynamicParams)(getQueryTable)

  def add(entityJs: JsValue): Future[Option[Int]] = fromJson(entityJs) match {
    case JsError(_) => Future.successful(None)
    case JsSuccess(value, _) => db.run(addAction(value)).map(i => Some(i))
  }

  def update(entityJs: JsValue): Future[Option[Int]] = fromJson(entityJs) match {
    case JsError(_) => Future.successful(None)
    case JsSuccess(value, _) => db.run(updateAction(value)).map(i => Some(i))
  }

  def delete(idStr: String): Future[Int] = db.run(deleteAction(string2IdType(idStr)))

  def string2IdType(idStr: String) : IdType = String2Value.string2Value(classTag[IdType].runtimeClass, idStr).asInstanceOf[IdType]

  def query(dynamicParams: DynamicParams): Future[Either[String, List[TableEntity]]] = dynamicQuery(dynamicParams) match {
    case Left(msg) => Future.successful(Left(msg))
    case Right(sqlAction) => db.run(sqlAction.as[TableEntity]).map(i => Right(i.toList))
  }

  def queryJs(dynamicParams: DynamicParams): Future[Either[String, JsValue]] = dynamicQuery(dynamicParams) match {
    case Left(msg) => Future.successful(Left(msg))
    case Right(sqlAction) => db.run(sqlAction.as[TableEntity]).map(i => Right(Json.toJson(i.map(toJson))))
  }

  def fromJson(js:JsValue): JsResult[TableEntity] = Json.fromJson(js)

  def toJson(entity: TableEntity): JsValue = Json.toJson(entity)
}