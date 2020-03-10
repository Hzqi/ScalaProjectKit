package entity

/**
  * Created by huangziqi on 2020/2/29
  */

//应用用户
case class SUser(
                id:Long,
                name:String,
                loginName:String,
                loginPassword:String
                )

//角色
case class SRole(
               id:Long,
               name:String,
               description:String
               )

//权限
case class SPermission(
                     id:Long,
                     name:String,
                     description:String
                     )

//资源
sealed trait SResourceKind
case object ApiKind extends SResourceKind
case object ViewKind extends SResourceKind
case object KindError extends SResourceKind
sealed trait SResourceType
case object Get extends SResourceType
case object Post extends SResourceType
case object Put extends SResourceType
case object Delete extends SResourceType
case object View extends SResourceType
case object Button extends SResourceType
case object TypeError extends SResourceType
case class SResource(
                    id:Long,
                    name:String,
                    kind:SResourceKind,
                    `type`:SResourceType,
                    content:String
                    )

//用户-角色关联
case class SUserRole(
                    id:Long,
                    uid:Long,
                    rid:Long
                    )

//角色-权限关联
case class SRolePermission(
                          id:Long,
                          rid:Long,
                          pid:Long
                          )

//权限-资源关联
case class SPermissionResource(
                              id:Long,
                              rid:Long,
                              pid:Long
                              )