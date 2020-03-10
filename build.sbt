//定义
lazy val ProjectKitSetting = Seq(
  name := """ProjectKit""",
  organization := "com.jackywong",
  version := "1.0-SNAPSHOT",
  scalaVersion := "2.12.8",
  libraryDependencies ++= ProjectKitDependencies
)

//全局依赖
lazy val ProjectKitDependencies = Seq(
  guice,
  "com.google.inject.extensions" % "guice-multibindings" % "4.2.2", //guice多重注入
  "org.reflections" % "reflections" % "0.9.11",                     //反射器，自动扫描

  "org.scalatestplus.play" %% "scalatestplus-play" % "4.0.2" % Test,
  //数据库scalikejdbc
  "mysql" % "mysql-connector-java" % "5.1.38",
  "org.postgresql" % "postgresql" % "42.2.5",
  //scalikejdbc
//  "org.scalikejdbc" %% "scalikejdbc"                  % "3.3.2",
//  "org.scalikejdbc" %% "scalikejdbc-config"           % "3.3.2",
//  "org.scalikejdbc" %% "scalikejdbc-play-initializer" % "2.7.0-scalikejdbc-3.3",
  //slick
  "com.typesafe.slick" %% "slick" % "3.3.0",
  "org.slf4j" % "slf4j-nop" % "1.6.4",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.3.0",
  "com.typesafe.play" %% "play-slick" % "4.0.0",
  "com.typesafe.play" %% "play-slick-evolutions" % "4.0.0",
  //scalaz
  "org.scalaz" %% "scalaz-core" % "7.2.30",
  //jwt
  "com.jason-goodwin" %% "authentikat-jwt" % "0.4.5",
  //缓存
  caffeine
)

lazy val ProjectKit = (project in file(".")).enablePlugins(PlayScala)
  .settings(ProjectKitSetting)
  .dependsOn(Common, RoleControl, DynamicParameter, DynamicCrud)
  .aggregate(Common, RoleControl, DynamicParameter, DynamicCrud)

//通用模块
lazy val Common = project in file("modules/common")

//鉴权模块
lazy val RoleControl = (project in file("modules/role-control"))
  .enablePlugins(PlayScala)
  .dependsOn(Common)
  .settings(Seq(libraryDependencies ++= ProjectKitDependencies)) //鉴权模块的依赖包要和根项目的一样

//动态查询参数模块
lazy val DynamicParameter = (project in file("modules/dynamic-parameter"))
  .dependsOn(Common)
  .settings(Seq(libraryDependencies ++= ProjectKitDependencies))

//动态Crud模块
lazy val DynamicCrud = (project in file("modules/dynamic-crud"))
  .enablePlugins(PlayScala)
  .dependsOn(Common, DynamicParameter)
  .settings(Seq(libraryDependencies ++= ProjectKitDependencies))

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.jackywong.controllers._"

// Adds additional packages into conf/role.routes
// play.sbt.role.routes.RoutesKeys.routesImport += "com.jackywong.binders._"
