package mt.edu.um.cf2.jgribx.grib1

import mt.edu.um.cf2.jgribx.GribInputStream
import mt.edu.um.cf2.jgribx.GribOutputStream
import mt.edu.um.cf2.jgribx.bitSpace
import mt.edu.um.cf2.jgribx.byteSpace
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Ignore
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.random.Random

class Grib1RecordGDSTest {
	@Test
	fun testWritingLatLon() {
		// Given
		val expected = Grib1GDSLatLon(
				Random.nextInt(1.byteSpace()),
				Random.nextInt(1.byteSpace()),

				Random.nextInt(2.byteSpace()),
				Random.nextInt(2.byteSpace()),
				Random.nextInt(3.bitSpace()) / 1000.0,
				Random.nextInt(3.bitSpace()) / 1000.0,
				0x80,
				Random.nextInt(3.bitSpace()) / 1000.0,
				Random.nextInt(3.bitSpace()) / 1000.0,
				Random.nextInt(3.bitSpace()) / 1000.0,
				Random.nextInt(3.bitSpace()) / 1000.0,
				0x40)

		// When
		val bos = ByteArrayOutputStream()
		val gos = GribOutputStream(bos)
		expected.writeTo(gos)

		// Then
		val byteArray = bos.toByteArray()
		val gis = GribInputStream(ByteArrayInputStream(byteArray))
		val actual = Grib1RecordGDS.readFromStream(gis)

		assertNotNull(expected)
		assertEquals(expected, actual)
		assertEquals(32, expected.length)
		assertEquals(32, actual.length)
	}

	@Test
	fun testWritingRotatedLatLon() {
		// Given
		val expected = Grib1GDSRotatedLatLon(
				Random.nextInt(1.byteSpace()),
				Random.nextInt(1.byteSpace()),

				Random.nextInt(2.byteSpace()),
				Random.nextInt(2.byteSpace()),
				Random.nextInt(3.bitSpace()) / 1000.0,
				Random.nextInt(3.bitSpace()) / 1000.0,
				0x80,
				Random.nextInt(3.bitSpace()) / 1000.0,
				Random.nextInt(3.bitSpace()) / 1000.0,
				Random.nextInt(3.bitSpace()) / 1000.0,
				Random.nextInt(3.bitSpace()) / 1000.0,
				0x40,

				Random.nextInt(3.bitSpace()) / 1000.0,
				Random.nextInt(3.bitSpace()) / 1000.0,
				0f)

		// When
		val bos = ByteArrayOutputStream()
		val gos = GribOutputStream(bos)
		expected.writeTo(gos)

		// Then
		val byteArray = bos.toByteArray()
		val gis = GribInputStream(ByteArrayInputStream(byteArray))
		val actual = Grib1RecordGDS.readFromStream(gis)

		assertNotNull(expected)
		assertEquals(expected, actual)
		assertEquals(42, expected.length)
		assertEquals(42, actual.length)
	}

	@Test
	@Ignore
	fun testWritingPolarStereo() {
		// Given
		val expected = Grib1GDSLambert(
				Random.nextInt(1.byteSpace()),
				Random.nextInt(1.byteSpace()),

				Random.nextInt(2.byteSpace()),
				Random.nextInt(2.byteSpace()),
				Random.nextInt(3.bitSpace()) / 1000.0,
				Random.nextInt(3.bitSpace()) / 1000.0,
				0x80,
				Random.nextInt(3.bitSpace()) / 1000.0,
				Random.nextInt(3.bitSpace()) / 1000.0,
				Random.nextInt(3.bitSpace()) / 1000.0,
				Random.nextInt(1.byteSpace()),
				0x40,

				Random.nextInt(3.bitSpace()) / 1000.0,
				Random.nextInt(3.bitSpace()) / 1000.0,
				Random.nextInt(3.bitSpace()) / 1000.0,
				Random.nextInt(3.bitSpace()) / 1000.0)

		// When
		val bos = ByteArrayOutputStream()
		val gos = GribOutputStream(bos)
		expected.writeTo(gos)

		// Then
		val byteArray = bos.toByteArray()
		val gis = GribInputStream(ByteArrayInputStream(byteArray))
		val actual = Grib1RecordGDS.readFromStream(gis)

		assertNotNull(expected)
		assertEquals(expected, actual)
		assertEquals(42, expected.length)
		assertEquals(42, actual.length)
	}
}
