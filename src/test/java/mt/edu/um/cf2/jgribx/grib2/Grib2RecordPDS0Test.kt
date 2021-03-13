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

class Grib2RecordPDS0Test {
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
		val expected = Grib2RecordPDS0(
				0,
				Grib2Parameter.getParameter(discipline, 0, 0)!!,
				Random.nextInt(1.bytesSpace()),
				Random.nextInt(1.bytesSpace()),
				Random.nextInt(1.bytesSpace()),
				Random.nextInt(2.bytesSpace()),
				Random.nextInt(1.bytesSpace()),
				Random.nextInt(8), // 1-7, 10-13
				Random.nextInt(4.bytesSpace()),
				referenceTime,
				Random.nextInt(10) + 1,
				Random.nextInt(1.bytesSpace()),
				Random.nextInt(4.bytesSpace()),
				Random.nextInt(10) + 1,
				Random.nextInt(1.bytesSpace()),
				Random.nextInt(4.bytesSpace()))

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
		assertEquals(34, expected.length)
		assertEquals(34, actual.length)
		assertEquals(4, expected.number)
		assertEquals(4, actual.number)
	}
}
