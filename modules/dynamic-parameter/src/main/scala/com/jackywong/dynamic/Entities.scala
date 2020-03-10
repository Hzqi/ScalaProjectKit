package com.jackywong.dynamic

/**
  * Created by huangziqi on 2020/3/5
  */

case class DynamicParams(
                        filters: Seq[DynamicFilter],
                        page: Int,
                        size: Int,
                        sortDirection: SortDirection,
                        sortField: String
                        )

case class DynamicFilter(
                        field: String,
                        operate: OperateType,
                        value: String
                        )

sealed trait OperateType
case object Equal extends OperateType
case object NotEqual extends OperateType
case object Less extends OperateType
case object LessEqual extends OperateType
case object Greater extends OperateType
case object GreaterEqual extends OperateType
case object Contains extends OperateType
case object StartWith extends OperateType
case object EndWith extends OperateType
case object OperaError extends OperateType
object OperateType {
  def of(name:String) = name.toUpperCase match {
    case "EQUAL" => Equal
    case "NOTEQUAL" => NotEqual
    case "LESS" => Less
    case "LESSEQUAL" => LessEqual
    case "GREATER" => Greater
    case "GREATEREQUAL" => GreaterEqual
    case "CONTAINS" => Contains
    case "STARTWITH" => StartWith
    case "ENDWITH" => EndWith
    case _ => OperaError
  }
}

sealed trait SortDirection
case object ASC extends SortDirection
case object DESC extends SortDirection
case object NonSort extends SortDirection
case object DirectionError extends SortDirection
object SortDirection {
  def of(name:String) = name.toUpperCase match {
    case "ASC" => ASC
    case "DESC" => DESC
    case _ => DirectionError
  }
}