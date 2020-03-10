package entity

import java.sql.Date


/**
  * Created by huangziqi on 2020/3/9
  */
case class MyEntity(id:String,
                    crtDate: Date,
                    lastUpdate: Date,
                    version:Int,
                    name:String,
                    balance:Int){

}
