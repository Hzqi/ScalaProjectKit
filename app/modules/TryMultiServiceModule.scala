package modules

import com.google.inject.AbstractModule
import com.google.inject.multibindings.Multibinder
import com.google.inject.name.Names
import org.reflections.Reflections
import service.{A, B, C, TryServiceTrait}

/**
  * Created by huangziqi on 2020/3/7
  */
class TryMultiServiceModule extends AbstractModule {
  override def configure(): Unit = {
    import scala.collection.JavaConverters._
    val r = new Reflections("service")  //扫描查找包路径下的包
    val subtypes = r.getSubTypesOf(classOf[TryServiceTrait]) //查找目标trait下的子类

    val executorBinder = Multibinder.newSetBinder(binder(), classOf[TryServiceTrait])  //构建一个Binder
    //遍历找到的接口子类，绑定到binder中
    //注意，这里只能用过java.util.Set来集合注入
    subtypes.asScala.foreach{ clazz =>
      executorBinder.addBinding().to(clazz)
    }
  }
}