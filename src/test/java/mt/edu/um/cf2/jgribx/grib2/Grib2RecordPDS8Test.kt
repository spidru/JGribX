package mt.edu.um.cf2.jgribx.grib2

import mt.edu.um.cf2.jgribx.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.random.Random

class Grib2RecordPDS8Test {
	companion object {
		@BeforeClass
		@JvmStatic
		fun setUpBeforeClass() {
			Logger.mode = Logger.LoggingMode.CONSOLE
			JGribX.setLoggingLevel(Logger.DEBUG)
		}
	}

	@Before
	fun setUp() {
		Grib2Parameter.loadDefaultParameters()
	}

	@Test
	fun testWriting() {
		// Given
		val referenceTime = GregorianCalendar(
				Random.nextInt(1970, 2050),
				Random.nextInt(0, 12),
				Random.nextInt(1, 28),
				Random.nextInt(0, 24),
				Random.nextInt(0, 60),
				Random.nextInt(0, 60))
				.also { it.timeZone = TimeZone.getTimeZone("UTC") }
		val discipline = ProductDiscipline.VALUES[0]!!
		val overallTimeIntervalEnd = GregorianCalendar(
				Random.nextInt(1970, 2050),
				Random.nextInt(0, 12),
				Random.nextInt(1, 28),
				Random.nextInt(0, 24),
				Random.nextInt(0, 60),
				Random.nextInt(0, 60))
				.also { it.timeZone = TimeZone.getTimeZone("UTC") }
		val nTimeRanges = Random.nextInt(5)
		val statsProcess = IntArray(nTimeRanges)
		val timeIncrementType = IntArray(nTimeRanges)
		val timeUnitIndicator1 = IntArray(nTimeRanges)
		val timeRangeLength = IntArray(nTimeRanges)
		val timeUnitIndicator2 = IntArray(nTimeRanges)
		val timeIncrement = IntArray(nTimeRanges)
		repeat(nTimeRanges) {
			statsProcess[it] = Random.nextInt(1.byteSpace())
			timeIncrementType[it] = Random.nextInt(1.byteSpace())
			timeUnitIndicator1[it] = Random.nextInt(1.byteSpace())
			timeRangeLength[it] = Random.nextInt(4.byteSpace())
			timeUnitIndicator2[it] = Random.nextInt(1.byteSpace())
			timeIncrement[it] = Random.nextInt(4.byteSpace())
		}
		val expected = Grib2RecordPDS8(
				0,
				Grib2Parameter.getParameter(discipline, 0, 0)!!,
				Random.nextInt(1.byteSpace()),
				Random.nextInt(1.byteSpace()),
				Random.nextInt(1.byteSpace()),
				Random.nextInt(2.byteSpace()),
				Random.nextInt(1.byteSpace()),
				Random.nextInt(8), // 1-7, 10-13
				Random.nextInt(4.byteSpace()),
				referenceTime,
				Random.nextInt(10) + 1,
				Random.nextInt(1.byteSpace()),
				Random.nextInt(4.byteSpace()),
				Random.nextInt(10) + 1,
				Random.nextInt(1.byteSpace()),
				Random.nextInt(4.byteSpace()),

				overallTimeIntervalEnd,
				Random.nextInt(1.byteSpace()),
				statsProcess,
				timeIncrementType,
				timeUnitIndicator1,
				timeRangeLength,
				timeUnitIndicator2,
				timeIncrement)

		// When
		val bos = ByteArrayOutputStream()
		val gos = GribOutputStream(bos)
		expected.writeTo(gos)

		// Then
		val byteArray = bos.toByteArray()
		val gis = GribInputStream(ByteArrayInputStream(byteArray))
		val actual = Grib2RecordPDS.readFromStream(gis, discipline, referenceTime)

		assertNotNull(expected)
		assertEquals(expected, actual)
		assertEquals(46 + 12 * nTimeRanges, expected.length)
		assertEquals(46 + 12 * nTimeRanges, actual.length)
		assertEquals(4, expected.number)
		assertEquals(4, actual.number)
	}
}
