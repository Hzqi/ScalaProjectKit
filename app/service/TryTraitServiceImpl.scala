package service

import javax.inject.Singleton

/**
  * Created by huangziqi on 2020/3/10
  */
@Singleton
class TryTraitServiceImpl extends TryTraitService {
  override def doSomething(): Unit = println("do something.")
}
