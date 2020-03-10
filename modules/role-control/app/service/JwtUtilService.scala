package service

import authentikat.jwt.{JsonWebToken, JwtClaimsSet, JwtClaimsSetJValue, JwtHeader}
import entity.{JwtObject, UserObject}
import javax.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.libs.json.{JsError, JsSuccess, Json}
import service.JwtUtilService._

/**
  * Created by huangziqi on 2020/3/4
  */
@Singleton
class JwtUtilService @Inject() (config: Configuration) {

  implicit val userObjectWrites = Json.writes[UserObject]
  implicit val userObjectReads = Json.reads[UserObject]
  implicit val writes = Json.writes[JwtObject]
  implicit val reads = Json.reads[JwtObject]

  private def getSecret = config.getOptional[String]("jwt.secret") match {
    case None => "JWT.SECRET"
    case Some(s) => s
  }

  private def getExpireTime = config.getOptional[String]("jwt.expireTime") match {
    case None => 2 * 60 * 60 * 1000L //两小时
    case Some(s) =>
      if (s.endsWith("ms")) s.replace("ms","").toLong
      else if (s.endsWith("s")) s.replace("s","").toLong * 1000
      else if (s.endsWith("m")) s.replace("m","").toLong * 60 * 1000
      else if(s.endsWith("h")) s.replace("h","").toLong * 60 * 60 * 1000
      else 2 * 60 * 60 * 1000L //两小时
  }

  def validJwt(jwt:String): (JwtValidResult, Option[UserObject]) = {
    val secret = getSecret
    val expireTime = getExpireTime
    if (!JsonWebToken.validate(jwt,secret))
      (ParserKeyError,None)
    else {
      JsonWebToken.unapply(jwt).map{t =>
        Json.fromJson[JwtObject](Json.parse(t._2.asJsonString)) } match {
        case None => (ParseJsonError, None)
        case Some(JsError(_)) => (ParseJsonError, None)
        case Some(JsSuccess(value, path)) =>
          if (value.startTime + expireTime >= System.currentTimeMillis() )
            (ValidSuccess, Some(value.userObject))
          else
            (JwtOutOfTime, None)
      }
    }
  }

  def createJwt(userObject: UserObject) = {
    val secret = getSecret
    val jwtObject = JwtObject(System.currentTimeMillis(), userObject)
    val json = Json.stringify(Json.toJson[JwtObject](jwtObject))
    JsonWebToken.apply(JwtHeader("HS256"), JwtClaimsSet(json), secret)
  }
}

object JwtUtilService {
  trait JwtValidResult
  case object ValidSuccess extends JwtValidResult
  case object ParserKeyError extends JwtValidResult
  case object ParseJsonError extends JwtValidResult
  case object JwtOutOfTime extends JwtValidResult
}