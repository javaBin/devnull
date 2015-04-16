package devnull

class UnitSpecExample extends DevNullSpec {

  describe("unit test example") {

    it("passing spec") {
      assert("a" == "a")
    }

    ignore("ignored spec") {
      assert("a" == "b")
    }
  }
}
