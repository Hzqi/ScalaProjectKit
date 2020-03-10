package dao

import entity._
import javax.inject.{Inject, Singleton}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext

/**
  * Created by huangziqi on 2020/2/29
  */
@Singleton
class ResourceDao @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) (implicit ec:ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile] {
  //必须要导入这个
  import profile.api._

  //自定义映射字段类型
  implicit val typedKind = MappedColumnType.base[SResourceKind,String]({
    case ApiKind => "ApiKind"
    case ViewKind => "ViewKind"
    case _ => "KindError"
  },{
    case "ApiKind" => ApiKind
    case "ViewKind" => ViewKind
    case _ => KindError
  })

  implicit val typedType = MappedColumnType.base[SResourceType,String]({
    case Get => "Get"
    case Post => "Post"
    case Put => "Put"
    case Delete => "Delete"
    case View => "View"
    case Button => "Button"
    case _ => "TypeError"
  },{
    case "Get" => Get
    case "Post" => Post
    case "Put" => Put
    case "Delete" => Delete
    case "View" => View
    case "Button" => Button
    case _ => TypeError
  })

  class SResourceTable(tag: Tag) extends Table[SResource](tag, "resource") {
    def id = column[Long]("id",O.PrimaryKey,O.AutoInc)
    def name = column[String]("name")
    def kind = column[SResourceKind]("kind")
    def `type` = column[SResourceType]("type")
    def content = column[String]("content")
    def * = (id,name,kind,`type`,content) <> (SResource.tupled, SResource.unapply)
  }
  val Resources = TableQuery[SResourceTable]

  def getApiResource = Resources filter(_.kind.asColumnOf[String] === "ApiKind")
  def getViewResource = Resources filter(_.kind.asColumnOf[String] === "ViewKind")
}