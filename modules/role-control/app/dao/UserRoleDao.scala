package dao

import entity.SUserRole
import javax.inject.{Inject, Singleton}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext

/**
  * Created by huangziqi on 2020/2/29
  */
@Singleton
class UserRoleDao @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) (implicit ec:ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile] {
  //必须要导入这个
  import profile.api._

  class SUserRoleTable(tag: Tag) extends Table[SUserRole](tag, "user_role") {
    def id = column[Long]("id",O.PrimaryKey,O.AutoInc)
    def uid = column[Long]("uid")
    def rid = column[Long]("rid")
    def * = (id,uid,rid) <> (SUserRole.tupled,SUserRole.unapply)
  }
  val UserRoles = TableQuery[SUserRoleTable]

}
