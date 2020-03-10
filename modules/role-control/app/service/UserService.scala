package service

import dao._
import entity.{SUser, UserObject}
import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.{GetResult, JdbcProfile}
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._

/**
  * Created by huangziqi on 2020/2/29
  */
@Singleton
class UserService @Inject()(protected val dbConfigProvider: DatabaseConfigProvider,
                            userDao: UserDao,
                            roleDao: RoleDao,
                            permissionDao: PermissionDao,
                            userRoleDao: UserRoleDao,
                            rolePermissionDao: RolePermissionDao)(implicit ec: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile] {

  private val Users = userDao.Users
  private val Roles = roleDao.Roles
  private val Permissions = permissionDao.Permissions
  private val UserRole = userRoleDao.UserRoles
  private val RolePermissions = rolePermissionDao.RolePermissions

  //其实在考虑要不要换成Future[Option[UserObject]]的结果
  //def getUserObject(id: Long): Option[UserObject] = Await.result(
  //  for {
  //    user <- db.run(Users.filter(_.id === id).take(1).result)
  //    rids <- db.run(UserRole.filter(_.uid === id).map(_.rid).result)
  //    roles <- db.run(Roles.filter(_.id inSet rids).result)
  //    pids <- db.run(RolePermissions.filter(_.pid inSet roles.map(_.id)).map(_.pid).result)
  //    permissions <- db.run(Permissions.filter(_.id inSet pids).result)
  //  } yield user.headOption.map { u =>
  //    UserObject(id, u.name, u.loginName, roles.map(_.name).toList, permissions.map(_.name).toList)
  //  },
  //  1.minute)
  def getUserObject(id: Long): Option[UserObject] = {
    val actions = for {
      user <- Users.filter(_.id === id).take(1).result
      rids <- UserRole.filter(_.uid === id).map(_.rid).result
      roles <- Roles.filter(_.id inSet rids).result
      pids <- RolePermissions.filter(_.rid inSet roles.map(_.id)).map(_.pid).result
      permissions <- Permissions.filter(_.id inSet pids).result
    } yield
      user.headOption.map { u =>
        UserObject(u.id,
          u.name,
          u.loginName,
          roles.map(_.name).toSet,
          permissions.map(_.name).toSet
        )
      }
    val transaction = actions.transactionally
    Await.result(db.run(transaction), 1.minute)
  }
}