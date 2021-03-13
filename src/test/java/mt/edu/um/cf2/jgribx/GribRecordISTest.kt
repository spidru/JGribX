package mt.edu.um.cf2.jgribx

import mt.edu.um.cf2.jgribx.grib2.ProductDiscipline
import org.junit.Assert.assertEquals
import org.junit.BeforeClass
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.random.Random

class GribRecordISTest {
	companion object {
		@BeforeClass
		@JvmStatic
		fun setUpBeforeClass() {
			Logger.mode = Logger.LoggingMode.CONSOLE
			JGribX.setLoggingLevel(Logger.DEBUG)
		}
	}

	@Test
	fun testWritingGRIB1() {
		// Given
		val expected = GribRecordIS(gribEdition = 1, discipline = null, recordLength = Random.nextLong(1000, 1000000))

		// When
		val bos = ByteArrayOutputStream()
		val gos = GribOutputStream(bos)
		expected.writeTo(gos)

		// Then
		val gis = GribInputStream(ByteArrayInputStream(bos.toByteArray()))
		val actual = GribRecordIS.readFromStream(gis)

		assertEquals(expected, actual)
	}

	@Test
	fun testWritingGRIB2() {
		// Given
		val discipline = ProductDiscipline.VALUES[0]
		val expected = GribRecordIS(gribEdition = 2, discipline = discipline, recordLength = Random.nextLong(1000, 1000000))

		// When
		val bos = ByteArrayOutputStream()
		val gos = GribOutputStream(bos)
		expected.writeTo(gos)

		// Then
		val byteArray = bos.toByteArray()
		val gis = GribInputStream(ByteArrayInputStream(byteArray))
		val actual = GribRecordIS.readFromStream(gis)

		assertEquals(expected, actual)
		assertEquals(0x00.toByte(), byteArray[4]) // Reserved octet 5
		assertEquals(0x00.toByte(), byteArray[5]) // Reserved octet 6
	}
}
