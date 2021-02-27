package mt.edu.um.cf2.jgribx.grib1

import mt.edu.um.cf2.jgribx.GribInputStream
import mt.edu.um.cf2.jgribx.GribOutputStream
import mt.edu.um.cf2.jgribx.bitSpace
import mt.edu.um.cf2.jgribx.byteSpace
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.random.Random

class Grib1RecordPDSTest {
	@Test
	fun testWriting() {
		// Given
		val referenceTime = GregorianCalendar(
				Random.nextInt(1970, 2050),
				Random.nextInt(0, 12),
				Random.nextInt(1, 28),
				Random.nextInt(0, 24),
				Random.nextInt(0, 60),
				0)
				.also { it.timeZone = TimeZone.getTimeZone("UTC") }
		val expected = Grib1RecordPDS(
				Random.nextInt(128),
				Random.nextInt(1.byteSpace()),
				Random.nextInt(1.byteSpace()),
				Random.nextInt(1.byteSpace()),
				Random.nextInt(1.byteSpace()),
				Random.nextInt(127) + 1,
				Random.nextInt(10),
				Random.nextInt(2.byteSpace()),
				referenceTime,
				Random.nextInt(1.byteSpace()),
				Random.nextInt(1.byteSpace()),
				Random.nextInt(1.byteSpace()),
				Random.nextInt(1.byteSpace()),
				Random.nextInt(2.byteSpace()),
				Random.nextInt(1.byteSpace()),
				Random.nextInt(1.byteSpace()),
				Random.nextInt(15.bitSpace()),
				0)

		// When
		val bos = ByteArrayOutputStream()
		val gos = GribOutputStream(bos)
		expected.writeTo(gos)

		// Then
		val byteArray = bos.toByteArray()
		val gis = GribInputStream(ByteArrayInputStream(byteArray))
		val actual = Grib1RecordPDS.readFromStream(gis)

		assertNotNull(expected)
		assertEquals(expected, actual)
		assertEquals(28, expected.length)
		assertEquals(28, actual.length)
	}
}
