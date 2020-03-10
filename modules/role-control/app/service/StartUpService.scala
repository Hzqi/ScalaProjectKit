package service

import javax.inject.{Inject, Singleton}
import play.api.Logger

/**
  * Created by huangziqi on 2020/3/4
  */
@Singleton
class StartUpService @Inject() (permissionTableService: PermissionTableService) {
  val logger: Logger = Logger("service")

  @Inject()
  def onStartUp = {
    logger.info("Starting startup process...")
    permissionTableService.createTable()
    logger.info("finished creating permission-table")
  }
}
