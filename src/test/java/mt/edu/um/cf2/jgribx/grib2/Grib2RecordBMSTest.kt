package mt.edu.um.cf2.jgribx.grib2

import mt.edu.um.cf2.jgribx.GribInputStream
import mt.edu.um.cf2.jgribx.GribOutputStream
import mt.edu.um.cf2.jgribx.JGribX
import mt.edu.um.cf2.jgribx.Logger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.BeforeClass
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class Grib2RecordBMSTest {
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
		val expected = Grib2RecordBMS(Grib2RecordBMS.MISSING, booleanArrayOf())

		// When
		val bos = ByteArrayOutputStream()
		val gos = GribOutputStream(bos)
		expected.writeTo(gos)

		// Then
		val byteArray = bos.toByteArray()
		val gis = GribInputStream(ByteArrayInputStream(byteArray))
		val actual = Grib2RecordBMS.readFromStream(gis)

		assertNotNull(expected)
		assertEquals(expected, actual)
		assertEquals(6, expected.length)
		assertEquals(6, actual.length)
		assertEquals(6, expected.number)
		assertEquals(6, actual.number)
	}
}
