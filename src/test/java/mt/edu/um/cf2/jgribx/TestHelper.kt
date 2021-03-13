package mt.edu.um.cf2.jgribx

import org.junit.Assert.*
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.net.URL
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.pow

fun Int.byteSpace() = (this * 8).bitSpace()

fun Int.bitSpace() = 2.0.pow(this).toInt()

fun assertContentEquals(message: String, expected: ByteArray, actual: ByteArray) = assertTrue(
		"${message}\n          %s\nExpected: %s\nActual  : %s".format(
				(1..expected.size).joinToString(" ") { "%02d".format(it) },
				expected.joinToString(" ") { "%02X".format(it) },
				actual.joinToString(" ") { "%02X".format(it) }),
		expected.contentEquals(actual))

fun assertContentEquals(message: String, expected: FloatArray, actual: FloatArray, tolerance: Float = 0f) {
	assertEquals(expected.size, actual.size)
	for (i in expected.indices) {
		if (abs(expected[i] - actual[i]) > tolerance) {
			fail("${message}\n" + (0..i).joinToString("\n") {
				"%f\t%f\t%f (tolerance=%f) => matches: %s".format(
						expected[it], actual[it],
						expected[it] - actual[it],
						tolerance,
						abs(expected[it] - actual[it]) <= tolerance)
			})
		}
	}
}

fun assertCoordinatesWithGoldStandard(url: URL,
									  gribFile: GribFile = GribFile(url.openStream(), { m, _ -> m == 0 })) {
	val gribFilepath = File(url.toURI()).absolutePath
	val dumpFilepath = File(url.toURI()).absolutePath + ".dump-coords"

	val pb = when (gribFile.edition) {
		1 -> TODO()
		2 -> ProcessBuilder("grib2/wgrib2/wgrib2", gribFilepath, "-gridout", dumpFilepath)
		else -> throw RuntimeException()
	}.redirectOutput(ProcessBuilder.Redirect.DISCARD)
	val cmd = pb.command().joinToString(" ")
	println("Executing: ${cmd}")

	val process = pb.start()
	val exited = process.waitFor(30, TimeUnit.SECONDS)
	println("Executed: ${process.exitValue()} (exited=${exited})")
	//assertTrue("Process not exited", exited)
	//assertEquals("Process exited with code ${process.exitValue()}", 0, process.exitValue())

	val grid = gribFile.records.first().gridDefinition

	BufferedReader(FileReader(dumpFilepath)).use { reader ->
		var line = ""
		var index = 0
		val coords = grid.coords
		val xCoords = grid.xCoords
		val yCoords = grid.yCoords
		val cols = grid.cols
		while (reader.readLine()?.also { line = it } != null) {
			val (i, j, lat, lng) = line.split(",").map { it.trim() }
					.let { (i, j, lat, lng) -> listOf(i.toInt(), j.toInt(), lat.toFloat(), lng.toFloat()) }
			assertEquals("Lat at ${i}, ${j} (index: [${index}][1])", coords[index][1].toFloat(), lat.toFloat(), 0.001f)
			assertEquals("Lng at ${i}, ${j} (index: [${index}][0])", (coords[index][0].toFloat() + 360f) % 360f, lng.toFloat(), 0.001f)
			assertEquals("Lat at ${i}, ${j} (index: [${index / cols}])", yCoords[index / cols].toFloat(), lat.toFloat(), 0.001f)
			assertEquals("Lng at ${i}, ${j} (index: [${index % cols}])", (xCoords[index % cols].toFloat() + 360f) % 360f, lng.toFloat(), 0.001f)
			index++
		}
	}
}

/** Compare values in each record against a "gold standard" (wgrib/wgrib2) */
fun assertDataWithGoldStandard(url: URL,
							   gribFile: GribFile = GribFile(url.openStream()),
							   records: Iterable<Int>? = null,
							   skipGribRecords: Iterable<Int> = emptyList(),
							   skipWGribRecords: Iterable<Int> = emptyList()) {
	val gribFilepath = File(url.toURI()).absolutePath
	val dumpFilepath = File(url.toURI()).absolutePath + ".dump"
	val gribRecords = records ?: (0 until gribFile.recordCount)
	val wgribIndices = mutableListOf<Int>()
	repeat(gribRecords.count()) {
		var next = wgribIndices.lastOrNull()?.let { it + 1 } ?: 0
		while (skipWGribRecords.contains(next)) next++
		wgribIndices.add(next)
	}

	for (r in gribRecords.filterNot { skipGribRecords.contains(it) }) {
		// Get values from GRIB file using JGribX
		val record = gribFile.records[r]
		record.productDefinition.log(r, 0)
		val actualValues: FloatArray = record.values

		val pb = when (gribFile.edition) {
			1 -> ProcessBuilder("wgrib", gribFilepath, "-d", "${wgribIndices[r] + 1}", "-text", "-o", dumpFilepath, "-nh")
			2 -> ProcessBuilder("grib2/wgrib2/wgrib2", gribFilepath, "-d", "${wgribIndices[r] + 1}", "-text", dumpFilepath, "-no_header")
			else -> throw RuntimeException()
		}.redirectOutput(ProcessBuilder.Redirect.DISCARD)
		val cmd = pb.command().joinToString(" ")
		println("Executing: ${cmd}")

		val process = pb.start()
		val exited = process.waitFor(5, TimeUnit.SECONDS)
		assertTrue("Process exited", exited)
		assertEquals("Process exited with code ${process.exitValue()}", 0, process.exitValue())

		var same = 0
		var different = 0
		val expectedValues = FloatArray(actualValues.size)
		val maxValue = actualValues.filter { it.isFinite() }.maxOrNull() ?: 0f
		val tolerance = when { // Dynamic tolerance based on data magnitude
			maxValue >= 100000 -> 0.9f
			maxValue >= 10000 -> 0.5f
			maxValue >= 1000 -> 0.05f
			maxValue >= 100 -> 0.005f
			maxValue >= 10 -> 0.0005f
			else -> 0.00005f
		}
		BufferedReader(FileReader(dumpFilepath)).use { reader ->
			var line = ""
			var i = 0
			while (reader.readLine()?.also { line = it } != null) {
				val expectedValue = line.toFloat()
				expectedValues[i] = expectedValue
				if (abs(expectedValue - actualValues[i]) <= tolerance) same++ else different++
				i++
			}
		}
		assertContentEquals("Record %d, %d values OK, %d values DIFFERENT".format(r, same, different),
				expectedValues, actualValues, tolerance)
	}
}

fun readIgnoredRecordIndices(resourcePath: String): List<Int> = GribTest::class.java
		.getResource(resourcePath)
		.openStream()
		.reader()
		.useLines { it.map(String::toInt).toList() }
