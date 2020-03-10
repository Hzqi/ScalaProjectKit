package service

import com.google.inject.ImplementedBy

/**
  * Created by huangziqi on 2020/3/10
  */
@ImplementedBy(classOf[TryTraitServiceImpl])
trait TryTraitService {
  def doSomething(): Unit
}
