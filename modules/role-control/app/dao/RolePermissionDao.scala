package dao

import entity.SRolePermission
import javax.inject.{Inject, Singleton}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext

/**
  * Created by huangziqi on 2020/2/29
  */
@Singleton
class RolePermissionDao @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) (implicit ec:ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile] {
  //必须要导入这个
  import profile.api._

  class SRolePermissionTable(tag: Tag) extends Table[SRolePermission](tag, "role_permission") {
    def id = column[Long]("id",O.PrimaryKey,O.AutoInc)
    def rid = column[Long]("rid")
    def pid = column[Long]("pid")
    def * = (id,rid,pid) <> (SRolePermission.tupled,SRolePermission.unapply)
  }
  val RolePermissions = TableQuery[SRolePermissionTable]
}
