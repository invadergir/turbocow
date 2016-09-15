package com.cars.bigdata.turbocow

import AvroOutputWriter._
import org.apache.spark.sql.types._
import org.json4s.JsonAST._

case class AvroFieldConfig(
  structField: StructField,
  defaultValue: JValue
) {

  // perform some checks on the data
  checkDefaultValue

  /** Get the default value according to what type it is.
    *  
    * @return 'primitive' type (Int, Float, etc.) in an Any;
    *         throws on JNothing (no default provided) or
    *         the JSON type was not a string, numeric, boolean,
    *         or null.
    */
  def getDefaultValue: Any = {
    implicit val jsonFormats = org.json4s.DefaultFormats
    defaultValue match {
      case j: JString => j.extract[String]
      case j: JInt => structField.dataType match {
        case IntegerType => j.extract[Int]
        case LongType => j.extract[Long]
      }
      case j: JDouble => structField.dataType match {
        case FloatType => j.extract[Float]
        case DoubleType => j.extract[Double]
      }
      case j: JBool => j.extract[Boolean]
      case JNull => null
      case JNothing => throw new Exception("no default value was specified")
      case _ => throw new Exception(s"unsupported JSON type specified as 'default' value for '${structField.name}' field.")
    }
  }


  /** Check for existence of the default value as well as its type.
    * 
    * @throws Exception if default value does not exist, or the 
    *         type is not compatible with the field StructField.
    */
  def checkDefaultValue: Unit = {

    implicit val jsonFormats = org.json4s.DefaultFormats
    lazy val mustBe = "(Must be " + {
      val dt = {structField.dataType.toString}
      if (structField.nullable) s"null or a $dt"
      else s"a $dt"
    } + ".)"
    defaultValue match {
      case j: JString => if (structField.dataType != StringType ) throw new Exception(s"invalid default value specified as 'default' value for '${structField.name}' field.  $mustBe")
      case j: JInt => structField.dataType match {
        case IntegerType | LongType => ;
        case _ => throw new Exception(s"invalid default value specified as 'default' value for '${structField.name}' field.  $mustBe")
      }
      case j: JDouble => structField.dataType match {
        case FloatType | DoubleType => ;
        case _ => throw new Exception(s"invalid default value specified as 'default' value for '${structField.name}' field.  $mustBe")
      }
      case j: JBool => if (structField.dataType != BooleanType) throw new Exception(s"invalid default value specified as 'default' value for '${structField.name}' field.  $mustBe")
      case JNull => if (structField.dataType != NullType && !structField.nullable) throw new Exception(s"invalid default value specified as 'default' value for '${structField.name}' field.  $mustBe")
      case JNothing => throw new Exception("a default value MUST be specified for every avro output field.")
      case _ => throw new Exception(s"an unsupported JSON type was specified as 'default' value for '${structField.name}' field.")
    }
  }

}

object AvroFieldConfig {

  /** Create a new one based on a json JValue.
    */
  def apply(config: JValue): AvroFieldConfig = {
    implicit val jsonFormats = org.json4s.DefaultFormats
    AvroFieldConfig(
      getStructFieldFromAvroElement(config),
      (config \ "default")
    )
  }

  lazy val allSupportedAvroTypesMap = Map( 
    "string"-> StringType,
    "int"-> IntegerType,
    "long"-> LongType,
    "float"-> FloatType,
    "double"-> DoubleType,
    "boolean"-> BooleanType,
    "null"-> NullType
  )

  lazy val exampleJsonTypesMap = Map(
    "string"-> JString(""),
    "int"-> JInt(0),
    "long"-> JInt(0),
    "float"-> JDouble(0.0),
    "double"-> JDouble(0.0),
    "boolean"-> JBool(false),
    "null"-> JNull
  )

  // Helper to get the unique values out of exampleJsonTypesMap.
  def exampleJsonTypesMapUniqueValues: List[JValue] = {
    exampleJsonTypesMap.values.toList.distinct
  }

}
