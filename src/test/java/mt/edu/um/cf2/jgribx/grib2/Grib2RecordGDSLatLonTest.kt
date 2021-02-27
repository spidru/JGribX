package mt.edu.um.cf2.jgribx.grib2

import mt.edu.um.cf2.jgribx.GribInputStream
import mt.edu.um.cf2.jgribx.GribOutputStream
import mt.edu.um.cf2.jgribx.byteSpace
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.random.Random

class Grib2RecordGDSLatLonTest {
	@Test
	fun testWriting() {
		// Given
		val expected = Grib2RecordGDSLatLon(
				Random.nextInt(1.byteSpace()),
				Random.nextInt(4.byteSpace()),
				Random.nextInt(1.byteSpace()),
				Random.nextInt(1.byteSpace()),

				Random.nextInt(1.byteSpace()),
				Random.nextInt(1.byteSpace()),
				Random.nextInt(4.byteSpace()),
				Random.nextInt(1.byteSpace()),
				Random.nextInt(4.byteSpace()),
				Random.nextInt(1.byteSpace()),
				Random.nextInt(4.byteSpace()),
				Random.nextInt(4.byteSpace()),
				Random.nextInt(4.byteSpace()),
				0,
				Random.nextInt(4.byteSpace()),
				Random.nextInt(4.byteSpace()) / 1.0e6,
				Random.nextInt(4.byteSpace()) / 1.0e6,
				0x10 or 0x20,
				Random.nextInt(4.byteSpace()) / 1.0e6,
				Random.nextInt(4.byteSpace()) / 1.0e6,
				Random.nextInt(4.byteSpace()) / 1.0e6,
				Random.nextInt(4.byteSpace()) / 1.0e6,
				Grib2RecordGDS.ScanMode(0x40),
				Random.nextBytes(Random.nextInt(10)))

		// When
		val bos = ByteArrayOutputStream()
		val gos = GribOutputStream(bos)
		expected.writeTo(gos)

		// Then
		val byteArray = bos.toByteArray()
		val gis = GribInputStream(ByteArrayInputStream(byteArray))
		val actual = Grib2RecordGDS.readFromStream(gis)

		assertNotNull(expected)
		assertEquals(expected, actual)
		assertEquals(72 + expected.additionalPoints.size, expected.length)
		assertEquals(72 + expected.additionalPoints.size, actual.length)
		assertEquals(3, expected.number)
		assertEquals(3, actual.number)
	}
}
