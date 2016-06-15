package com.cars.turbocow

import scala.collection.immutable.HashMap
import org.json4s.JsonAST.JValue

case class SourceAction(
  source: List[String], 
  actions: List[Action]
) extends Action
{
  /** Run through all actions and perform each in order.
    */
  override def perform(
    sourceFields: List[String], 
    inputRecord: JValue, 
    currentEnrichedMap: Map[String, String],
    context: ActionContext): 
    PerformResult = {

    var enrichedMap = currentEnrichedMap
    
    // TODO - do we want to pass in sourceFields list, or iterate through it?
    actions.foreach{ action => 
      val result = action.perform(sourceFields, inputRecord, enrichedMap, context)

      // merge in the results
      enrichedMap = enrichedMap ++ result.enrichedUpdates
    }
    
    // (todo) there's a better way to do this than foreach...
    
    PerformResult(enrichedMap)
  }
}

