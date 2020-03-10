package modules

import com.google.inject.AbstractModule
import service.StartUpService

/**
  * Created by huangziqi on 2020/3/4
  */

//自定义Module， 用于启动时使用
class EagerLoaderModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[StartUpService]).asEagerSingleton()
  }
}