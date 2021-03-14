package mt.edu.um.cf2.jgribx.grib2

import mt.edu.um.cf2.jgribx.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.BeforeClass
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.random.Random

class Grib2RecordDRS0Test {
	companion object {
		@BeforeClass
		@JvmStatic
		fun setUpBeforeClass() {
			Logger.mode = Logger.LoggingMode.CONSOLE
			JGribX.setLoggingLevel(Logger.DEBUG)
		}
	}

	@Test
	fun testWriting() {
		// Given
		val nDataPoints = Random.nextInt(5, 1000)
		val expected = Grib2RecordDRS0(
				nDataPoints,
				Random.nextDouble(0.0, 100.0).toFloat(),
				Random.nextInt(5),
				Random.nextInt(10),
				Random.nextInt(3, 8),
				Random.nextInt(1.byteSpace()))

		// When
		val bos = ByteArrayOutputStream()
		val gos = GribOutputStream(bos)
		expected.writeTo(gos)

		// Then
		val byteArray = bos.toByteArray()
		val gis = GribInputStream(ByteArrayInputStream(byteArray))
		val actual = Grib2RecordDRS.readFromStream(gis)

		assertNotNull(expected)
		assertEquals(expected, actual)
		assertEquals(21, expected.length)
		assertEquals(21, actual.length)
		assertEquals(5, expected.number)
		assertEquals(5, actual.number)
	}
}
