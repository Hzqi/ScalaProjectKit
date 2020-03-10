package com.jackywong.vo.response

/**
  * Created by huangziqi on 2019/6/18
  */
case class ResultObject[T](code:Int, msg:String, data:T) {

}
