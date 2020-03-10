package controllers.dynamic

import com.jackywong.dynamic.DynamicParamWorker
import javax.inject.{Inject, Singleton}
import play.api.libs.json.{JsError, JsSuccess, Json}
import play.api.mvc.{AbstractController, ControllerComponents}
import service.IDynamicCrudService

import scala.concurrent.{ExecutionContext, Future}
import scala.collection.JavaConverters._


/**
  * Created by huangziqi on 2020/3/9
  */
@Singleton
class TryController @Inject()(cc: ControllerComponents,
                               dynamicCrudServices: java.util.Set[IDynamicCrudService[_,_]])(implicit executionContext: ExecutionContext) extends AbstractController(cc) {



  def insert(modelKey:String) = Action.async(parse.json) { request =>
    dynamicCrudServices.asScala.find(_.getModelKey == modelKey) match {
      case None => Future.successful(NotFound)
      case Some(service) => service.add(request.body).map{
        case None => InternalServerError("json parse error")
        case Some(n) => Ok(s"Affected: $n")
      }
    }
  }

  def update(modelKey:String) = Action.async(parse.json) { request =>
    dynamicCrudServices.asScala.find(_.getModelKey == modelKey) match {
      case None => Future.successful(NotFound)
      case Some(service) => service.update(request.body).map{
        case None => InternalServerError("json parse error")
        case Some(n) => Ok(s"Affected: $n")
      }
    }
  }

  def delete(modelKey:String, idStr:String) = Action.async{ request =>
    dynamicCrudServices.asScala.find(_.getModelKey == modelKey) match {
      case None => Future.successful(NotFound)
      case Some(service) =>
        service.delete(idStr).map(i => Ok(s"Affected: $i"))
    }
  }

  def query(modelKey:String) = Action.async{ request =>
    dynamicCrudServices.asScala.find(_.getModelKey == modelKey) match {
      case None => Future.successful(NotFound)
      case Some(service) => DynamicParamWorker.analyze(request.queryString) match {
        case Left(msg) => Future.successful(BadRequest(msg))
        case Right(value) => service.queryJs(value).map {
          case Left(msg) => BadRequest(msg)
          case Right(value) => Ok(value)
        }
      }
    }
  }
}
