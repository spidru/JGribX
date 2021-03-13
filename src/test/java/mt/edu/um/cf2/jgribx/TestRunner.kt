package mt.edu.um.cf2.jgribx

import org.junit.runner.JUnitCore

object TestRunner {
	@JvmStatic
	fun main(args: Array<String>) {
		val result = JUnitCore.runClasses(GribTest::class.java)
		for (failure in result.failures) {
			println(failure.toString())
		}
		println(result.wasSuccessful())
	}
}
