package service.impl

import java.sql.Date

import com.jackywong.dynamic.service.DynamicParamQuery
import entity.MyEntity
import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.{Json, OWrites, Reads}
import service.IDynamicCrudService
import slick.dbio.{Effect, NoStream}
import slick.jdbc.GetResult
import slick.sql.FixedSqlAction

import scala.concurrent.ExecutionContext

//不过这里需要指定数据库类型的导入profile
import slick.jdbc.PostgresProfile.api._

/**
  * Created by huangziqi on 2020/3/9
  */
@Singleton
class MyEntityCrudService @Inject() (protected val dbConfigProvider: DatabaseConfigProvider,
                                     dynamicParamQuery: DynamicParamQuery)
                                    (implicit executionContext: ExecutionContext) extends IDynamicCrudService[MyEntity, String] {


  override type TableMapper = MyEntityTable

  class MyEntityTable(tag: Tag) extends Table[MyEntity](tag,"my_entity") {
    def id = column[String]("id",O.PrimaryKey)
    def crtDate = column[Date]("crt_date")
    def lastUpdate = column[Date]("last_update")
    def version = column[Int]("version")
    def name = column[String]("name")
    def balance = column[Int]("balance")
    def * = (id,crtDate,lastUpdate,version,name,balance) <> (MyEntity.tupled, MyEntity.unapply)
  }

  val MyEntities = TableQuery[TableMapper]

  override def getModelKey: String = "myEntity"

  override def getQueryTable = MyEntities

  override def getDynamicParamQuery: DynamicParamQuery = dynamicParamQuery

  override implicit def getResult: GetResult[MyEntity] = GetResult(r => MyEntity(r.<<, r.<<, r.<<, r.<<, r.<<, r.<<))

  override implicit def tableEntityWrites: OWrites[MyEntity] = Json.writes[MyEntity]

  override implicit def tableEntityReads: Reads[MyEntity] = Json.reads[MyEntity]

  override protected def addAction(entity: MyEntity): FixedSqlAction[Int, NoStream, Effect.Write] = getQueryTable += entity

  override protected def updateAction(entity: MyEntity): FixedSqlAction[Int, NoStream, Effect.Write] = getQueryTable.filter(_.id === entity.id).update(entity)

  override protected def deleteAction(id: String): FixedSqlAction[Int, NoStream, Effect.Write] = getQueryTable.filter(_.id === id).delete
}