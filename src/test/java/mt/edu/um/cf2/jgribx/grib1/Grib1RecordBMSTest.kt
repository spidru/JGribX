package mt.edu.um.cf2.jgribx.grib1

import mt.edu.um.cf2.jgribx.GribInputStream
import mt.edu.um.cf2.jgribx.GribOutputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.math.ceil
import kotlin.random.Random

class Grib1RecordBMSTest {
	@Test
	fun testWriting() {
		// Given
		val bits = Random.nextInt(100, 1000)
		val expected = Grib1RecordBMS(
				8 - (bits % 8),
				0,
				(0 until bits).map { Random.nextBoolean() }.toBooleanArray())

		// When
		val bos = ByteArrayOutputStream()
		val gos = GribOutputStream(bos)
		expected.writeTo(gos)

		// Then
		val byteArray = bos.toByteArray()
		val gis = GribInputStream(ByteArrayInputStream(byteArray))
		val actual = Grib1RecordBMS.readFromStream(gis)

		assertNotNull(expected)
		assertEquals(expected, actual)
		assertEquals(6 + ceil(bits / 8.0).toInt(), expected.length)
		assertEquals(6 + ceil(bits / 8.0).toInt(), actual.length)
	}
}
