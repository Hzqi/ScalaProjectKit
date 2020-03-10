package utils

import java.sql.Date
import java.time._
import java.time.format.DateTimeFormatter

import scala.util.{Failure, Success, Try}

/**
  * Created by huangziqi on 2020/3/9
  */
object String2Value {

  def string2Value(clazz: Class[_],str: String):Any = typeMap(clazz)(str)

  private val typeMap:Map[Class[_], String => Any] = Map(
    classOf[Byte] -> ((s:String) => s.toByte),
    classOf[Short] -> ((s:String) => s.toShort),
    classOf[Int] -> ((s:String) => s.toInt),
    classOf[Long] -> ((s:String) => s.toLong),
    classOf[BigDecimal] -> ((s:String) => BigDecimal(s)),
    classOf[Float] -> ((s:String) => s.toFloat),
    classOf[Double] -> ((s:String) => s.toDouble),
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
}
