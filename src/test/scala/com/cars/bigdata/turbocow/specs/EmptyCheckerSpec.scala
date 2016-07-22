package com.cars.bigdata.turbocow

import com.cars.bigdata.turbocow.actions._
//import org.json4s._
import org.json4s.jackson.JsonMethods._

class EmptyCheckerSpec extends UnitSpec {

  // before all tests have run
  override def beforeAll() = {
    super.beforeAll()
  }

  // before each test has run
  override def beforeEach() = {
    super.beforeEach()
  }

  // after each test has run
  override def afterEach() = {
    //myAfterEach()
    super.afterEach()
  }

  // after all tests have run
  override def afterAll() = {
    super.afterAll()
  }

  describe("EmptyChecker") {

    def doCheck(inputJson: String): Boolean = {

      (new EmptyChecker).performCheck(
        CheckParams("fieldA"),
        parse(inputJson),
        Map.empty[String, String],
        new ActionContext
      )
    }

    it("should return true if field is empty") {
      doCheck("""{"fieldA": ""}""") should be (true)
    }
    it("should return true if field is null") {
      doCheck("""{"fieldA": null}""") should be (true)
    }
    it("should return true if field is nonexistent") {
      doCheck("""{"X": ""}""") should be (true)
    }

    it("should return false if anything is in the field") {
      doCheck("""{"fieldA": "X"}""") should be (false)
    }

  }

}



