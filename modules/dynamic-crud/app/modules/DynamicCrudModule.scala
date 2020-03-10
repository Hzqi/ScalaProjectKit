package modules

import com.google.inject.{AbstractModule, TypeLiteral}
import com.google.inject.multibindings.Multibinder
import org.reflections.Reflections
import service.IDynamicCrudService

/**
  * Created by huangziqi on 2020/3/9
  */
class DynamicCrudModule extends AbstractModule{
  override def configure(): Unit = {
    import scala.collection.JavaConverters._
    val r = new Reflections("service")  //扫描查找包路径下的包
    val subtypes = r.getSubTypesOf(classOf[IDynamicCrudService[_,_]]) //查找目标trait下的子类

    val executorBinder = Multibinder.newSetBinder(binder(), new TypeLiteral[IDynamicCrudService[_,_]]{})  //构建一个Binder
    //遍历找到的接口子类，绑定到binder中
    //注意，这里只能用过java.util.Set来集合注入
    subtypes.asScala.foreach{ clazz =>
      executorBinder.addBinding().to(clazz)
    }
  }
}
