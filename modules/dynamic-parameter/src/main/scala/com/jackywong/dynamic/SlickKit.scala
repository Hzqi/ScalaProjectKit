package com.jackywong.dynamic

import slick.jdbc.{PositionedParameters, SQLActionBuilder, SetParameter}

/**
  * Created by huangziqi on 2020/3/6
  */
object SlickKit {
  implicit class SQLActionBuilderConcat (a: SQLActionBuilder) {
    def <+> (b: SQLActionBuilder): SQLActionBuilder = {
      SQLActionBuilder(a.queryParts ++ b.queryParts, (p: Unit, pp: PositionedParameters) => {
        a.unitPConv.apply(p, pp)
        b.unitPConv.apply(p, pp)
      })
    }
  }
}
