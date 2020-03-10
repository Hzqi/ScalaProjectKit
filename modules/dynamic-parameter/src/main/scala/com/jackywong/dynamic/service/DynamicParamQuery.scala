package com.jackywong.dynamic.service

import java.sql.Date
import java.time._
import java.time.format.DateTimeFormatter

import com.jackywong.dynamic.SlickKit._
import com.jackywong.dynamic._
import javax.inject.{Inject, Singleton}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.PostgresProfile.api._
import slick.jdbc.{JdbcProfile, PositionedParameters, SQLActionBuilder}
import slick.lifted.TableQuery

import scala.annotation.tailrec
import scala.util.{Failure, Success, Try}

/**
  * Created by huangziqi on 2020/3/5
  */
@Singleton
class DynamicParamQuery @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile] {

  //驼峰转下划线
  private def camel2dash(name: String) = {
    @tailrec
    def _camel2dash(chars: List[Char], tmpRes: List[Char]): String = chars match {
      case head :: tail =>
        if (head.isUpper) _camel2dash(tail, head.toLower :: '_' :: tmpRes)
        else _camel2dash(tail, head.toLower :: tmpRes)
      case Nil => tmpRes.reverse.mkString
    }

    _camel2dash(name.toList, Nil)
  }

  //字符串转成值
  private def string2value[T](entityClass: Class[T], field:String): String => Any = {
    val fieldType = entityClass.getDeclaredField(field).getType
    typeMap(fieldType)
  }

  case class UnSupportedYetTypeException(`type`:String)
    extends Exception(s"DynamicParamQuery module doesn't support ${`type`} yet. If you receive this message, I'm sorry.")

  //目前满足类型的转值map
  private val typeMap:Map[Class[_], String => Any] = Map(
    classOf[Byte] -> ((s:String) => s.toByte),
    classOf[Short] -> ((s:String) => s.toShort),
    classOf[Int] -> ((s:String) => s.toInt),
    classOf[Long] -> ((s:String) => s.toLong),
    classOf[BigDecimal] -> ((s:String) => BigDecimal(s)),
    classOf[Float] -> ((s:String) => s.toFloat),
    classOf[Double] -> ((s:String) => s.toDouble),
    classOf[java.sql.Blob] -> ((s:String) => throw UnSupportedYetTypeException("Blob")),
    classOf[java.sql.Clob] -> ((s:String) => throw UnSupportedYetTypeException("Clob")),
    classOf[Array[Byte]] -> ((s:String) => s.split(",").map(_.toByte)),
    classOf[java.sql.Date] -> ((s:String) => Try(s.toLong) match {
      case Success(value) => new Date(value)
      case Failure(_) =>
        val dateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").parse(s)
        val millis = LocalDateTime.from(dateTime).atZone(ZoneId.systemDefault()).toInstant.toEpochMilli
        new Date(millis)
    }),
    classOf[java.sql.Time] -> ((s:String) => Try(s.toLong) match {
      case Success(value) => new java.sql.Time(value)
      case Failure(_) =>
        val dateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").parse(s)
        val millis = LocalDateTime.from(dateTime).atZone(ZoneId.systemDefault()).toInstant.toEpochMilli
        new java.sql.Time(millis)
    }),
    classOf[java.sql.Timestamp] -> ((s:String) => Try(s.toLong) match {
      case Success(value) => new java.sql.Timestamp(value)
      case Failure(_) =>
        val dateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").parse(s)
        val millis = LocalDateTime.from(dateTime).atZone(ZoneId.systemDefault()).toInstant.toEpochMilli
        new java.sql.Timestamp(millis)
    }),
    classOf[java.time.LocalDate] -> ((s:String) => Try(s.toLong) match {
      case Success(value) =>
        LocalDate.ofInstant(Instant.ofEpochMilli(value), ZoneId.systemDefault)
      case Failure(_) =>
        val dateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").parse(s)
        LocalDate.from(dateTime)
    }),
    classOf[java.time.LocalTime] -> ((s:String) => Try(s.toLong) match {
      case Success(value) =>
        LocalTime.ofInstant(Instant.ofEpochMilli(value), ZoneId.systemDefault)
      case Failure(_) =>
        val dateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").parse(s)
        LocalTime.from(dateTime)
    }),
    classOf[java.time.LocalDateTime] -> ((s:String) => Try(s.toLong) match {
      case Success(value) =>
        LocalDateTime.ofInstant(Instant.ofEpochMilli(value), ZoneId.systemDefault)
      case Failure(_) =>
        val dateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").parse(s)
        LocalDateTime.from(dateTime)
    }),
    classOf[Instant] -> ((s:String) => Try(s.toLong) match {
      case Success(value) =>
        Instant.ofEpochMilli(value)
      case Failure(_) =>
        val dateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").parse(s)
        Instant.from(dateTime)
    }),
    classOf[java.time.OffsetTime] -> ((s:String) => Try(s.toLong) match {
      case Success(value) =>
        OffsetTime.ofInstant(Instant.ofEpochMilli(value), ZoneId.systemDefault)
      case Failure(_) =>
        val dateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").parse(s)
        OffsetTime.from(dateTime)
    }),
    classOf[java.time.OffsetDateTime] -> ((s:String) => Try(s.toLong) match {
      case Success(value) =>
        OffsetDateTime.ofInstant(Instant.ofEpochMilli(value), ZoneId.systemDefault)
      case Failure(_) =>
        val dateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").parse(s)
        OffsetDateTime.from(dateTime)
    }),
    classOf[java.time.ZonedDateTime] -> ((s: String) => Try(s.toLong) match {
      case Success(value) =>
        ZonedDateTime.ofInstant(Instant.ofEpochMilli(value), ZoneId.systemDefault)
      case Failure(_) =>
        val dateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").parse(s)
        ZonedDateTime.from(dateTime)
    }),
    classOf[Boolean] -> ((s:String) => s.toBoolean),
    classOf[String] -> ((s:String) => s),
    classOf[java.util.UUID] -> ((s:String) => java.util.UUID.fromString(s))
  )

  implicit val setAnyParameter = new scala.AnyRef with slick.jdbc.SetParameter[Any] {
    override def apply(v: Any, pp: PositionedParameters): Unit = {
      v match {
        case b: Byte => pp.setByte(b)
        case s: Short => pp.setShort(s)
        case i: Int => pp.setInt(i)
        case l: Long => pp.setLong(l)
        case b: BigDecimal => pp.setBigDecimal(b)
        case f: Float => pp.setFloat(f)
        case d: Double => pp.setDouble(d)
        case ab: Array[Byte] => pp.setBytes(ab)
        case d: java.sql.Date => pp.setDate(d)
        case t: java.sql.Time => pp.setTime(t)
        case t: java.sql.Timestamp => pp.setTimestamp(t)
        case d: java.time.LocalDate =>
          pp.setDate(new Date(Instant.from(d).toEpochMilli))
        case d:java.time.LocalDateTime =>
          pp.setDate(new Date(Instant.from(d).toEpochMilli))
        case i:Instant =>
          pp.setDate(new Date(i.toEpochMilli))
        case d:java.time.ZonedDateTime =>
          pp.setDate(new Date(Instant.from(d).toEpochMilli))
        case d:java.time.OffsetTime =>
          pp.setDate(new Date(Instant.from(d).toEpochMilli))
        case d:java.time.OffsetDateTime =>
          pp.setDate(new Date(Instant.from(d).toEpochMilli))
        case b: Boolean => pp.setBoolean(b)
        case s: String => pp.setString(s)
        case _ => throw UnSupportedYetTypeException("UUID")
      }
    }
  }

  def toQuery[T, E <: slick.lifted.AbstractTable[_]](entityClass: Class[T], dynamicParams: DynamicParams): TableQuery[E] => Either[String, SQLActionBuilder] = (table: TableQuery[E]) => {
    val sqlHead = sql"""SELECT * FROM "#${table.baseTableRow.tableName}" WHERE 1 = 1 """

    dynamicParams.filters.map { filter =>
      val field = camel2dash(filter.field)
      val value = string2value(entityClass,filter.field)(filter.value)
      filter.operate match {
        case Equal => Right(sql" and #${field} = ${value}")
        case NotEqual => Right(sql" and #${field} <> ${value}")
        case Less => Right(sql" and #${field} < ${value}")
        case LessEqual => Right(sql" and #${field} <= ${value}")
        case Greater => Right(sql" and #${field} > ${value}")
        case GreaterEqual => Right(sql" and #${field} >= ${value}")
        case Contains => Right(sql" and #${field} like ${"%" + filter.value + "%"}")
        case StartWith => Right(sql" and #${field} like ${"%" + filter.value + "%"}")
        case EndWith => Right(sql" and #${field} like ${"%" + filter.value + "%"}")
        case OperaError => Left(s"unmatched operator ${filter.operate}")
      }
    }.fold(Right(sqlHead)) {
      case (a@Left(_), _) => a
      case (_, b@Left(_)) => b
      case (Right(a), Right(b)) => Right(a <+> b)
    } match {
      case e@Left(_) => e
      case origin@Right(sqlBuilder) =>
        (dynamicParams.sortDirection, dynamicParams.sortField) match {
          case (_, "") | (NonSort, _) => origin
          case (ASC, sortField) => Right(sqlBuilder <+> sql" order by $sortField asc")
          case (DESC, sortField) => Right(sqlBuilder <+> sql" order by $sortField desc")
          case _ => Left("sortField parse error.")
        }
    }
  }


  def toQueryWithPage[T, E <: slick.lifted.AbstractTable[_]](entityClass: Class[T], dynamicParams: DynamicParams): TableQuery[E] => Either[String, SQLActionBuilder] = (table: TableQuery[E]) => {
    toQuery(entityClass, dynamicParams)(table) match {
      case e@Left(_) => e
      case origin@Right(sqlBuilder) =>
        if (dynamicParams.size != 0 && dynamicParams.page != 0) {
          val limit = dynamicParams.size
          val offset = (dynamicParams.page - 1) * limit
          Right(sqlBuilder <+> sql" limit $limit offset $offset")
        }
         else origin
    }
  }
}
