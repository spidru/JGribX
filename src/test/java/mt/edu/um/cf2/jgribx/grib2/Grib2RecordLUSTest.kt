package mt.edu.um.cf2.jgribx.grib2

import mt.edu.um.cf2.jgribx.GribInputStream
import mt.edu.um.cf2.jgribx.GribOutputStream
import org.junit.Assert.*
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.random.Random

class Grib2RecordLUSTest {
	@Test
	fun testWriting() {
		// Given
		val expected = Grib2RecordLUS(Random.nextBytes(Random.nextInt(100)))

		// When
		val bos = ByteArrayOutputStream()
		val gos = GribOutputStream(bos)
		expected.writeTo(gos)

		// Then
		val byteArray = bos.toByteArray()
		val gis = GribInputStream(ByteArrayInputStream(byteArray))
		val actual = Grib2RecordLUS.readFromStream(gis, readEntire = true)

		assertNotNull(expected)
		assertEquals(expected, actual)
		assertEquals(5 + expected.data!!.size, expected.length)
		assertEquals(5 + actual.data!!.size, actual.length)
		assertEquals(2, expected.number)
		assertEquals(2, actual.number)
		assertNotNull(expected.data)
		assertNotNull(actual.data)
	}

	@Test
	fun testWritingWithoutReadingEntireSection() {
		// Given
		val expected = Grib2RecordLUS(Random.nextBytes(Random.nextInt(100)))

		// When
		val bos = ByteArrayOutputStream()
		val gos = GribOutputStream(bos)
		expected.writeTo(gos)

		// Then
		val byteArray = bos.toByteArray()
		val gis = GribInputStream(ByteArrayInputStream(byteArray))
		val actual = Grib2RecordLUS.readFromStream(gis, readEntire = false)

		assertNotNull(expected)
		assertNotEquals(expected, actual)
		assertEquals(5 + expected.data!!.size, expected.length)
		assertEquals(5, actual.length)
		assertEquals(2, expected.number)
		assertEquals(2, actual.number)
		assertNotNull(expected.data)
		assertNull(actual.data)
	}
}
