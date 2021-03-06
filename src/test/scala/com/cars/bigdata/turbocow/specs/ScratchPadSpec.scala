package com.cars.bigdata.turbocow

import com.cars.bigdata.turbocow.actions._
import org.scalatest.junit.JUnitRunner

import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._

class ScratchPadSpec 
  extends UnitSpec 
  with MockitoSugar {

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

  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////
  // Tests start
  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  describe("ScratchPad")  { // ------------------------------------------------

    it("should have no elements after construction") {
      val sp = new ScratchPad()
      sp.size should be (0)
    }
  }

  describe("set") {
    it("should store something") {
      val sp = new ScratchPad()
      sp.set("A", "a some thing")
      sp.get("A") should be (Some("a some thing"))
      sp.size should be (1)
      sp.set("B", 123)
      sp.get("B") should be (Some(123))
      sp.size should be (2)
    }
  }

  describe("remove") {
    it("should remove the item") {
      val sp = new ScratchPad()
      sp.set("L", "AA")
      sp.size should be (1)
      sp.remove("L")
      sp.size should be (0)
      sp.get("L") should be (None)
    }
  }

  describe("setResult") {
    it("should save a result as a string") {
      val sp = new ScratchPad()
      sp.set("L-result", "XXX")
      sp.setResult("L", "result 1")
      sp.getResult("L") should be (Some("result 1"))
      sp.get("L-result") should be (Some("XXX"))
    }

    it("should save a result and not interfere with anything else") {
      val sp = new ScratchPad()
      sp.set("L-result", "XXX")
      sp.get("L-result") should be (Some("XXX"))

      sp.setResult("L", "result 1")
      sp.getResult("L") should be (Some("result 1"))
      sp.get("L-result") should be (Some("XXX"))
    }

    it("should overwrite previous results when setting a new result with same name") {
      val sp = new ScratchPad()
      sp.setResult("L", "result 1")
      sp.getResult("L") should be (Some("result 1"))

      sp.setResult("L", "XXX")
      sp.getResult("L") should be (Some("XXX"))
    }
  }

  describe("removeResult") {
    it("should remove the result") {
      val sp = new ScratchPad()
      sp.setResult("L", "result 1")
      sp.resultSize should be (1)
      sp.removeResult("L")
      sp.resultSize should be (0)
      sp.getResult("L") should be (None)
    }
  }

  describe("resultSize") {

    it("should return the right size") {
      val sp = new ScratchPad()
      sp.resultSize should be (0)

      sp.setResult("L", "result 1")
      sp.resultSize should be (1)
      sp.getResult("L") should be (Some("result 1"))
      sp.setResult("C", "result 2")
      sp.resultSize should be (2)
      sp.getResult("C") should be (Some("result 2"))
    }
  }

  describe("copy") {
    it("should make a copy of itself with different hash map objects but same items") {
      val sp = new ScratchPad
      sp.setResult("a", "A")
      sp.setResult("b", "B")
      sp.set("c", "C")
      sp.set("d", "D")

      val copy = sp.copy
      copy.allMainPad eq sp.allMainPad should be (false)
      copy.allResults eq sp.allResults should be (false)

      copy.getResult("a") eq sp.getResult("a") should be (false)
      copy.getResult("b") eq sp.getResult("b") should be (false)
      copy.get("c") eq sp.get("c") should be (false)
      copy.get("d") eq sp.get("d") should be (false)

      copy.getResult("a") should be (sp.getResult("a"))
      copy.getResult("b") should be (sp.getResult("b"))
      copy.get("c") should be (sp.get("c"))
      copy.get("d") should be (sp.get("d"))
    }
  }

}
    

