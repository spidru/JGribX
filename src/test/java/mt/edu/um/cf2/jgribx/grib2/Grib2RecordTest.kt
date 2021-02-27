package mt.edu.um.cf2.jgribx.grib2

import mt.edu.um.cf2.jgribx.GribFile
import mt.edu.um.cf2.jgribx.GribTest
import org.junit.Assert.*
import org.junit.Test
import java.util.*
import kotlin.math.abs

class Grib2RecordTest {
	@Test
	fun testCutOutPerRecord() {
		val refTimes: MutableList<Calendar> = ArrayList()
		refTimes.add(GregorianCalendar(2021, Calendar.FEBRUARY, 27, 0, 0, 0)
				.apply { timeZone = TimeZone.getTimeZone("UTC") })
		val url = GribTest::class.java.getResource("/North_Sea_NE_ICON_EU_EWAM_20210227-00.grb2")
		val file = GribFile(url.openStream())

		val (northInput, eastInput, southInput, westInput) = listOf(58.493725, 3.633039, 57.807050, 1.759022)
		val (northCalc, eastCalc, southCalc, westCalc) = listOf(58.5, 3.6875, 57.75, 1.75)
		assertNotEquals(file.records[0], file.records[1]) // 14
		file.records.forEach { record ->
			val expectedCoords = record.gridDefinition.coords
					.filter { (lon, lat) -> lat in (southCalc..northCalc) && lon in (westCalc..eastCalc) }
			val expectedData = expectedCoords
					.map { (lon, lat) -> Triple(lon, lat, record.getValue(lat, lon)) }
					//.onEach { (lon, lat, value) -> println("ORIG ${lat}, ${lon}: ${value}") }
					.map { (_, _, value) -> value }

			record.cutOut(northInput, eastInput, southInput, westInput)

			val coordinatesMatch = expectedCoords
					.asSequence()
					.mapIndexed { i, c -> c to record.gridDefinition.coords[i] }
					.map { (a, b) -> abs(a[0] - b[0]) to abs(a[1] - b[1]) }
					.map { (dLng, dLat) -> dLng + dLat }
					.onEach { assertEquals(0.0, it, 0.0) }
					.all { it == 0.0 }
			assert(coordinatesMatch)
			val dataMatch = expectedData
					.asSequence()
					.mapIndexed { i, v -> v to record.values.getOrNull(i) }
					.onEach { (expected, actual) -> assertEquals(expected, actual) }
					.all { (a, b) -> b != null && a == b }
			assert(dataMatch)
		}
		assertEquals("GRIB edition", 2L, file.edition.toLong())
		assertEquals("Reference time(s)", file.referenceTimes, refTimes)
		assertArrayEquals("Weather centres", intArrayOf(78), file.centreIDs.toIntArray())
		assertArrayEquals("Generating processes", intArrayOf(2), file.processIDs)
	}

	@Test
	fun testCutOutFile() {
		val refTimes: MutableList<Calendar> = ArrayList()
		refTimes.add(GregorianCalendar(2021, Calendar.FEBRUARY, 27, 0, 0, 0)
				.apply { timeZone = TimeZone.getTimeZone("UTC") })
		val url = GribTest::class.java.getResource("/North_Sea_NE_ICON_EU_EWAM_20210227-00.grb2")
		val file = GribFile(url.openStream())

		val (northInput, eastInput, southInput, westInput) = listOf(58.493725, 3.633039, 57.807050, 1.759022)
		val (northCalc, eastCalc, southCalc, westCalc) = listOf(58.5, 3.6875, 57.75, 1.75)
		assertNotEquals(file.records[0], file.records[1]) // 14
		val expectedCoords = file.records[0].gridDefinition.coords
				.filter { (lon, lat) -> lat in (southCalc..northCalc) && lon in (westCalc..eastCalc) }
		val expectedData = mutableListOf<List<Float>>()
		file.records.forEach { record ->
			expectedData.add(expectedCoords
					.map { (lon, lat) -> Triple(lon, lat, record.getValue(lat, lon)) }
					//.onEach { (lon, lat, value) -> println("ORIG ${lat}, ${lon}: ${value}") }
					.map { (_, _, value) -> value })
		}

		file.cutOut(northInput, eastInput, southInput, westInput)

		file.records.forEachIndexed { index, record ->
			val coordinatesMatch = expectedCoords
					.asSequence()
					.mapIndexed { i, c -> c to record.gridDefinition.coords[i] }
					.map { (a, b) -> abs(a[0] - b[0]) to abs(a[1] - b[1]) }
					.map { (dLng, dLat) -> dLng + dLat }
					.onEach { assertEquals(0.0, it, 0.0) }
					.all { it == 0.0 }
			assert(coordinatesMatch)
			val dataMatch = expectedData[index]
					.asSequence()
					.mapIndexed { i, v -> v to record.values.getOrNull(i) }
					.onEach { (expected, actual) -> assertEquals(expected, actual) }
					.all { (a, b) -> b != null && a == b }
			assert(dataMatch)
		}
		assertEquals("GRIB edition", 2L, file.edition.toLong())
		assertEquals("Reference time(s)", file.referenceTimes, refTimes)
		assertArrayEquals("Weather centres", intArrayOf(78), file.centreIDs.toIntArray())
		assertArrayEquals("Generating processes", intArrayOf(2), file.processIDs)
	}
}
