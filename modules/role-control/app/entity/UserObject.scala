package entity

/**
  * Created by huangziqi on 2020/2/29
  */
case class UserObject(
                       id:Long,
                       name:String,
                       loginName:String,
                       role:Set[String],
                       permissions:Set[String]
                     ) {
  def hasPermission(permission:String):Boolean = permissions.contains(permission)
}
