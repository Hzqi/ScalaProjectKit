package service

import dao._
import entity._
import javax.inject.{Inject, Singleton}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import service.PermissionTableService._
import slick.dbio.Effect
import slick.jdbc.JdbcProfile
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration._

/**
  * Created by huangziqi on 2020/3/3
  */
@Singleton
class PermissionTableService  @Inject()(protected val dbConfigProvider: DatabaseConfigProvider,
                                        userDao: UserDao,
                                        roleDao: RoleDao,
                                        permissionDao: PermissionDao,
                                        userRoleDao: UserRoleDao,
                                        rolePermissionDao: RolePermissionDao,
                                        permissionResourceDao: PermissionResourceDao,
                                        resourceDao: ResourceDao)(implicit ec: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile] {

  private val Users = userDao.Users
  private val Roles = roleDao.Roles
  private val Permissions = permissionDao.Permissions
  private val UserRole = userRoleDao.UserRoles
  private val RolePermissions = rolePermissionDao.RolePermissions
  private val PermissionResourcs = permissionResourceDao.PermissionResources
  private val Resources = resourceDao.Resources

  private val permissionTable:PermissionTable = PermissionTable(Map())// url/content(name) -> HttpMethod/ViewType -> Permission-named-string 的表

  def createTable(): Unit = {
    val action = createTableAction.transactionally
    val future = db.run(action)
    val res = Await.result(future, 60.second)
    permissionTable.table = res
  }

  private def createTableAction = {
    for {
      resources <- resourceDao.getApiResource.result
      tuples <- findPermAction(resources)
    } yield {
      tuples.map(t =>                    // tuples: Seq( (SResource, Seq(Permission) )
        (t._1,
          Map(t._1.`type` -> t._2.map(permission =>
          permission.name).toList))      // (resource, Map(HttpMethod -> List[String]))
      ).groupBy{tupled =>
        val resource = tupled._1
        resource.content //url           // Map(string(url)  -> Seq( (resource, Map(HttpMethond -> List[String])) ) )
      }.map{t =>
        val url: String = t._1
        val methodMap = t._2             //t._2:  Seq( (resource, Map(HttpMethond -> List[String])) ) )
          .map(_._2)                     //再取_2: Seq( Map(HttpMethond -> List[String])) )
          .reduce((a,b) => a ++ b)       // Seq( Map(...)) => Map(HttpMethond -> List[String]))
        (url, methodMap)                 // Map( string(url) -> Map(HttpMethod -> List[String]))
      }
    }
  }

  //将所有Resource每个都去获取Permission，然后「倒转」得到 DBIOAction[Seq[(SResrouce,Seq[Permission])]
  private def findPermAction(resources: Seq[SResource]) = DBIO.sequence( // DBIO.sequence就是Haskell 的traverse: T[M[A]] => M[T[A]]
    resources.map { resource =>
      val queryForPid = PermissionResourcs.filter(_.rid === resource.id).map(_.pid)
      for {
        permissions <- Permissions.filter(_.id in queryForPid).result
      } yield {
        (resource, permissions)
      }
    })

  def getPermissionTable = permissionTable

  def filterWithPermission(content:String, resourceType: SResourceType, userObject: UserObject): FilterResult =
    permissionTable.table.get(content) match {
      case None => NoSuchContent
      case Some(httpMethodMap) =>
        httpMethodMap.get(resourceType) match {
          case None => NoSuchHttpMethod
          case Some(permissions) =>
            if(permissions.forall(userObject.permissions.contains)) FilterSuccess
            else NotEnoughPermission
        }
    }
}

object PermissionTableService {
  sealed trait FilterResult
  case object NoSuchContent extends FilterResult
  case object NoSuchHttpMethod extends FilterResult
  case object NotEnoughPermission extends FilterResult
  case object FilterSuccess extends FilterResult
}