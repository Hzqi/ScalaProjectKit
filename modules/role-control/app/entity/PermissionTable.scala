package entity

import play.api.libs.json._

/**
  * Created by huangziqi on 2020/2/29
  */
case class PermissionTable(var table: Map[String, Map[SResourceType,List[String]]]){ self =>
//  implicit val writes = new Writes[PermissionTable] {
//    override def writes(o: PermissionTable): JsValue = JsObject(Map(
//      "table" -> JsObject(o.table.map(t1 =>
//        (t1._1, JsObject(t1._2.map{t2 =>
//          val k = t2._1 match {
//            case Get => "Get"
//            case Post => "Post"
//            case Put => "Put"
//            case Delete => "Delete"
//            case _ => "TypeError"
//          }
//          (k,JsArray(t2._2.map(JsString)))
//        }))
//      ))
//    ))
//  }
//  def toJson = Json.toJson(self)
}