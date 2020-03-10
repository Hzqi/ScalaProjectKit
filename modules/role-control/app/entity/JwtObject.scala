package entity

import play.api.libs.json.Json

/**
  * Created by huangziqi on 2020/3/4
  */
case class JwtObject(startTime: Long, userObject: UserObject) {
}