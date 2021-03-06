package com.cars.bigdata.turbocow.actions

import com.cars.bigdata.turbocow.Action
import com.cars.bigdata.turbocow.ActionContext
import com.cars.bigdata.turbocow.JsonUtil
import com.cars.bigdata.turbocow.PerformResult
import com.cars.bigdata.turbocow.ValidString
import org.json4s._
import org.json4s.JsonDSL._
import org.json4s.JsonAST.JNothing
import org.json4s.jackson.JsonMethods._

import scala.io.Source

class Reject(
  val reasonFrom: Option[String] = None, 
  val rejectionReason: Option[String] = None,
  val stopProcessingActionList: Boolean = Reject.defaultStopProcessing)
  extends Action {

  // can't have both:
  if (ValidString(reasonFrom).nonEmpty && ValidString(rejectionReason).nonEmpty) 
    throw new Exception("'reject' actions should not have both 'reason' and 'reasonFrom' fields.  (Pick only one)")

  // must have at least one:
  if (ValidString(reasonFrom).isEmpty && ValidString(rejectionReason).isEmpty) 
    throw new Exception("'reject' actions should have either a 'reason' or 'reasonFrom' fields.  (Add one)")

  /** alt constructor with a JValue
    * If there is a configuration section for this reject action, it will 
    * override anything passed in the constructor.
    * 
    * @param  actionConfig the parsed configuration for this action
    */
  def this(actionConfig: JValue) = {

    this(
      reasonFrom = actionConfig match {
        case jobj: JObject => {
          JsonUtil.extractOption[String](jobj \ "reasonFrom")
        }
        case JNothing | JNull => None
        case _ => None
      },
      rejectionReason = actionConfig match {
        case jobj: JObject => {
          JsonUtil.extractOption[String](jobj \ "reason")
        }
        case JNothing | JNull => None
        case _ => None
      },
      stopProcessingActionList = actionConfig match {
        case jobj: JObject => {
          JsonUtil.extractOptionalBool(jobj \ "stopProcessingActionList", Reject.defaultStopProcessing)
        }
        case _ => Reject.defaultStopProcessing
      }
    )
  }

  /** Perform the rejection
    *
    */
  def perform(
    inputRecord: JValue, 
    currentEnrichedMap: Map[String, String],
    context: ActionContext): 
    PerformResult = {

    implicit val jsonFormats = org.json4s.DefaultFormats

    // get the rejection reason from the right place
    val reason = ValidString(
      if (reasonFrom.nonEmpty) context.scratchPad.getResult(reasonFrom.get)
      else if (rejectionReason.nonEmpty) rejectionReason
      else None
    )

    // make sure nonempty, 
    if (reason.nonEmpty)
      context.rejectionReasons.add(reason.get)

    PerformResult(Map.empty[String, String], stopProcessingActionList)
  }
  
}

object Reject 
{
  val defaultStopProcessing = true
}
