package com.cars.turbocow.actions

import com.cars.turbocow.Action
import com.cars.turbocow.ActionContext
import com.cars.turbocow.JsonUtil
import org.json4s._
import org.json4s.JsonDSL._
import org.json4s.JsonAST.JNothing
import org.json4s.jackson.JsonMethods._
import com.cars.turbocow.ActionFactory
import com.cars.turbocow.PerformResult

import Lookup._

import scala.io.Source

class Lookup(
  val fromFile: Option[String],
  val fromDB: Option[String],
  val fromTable: Option[String],
  val where: String,
  val equals: String,
  val select: List[String],
  val onPass: SubActionList = new SubActionList,
  val onFail: SubActionList = new SubActionList
) extends Action {

  override def toString() = {
    
    val sb = new StringBuffer
    sb.append(s"""Lookup:{fromFile(${fromFile.getOrElse("<NONE>")})""")
    sb.append(s""", fromDB(${fromDB.getOrElse("<NONE>")})""")
    sb.append(s""", fromTable(${fromTable.getOrElse("<NONE>")})""")
    sb.append(s""", where($where)""")
    sb.append(s""", select = """)
    select.foreach{ f => sb.append(f + ",") }
    sb.append(s""", onPass = ${onPass.toString}""")
    sb.append(s""", onFail = ${onFail.toString}""")
    sb.append("}")
    sb.toString
  }

  /*
  val dbAndTableNames = from.split('.')
  if (fromIsDB)
  val dbName: Option[String] = 
    if (!fromIsDB) None
    else Option(dbAndTableNames.head)

  val tableName: Option[String] = 
    if (!fromIsDB) None
    else Option(dbAndTableNames.last)
  */

  val fields = if(select.length > 1) {
    val tableName = fromTable match {
      case None => ""
      case some => "`" + some.get + "."
    }
    tableName + select.mkString("`," + tableName)
  }
  else if(select.length == 1){
    select.head
  }
  else ""

  // "db.table", or "fromFile"
  // todo - separate object for 'fromFile' lookup, or remove once hdfs/hive testing works
  val dbAndTable = 
    if (fromFile.nonEmpty)
      fromFile.get
    else if (fromDB.nonEmpty && fromTable.nonEmpty) 
      s"${fromDB.get}.${fromTable.get}"
    else
      throw new Exception(s"couldn't find fromDB($fromDB) or fromTable($fromTable)")

  // get all the fields needed in this table (select + where), without dups
  val allFields = { 
    if (where != null && where.nonEmpty) {
      select :+ where
    }
    else select
  }.distinct

  /** Perform the lookup
    *
    */
  def perform(
    inputRecord: JValue, 
    currentEnrichedMap: Map[String, String],
    context: ActionContext): 
    PerformResult = {

    implicit val jsonFormats = org.json4s.DefaultFormats

    val result: PerformResult = {

      // search in the table for this key
      fromFile match {
        case None => { // do hdfs "lookup"
        
          val caches = context.tableCaches
          if (caches.isEmpty) {
            PerformResult()
          }
          else {  // cache is not empty

            fromDB.getOrElse{ throw new Exception("TODO - reject this because fromDB not found in config") }
            fromTable.getOrElse{ throw new Exception("TODO - reject this because fromTable not found in config") }

            val fromDBAndTable = dbAndTable
            val tableCacheOpt = caches.get(fromDBAndTable)
            tableCacheOpt.getOrElse{ throw new Exception("couldn't find cached lookup table for: "+fromDBAndTable) }
                                   
            // get the table cache and do lookup
            val tc = tableCacheOpt.get
            val lookupValue = JsonUtil.extractOption[String](inputRecord \ equals)
            // TODOTODO if getting this value fails..... ?

            // todo what if the select fields are not there
            PerformResult(
              select.map{ field => 
                val resultOpt: Option[String] = tc.lookup(
                  where, 
                  lookupValue.get.toString,
                  field)
                if (resultOpt.isEmpty) Map.empty[String, String]
                else Map(field -> resultOpt.get)
              }.reduce( _ ++ _ ) // combine all maps into one
            )
          }
        }
        case _ => { // local file lookup
        
          // look up local file and parse as json.
          val configAST = parse(Source.fromFile(fromFile.get).getLines.mkString)

          // get value of source field from the input JSON:
          val lookupValue = JsonUtil.extractOption[String](inputRecord \ equals)

          val dimRecord: Option[JValue] = 
            if( lookupValue.isEmpty ) None
            else configAST.children.find( record => (record \ where) == JString(lookupValue.get) )

          if (dimRecord.isEmpty) { // failed

            // Set the failure reason in the scratchpad for pickup later and 
            // possible rejection.
            val rejectReason = s"""Invalid $where: '${lookupValue.getOrElse("")}'"""
            context.scratchPad.setResult("lookup", rejectReason)

            onFail.perform(inputRecord, currentEnrichedMap, context)
          }
          else { // ok, found it

            context.scratchPad.setResult("lookup", s"""Field '$where' exists in table '$dbAndTable':  '${lookupValue.getOrElse("")}'""")

            val enrichedAdditions = select.map{ selectField => 
              val fieldVal = (dimRecord.get \ selectField).extract[String]
              (selectField, fieldVal)
            }.toMap

            onPass.perform(inputRecord, currentEnrichedMap ++ enrichedAdditions, context)
          }
        }
      }
    }

    // return result
    result
  }

}

object Lookup
{

  /** Alternate constructor to parse the Json config.
    */
  def apply(
    actionConfig: JValue, 
    actionFactory: Option[ActionFactory]): 
    Lookup = {

    new Lookup(
      fromFile = JsonUtil.extractOption[String](actionConfig \ "fromFile"),
      fromDB = JsonUtil.extractOption[String](actionConfig \ "fromDB"),
      fromTable = JsonUtil.extractOption[String](actionConfig \ "fromTable"),
      where = JsonUtil.extractString(actionConfig \ "where"),
      equals = JsonUtil.extractValidString(actionConfig \ "equals").getOrElse("equals cannot be blank in 'lookup' action."),
      select = 
        (actionConfig \ "select").children.map{e => JsonUtil.extractString(e) },
      onPass = new SubActionList(actionConfig \ "onPass", actionFactory),
      onFail = new SubActionList(actionConfig \ "onFail", actionFactory)
    )
  }

}

