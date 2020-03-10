package com.jackywong.dynamic

import scala.annotation.tailrec

/**
  * Created by huangziqi on 2020/3/5
  */
object DynamicParamWorker {
  val EmptyParams = DynamicParams(Nil,1,10, NonSort,"")

  def analyze(queryString: Map[String,Seq[String]]): Either[String,DynamicParams] =
    forAnalyze(queryString.toList, Map(),Map(), EmptyParams)

  @tailrec
  private def forAnalyze(contentList: Seq[(String,Seq[String])],
                         tmpFilterMap: Map[String,DynamicFilter],
                         tmpOperateMap: Map[String,OperateType],
                         tmpResult: DynamicParams): Either[String,DynamicParams] = contentList match {
    case (k,vs) :: tail =>
      if (k.endsWith("Operate")) {
        val key = k.replace("Operate","")
        val opera = OperateType.of(vs.head)
        if(opera == OperaError)
          Left(s"Operator error for: ${vs.head}")
        else
          forAnalyze(tail, tmpFilterMap, tmpOperateMap + ((key,opera)), tmpResult)
      } else if (k == "page") {
        forAnalyze(tail, tmpFilterMap, tmpOperateMap, tmpResult.copy(page = vs.head.toInt))
      } else if (k == "size") {
        forAnalyze(tail, tmpFilterMap, tmpOperateMap, tmpResult.copy(size = vs.head.toInt))
      } else if (k == "sortDirection") {
        val direction = SortDirection.of(vs.head)
        if (direction == DirectionError)
          Left(s"SortDirection error for: ${vs.head}")
        else
          forAnalyze(tail, tmpFilterMap, tmpOperateMap, tmpResult.copy(sortDirection = direction))
      } else if (k == "sortField") {
        forAnalyze(tail, tmpFilterMap, tmpOperateMap, tmpResult.copy(sortField = vs.head))
      } else {
        val filter = DynamicFilter(k,Equal,vs.head)
        forAnalyze(tail, tmpFilterMap + ((k,filter)), tmpOperateMap, tmpResult)
      }

    case Nil =>
      val newFilterMap = tmpFilterMap.map{t =>
        val operateOpt = tmpOperateMap.get(t._1)
        if (operateOpt.isDefined) {
          (t._1, t._2.copy(operate = operateOpt.get))
        } else t
      }
      Right(tmpResult.copy(filters = newFilterMap.values.toSeq))
  }
}
