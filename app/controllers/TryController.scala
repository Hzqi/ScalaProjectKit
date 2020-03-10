package controllers

import com.jackywong.dynamic.DynamicParamWorker
import javax.inject.{Inject, Singleton}
import play.api.mvc.{AbstractController, ControllerComponents}
import service.{TryPureService, TryService, TryTraitService}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

/**
  * Created by huangziqi on 2020/3/3
  */
@Singleton
class TryController  @Inject()(cc: ControllerComponents,
                               tryService: TryService,
                               tryPureService: TryPureService,
                               tryTraitService: TryTraitService
                              )(implicit executionContext: ExecutionContext) extends AbstractController(cc) {

  def getValue(key:String) = Action {request =>
    Ok(tryService.getValue(key).toString)
  }

  def setValue(key: String, value :String) = Action {
    Ok(tryService.setValue(key,value).toString)
  }

  def trySomething(withPage: Boolean) = Action.async { request =>
    DynamicParamWorker.analyze(request.queryString) match {
      case Left(msg) => Future.successful(Ok(s"some error : $msg"))
      case Right(value) => tryService.trySomething(value,withPage).map {
        case Left(msg) => Ok(s"some error : $msg")
        case Right(value) => Ok(value.toString)
      }
    }
  }

  def trySomething2 = Action {
    tryService.tryMulti
    Ok("checked")
  }

  def trySomething3 = Action {
    Ok(tryPureService.simpleFunction)
  }

  def trySomething4 = Action {
    tryTraitService.doSomething()
    Ok("checked")
  }
}
