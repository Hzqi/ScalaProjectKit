package dao

import entity.SPermissionResource
import javax.inject.{Inject, Singleton}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext

/**
  * Created by huangziqi on 2020/2/29
  */
@Singleton
class PermissionResourceDao @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) (implicit ec:ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile] {
  //必须要导入这个
  import profile.api._

  class SPermissionResourceTable(tag: Tag) extends Table[SPermissionResource](tag, "permission_resource") {
    def id = column[Long]("id",O.PrimaryKey,O.AutoInc)
    def rid = column[Long]("rid")
    def pid = column[Long]("pid")
    def * = (id,rid,pid) <> (SPermissionResource.tupled,SPermissionResource.unapply)
  }
  val PermissionResources = TableQuery[SPermissionResourceTable]
}
