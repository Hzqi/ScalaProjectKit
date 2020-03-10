import filter.PermissionFilter
import javax.inject.Inject
import play.api.http.DefaultHttpFilters
import play.api.http.EnabledFilters

/**
  * Created by huangziqi on 2020/3/4
  */

class Filters @Inject() (
                          defaultFilters: EnabledFilters,
                          //gzip: GzipFilter,
                          permissionFilter: PermissionFilter
                        ) extends DefaultHttpFilters(defaultFilters.filters /*:+ gzip*/ :+ permissionFilter: _*)
