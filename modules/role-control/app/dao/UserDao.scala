package dao

import entity.SUser
import javax.inject.{Inject, Singleton}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext

/**
  * Created by huangziqi on 2020/2/29
  */
@Singleton
class UserDao @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) (implicit ec:ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile]  {

  //必须要导入这个
  import profile.api._

  class SUserTable(tag:Tag) extends Table[SUser](tag,"user") {
    def id = column[Long]("id",O.PrimaryKey,O.AutoInc)
    def name = column[String]("name")
    def loginName = column[String]("login_name")
    def loginPassword = column[String]("login_password")
    def * = (id,name,loginName,loginPassword) <> (SUser.tupled, SUser.unapply)
  }
  val Users = TableQuery[SUserTable]
}
