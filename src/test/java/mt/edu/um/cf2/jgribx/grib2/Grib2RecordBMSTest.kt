package mt.edu.um.cf2.jgribx.grib2

import mt.edu.um.cf2.jgribx.GribInputStream
import mt.edu.um.cf2.jgribx.GribOutputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class Grib2RecordBMSTest {
	@Test
	fun testWriting() {
		// Given
		val expected = Grib2RecordBMS(Grib2RecordBMS.MISSING)

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
