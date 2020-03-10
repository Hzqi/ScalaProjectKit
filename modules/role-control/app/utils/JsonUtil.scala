package utils

import play.api.libs
import play.api.libs.json._

/**
  * Created by huangziqi on 2020/3/4
  */
object JsonUtil {
  //通用型Map写Json器
  implicit def mapWrites[K,A:Writes] = new Writes[Map[K,A]] {
    override def writes(o: Map[K, A]): JsValue = JsObject(o.map(t => (t._1.toString, Json.toJson(t._2))))
  }

  //通用型Map读Json器
  implicit def mapReads[String, A: Reads] = new Reads[Map[String, A]] {
    override def reads(json: JsValue): JsResult[Map[String, A]] = json match {
      case JsObject(map) =>
        val list = map.toList.map { t =>
          Json.fromJson(t._2) match {
            case s@JsSuccess(_, _) => (t._1, s)
            case e@JsError(_) => (t._1, e)
          }
        }
        val errors = list.filter(_._2.isError)
        if (errors.isEmpty)
          JsSuccess(list.map(t => (t._1,t._2.get)).toMap).asInstanceOf[JsResult[Map[String,A]]]
        else
          errors.head._2.asInstanceOf[JsError]
      case _ => JsError()
    }
  }
}
