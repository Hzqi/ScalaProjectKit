package dao

import entity.SPermission
import javax.inject.{Inject, Singleton}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext

/**
  * Created by huangziqi on 2020/2/29
  */
@Singleton
class PermissionDao @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) (implicit ec:ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile] {

  //必须要导入这个
  import profile.api._

  class SPermissionTable(tag: Tag) extends Table[SPermission](tag,"permission") {
    def id = column[Long]("id",O.PrimaryKey,O.AutoInc)
    def name = column[String]("name")
    def description = column[String]("description")
    def * = (id,name,description) <> (SPermission.tupled,SPermission.unapply)
  }
  val Permissions = TableQuery[SPermissionTable]
}
