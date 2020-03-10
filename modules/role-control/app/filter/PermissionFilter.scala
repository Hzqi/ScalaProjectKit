package filter

import akka.stream.Materializer
import entity._
import filter.PermissionFilter.{NotFoundJwt, UserKey}
import javax.inject.Inject
import play.api.Configuration
import play.api.libs.typedmap.TypedKey
import play.api.mvc.{Filter, RequestHeader, Result}
import play.api.mvc.Results._
import service.{JwtUtilService, PermissionTableService}
import service.JwtUtilService.{JwtOutOfTime, ParseJsonError, ParserKeyError, ValidSuccess}
import service.PermissionTableService.FilterSuccess

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by huangziqi on 2020/3/4
  */
class PermissionFilter @Inject() (implicit val mat: Materializer, ec: ExecutionContext,
                                  config: Configuration,
                                  jwtUtilService: JwtUtilService,
                                  permissionTableService: PermissionTableService) extends Filter {
  private def httpMethod2ResourceType(method: String) = method match {
    case "Get" => Get
    case "Post" => Post
    case "Put" => Put
    case "Delete" => Delete
    case _ => TypeError
  }

  // 从配置文件中获取无需过滤路径
  private def getFilterAnon: Seq[String] = config.getOptional[Seq[String]]("filter.anon.prefix") match {
    case None => List()
    case Some(list) => list
  }

  // 从配置文件中获取需要jwt状态的过滤路径
  private def getFilterWithJwt: Seq[String] = config.getOptional[Seq[String]]("filter.withJwt.prefix") match {
    case None => List()
    case Some(list) => list
  }

  override def apply(nextFilter: RequestHeader => Future[Result])(requestHeader: RequestHeader): Future[Result] = {
    // 1、匹配无需过滤的路径
    if (getFilterAnon.exists{ r =>
      if(r.endsWith("*"))
        requestHeader.uri.startsWith(r.replace("*",""))
      else
        requestHeader.uri == r
    }) nextFilter(requestHeader)
    else requestHeader.headers.get("jwt") match {
      case None => Future.successful(Forbidden(NotFoundJwt.toString))
      case Some(jwt) => jwtUtilService.validJwt(jwt) match {
        case (JwtOutOfTime, _) => Future.successful(Forbidden(JwtOutOfTime.toString))
        case (ParserKeyError, _) | (ParseJsonError, _) => Future.successful(Forbidden("parse JWT error"))
        case (ValidSuccess, Some(userObject)) => //jwt验证成功
          // 2、匹配需要jwt登陆状态的
          if (getFilterWithJwt.exists{r =>
            if (r.endsWith("*"))
              requestHeader.uri.startsWith(r.replace("*",""))
            else
              requestHeader.uri == r
          }) nextFilter(requestHeader)
          else {
            // 3、根据权限列表匹配
            val filterResult = permissionTableService.filterWithPermission(requestHeader.uri, httpMethod2ResourceType(requestHeader.method), userObject)
            filterResult match {
              case FilterSuccess =>
                requestHeader.addAttr(UserKey, userObject)
                nextFilter(requestHeader)
              case r@_ => Future.successful(Forbidden(r.toString))
            }
          }
        case _ => Future.successful(InternalServerError("unknown filter error"))
      }
    }
  }
}

object PermissionFilter {
  trait FilterResult extends JwtUtilService.JwtValidResult
  case object NotFoundJwt

  //构造requestAttr的参数
  //存值： requestHeader.addAttr(UserKey, userObject)
  //取值： request.attrs(UserKey)
  val UserKey: TypedKey[UserObject] = TypedKey.apply[UserObject]("UserObject")
}