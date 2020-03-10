package service.others

import javax.inject.Singleton
import service.TryServiceTrait

/**
  * Created by huangziqi on 2020/3/7
  */
@Singleton
class TryClass extends TryServiceTrait{
  override def name: String = "TryClass"
}
