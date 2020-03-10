package service

import javax.inject.Singleton

/**
  * Created by huangziqi on 2020/3/7
  */
trait TryServiceTrait {
  def name:String
}

@Singleton
class A extends TryServiceTrait{
  override def name: String = "AAA"
}

@Singleton
class B extends TryServiceTrait{
  override def name: String = "BBB"
}

@Singleton
class C extends TryServiceTrait{
  override def name: String = "CCC"
}