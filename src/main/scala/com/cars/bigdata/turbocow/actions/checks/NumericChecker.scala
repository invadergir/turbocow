package com.cars.bigdata.turbocow.actions.checks

import com.cars.bigdata.turbocow.actions.{CheckParams, Checker}
import com.cars.bigdata.turbocow.{ActionContext, JsonUtil, ValidString}
import org.json4s.JValue
import scala.util.{Try, Success, Failure}

class NumericChecker extends Checker {

  /** Check if the requested field is numeric
    */
  def performCheck(
    checkParams: CheckParams,
    inputRecord: JValue, 
    currentEnrichedMap: Map[String, String],
    context: ActionContext): 
    Boolean = {

    // get the test value
    val testVal = JsonUtil.extractValidString(inputRecord \ checkParams.left)
    if(testVal.isDefined) {
      testVal.get.matches(s"""[+-]?((\\d+(e\\d+)?[lL]?)|(((\\d+(\\.\\d*)?)|(\\.\\d+))(e\\d+)?[fF]?))""")
      /*//checking if the String is a number. gives success or failure accordingly
        val result = Try(testVal.get.toDouble)
        result match {
          case Success(v) => true
          case Failure(e) => false
        }*/
    }
    else{
      false
    }
  }
}


