package mt.edu.um.cf2.jgribx.grib2

import mt.edu.um.cf2.jgribx.GribInputStream
import mt.edu.um.cf2.jgribx.GribOutputStream
import mt.edu.um.cf2.jgribx.byteSpace
import org.junit.Assert.*
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.random.Random

class Grib2RecordIDSTest {
	@Test
	fun testWritingWithoutReserved() {
		// Given
		val expected = Grib2RecordIDS(
				Random.nextInt(2.byteSpace()),
				Random.nextInt(2.byteSpace()),
				Random.nextInt(1.byteSpace()),
				Random.nextInt(1.byteSpace()),
				Random.nextInt(1.byteSpace()),
				GregorianCalendar(
						Random.nextInt(1970, 2050),
						Random.nextInt(0, 12),
						Random.nextInt(1, 28),
						Random.nextInt(0, 24),
						Random.nextInt(0, 60),
						Random.nextInt(0, 60))
						.also { it.timeZone = TimeZone.getTimeZone("UTC") },
				Random.nextInt(1.byteSpace()),
				Random.nextInt(1.byteSpace()),
				null)

		// When
		val bos = ByteArrayOutputStream()
		val gos = GribOutputStream(bos)
		expected.writeTo(gos)

		// Then
		val byteArray = bos.toByteArray()
		val gis = GribInputStream(ByteArrayInputStream(byteArray))
		val actual = Grib2RecordIDS.readFromStream(gis)

		assertNotNull(expected)
		assertEquals(expected, actual)
		assertEquals(21, expected.length)
		assertEquals(21, actual.length)
		assertEquals(1, expected.number)
		assertEquals(1, actual.number)
	}

	@Test
	fun testWritingWithReserved() {
		// Given
		val expected = Grib2RecordIDS(
				Random.nextInt(2.byteSpace()),
				Random.nextInt(2.byteSpace()),
				Random.nextInt(1.byteSpace()),
				Random.nextInt(1.byteSpace()),
				Random.nextInt(1.byteSpace()),
				GregorianCalendar(
						Random.nextInt(1970, 2050),
						Random.nextInt(0, 12),
						Random.nextInt(1, 28),
						Random.nextInt(0, 24),
						Random.nextInt(0, 60),
						Random.nextInt(0, 60))
						.also { it.timeZone = TimeZone.getTimeZone("UTC") },
				Random.nextInt(1.byteSpace()),
				Random.nextInt(1.byteSpace()),
				Random.nextBytes(Random.nextInt(100)))

		// When
		val bos = ByteArrayOutputStream()
		val gos = GribOutputStream(bos)
		expected.writeTo(gos)

		// Then
		val byteArray = bos.toByteArray()
		val gis = GribInputStream(ByteArrayInputStream(byteArray))
		val actual = Grib2RecordIDS.readFromStream(gis, readEntire = true)

		assertNotNull(expected)
		assertEquals(expected, actual)
		assertEquals(21 + expected.reserved!!.size, expected.length)
		assertEquals(21 + actual.reserved!!.size, actual.length)
		assertEquals(1, expected.number)
		assertEquals(1, actual.number)
		assertNotNull(expected.reserved)
		assertNotNull(actual.reserved)
	}

	@Test
	fun testWritingWithReservedWithoutReadingEntireSection() {
		// Given
		val expected = Grib2RecordIDS(
				Random.nextInt(2.byteSpace()),
				Random.nextInt(2.byteSpace()),
				Random.nextInt(1.byteSpace()),
				Random.nextInt(1.byteSpace()),
				Random.nextInt(1.byteSpace()),
				GregorianCalendar(
						Random.nextInt(1970, 2050),
						Random.nextInt(0, 12),
						Random.nextInt(1, 28),
						Random.nextInt(0, 24),
						Random.nextInt(0, 60),
						Random.nextInt(0, 60))
						.also { it.timeZone = TimeZone.getTimeZone("UTC") },
				Random.nextInt(1.byteSpace()),
				Random.nextInt(1.byteSpace()),
				Random.nextBytes(Random.nextInt(100)))

		// When
		val bos = ByteArrayOutputStream()
		val gos = GribOutputStream(bos)
		expected.writeTo(gos)

		// Then
		val byteArray = bos.toByteArray()
		val gis = GribInputStream(ByteArrayInputStream(byteArray))
		val actual = Grib2RecordIDS.readFromStream(gis, readEntire = false)

		assertNotNull(expected)
		assertNotEquals(expected, actual)
		assertEquals(21 + expected.reserved!!.size, expected.length)
		assertEquals(21, actual.length)
		assertEquals(1, expected.number)
		assertEquals(1, actual.number)
		assertEquals(expected.centreId, actual.centreId)
		assertEquals(expected.origSubCentreId, actual.origSubCentreId)
		assertEquals(expected.masterTableVersion, actual.masterTableVersion)
		assertEquals(expected.localTableVersion, actual.localTableVersion)
		assertEquals(expected.refTimeSig, actual.refTimeSig)
		assertEquals(expected.referenceTime, actual.referenceTime)
		assertEquals(expected.dataProdStatus, actual.dataProdStatus)
		assertEquals(expected.dataType, actual.dataType)
		assertNotNull(expected.reserved)
		assertNull(actual.reserved)
	}
}
