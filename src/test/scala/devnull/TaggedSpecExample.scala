package devnull

import devnull.TestTags.{SlowTag, DatabaseTag}

class TaggedSpecExample extends DevNullSpec {

  describe("example of tagged test") {

    it("database tagged spec", DatabaseTag) {
      assert("a" == "a")
    }

    ignore("ignored database spec", DatabaseTag) {
      assert("a" == "b")
    }

    it("slow tagged spec", SlowTag) {
      assert("b" == "b")
    }
  }
}
