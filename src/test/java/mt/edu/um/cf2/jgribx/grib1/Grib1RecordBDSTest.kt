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
import kotlin.math.ceil
import kotlin.math.pow
import kotlin.random.Random

class Grib1RecordBDSTest {
	@Test
	fun testWriting() {
		// Given
		val numberOfDataPoints = Random.nextInt(10, 11)
		val data = generateData(numberOfDataPoints)
		val binaryScaleFactor = 0
		val decimalScaleFactor = 1
		val refValue = (data.minOrNull() ?: 0.0f) * 10.0f.pow(decimalScaleFactor)
		val nBits = 10
		val referenceTime = GregorianCalendar(
				Random.nextInt(1970, 2050),
				Random.nextInt(0, 12),
				Random.nextInt(1, 28),
				Random.nextInt(0, 24),
				Random.nextInt(0, 60),
				0)
				.also { it.timeZone = TimeZone.getTimeZone("UTC") }
		val pds = Grib1RecordPDS(
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
				decimalScaleFactor,
				0)
		val gds = Grib1GDSLatLon(
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
		//val bits = 0
		//val bms = Grib1RecordBMS(
		//		8 - (bits % 8),
		//		0,
		//		(0 until bits).map { Random.nextBoolean() }.toBooleanArray())
		val dataFlag = 0
		val expected = Grib1RecordBDS(pds, gds, null,
				dataFlag,
				binaryScaleFactor,
				refValue,
				nBits,
				data)

		// When
		val bos = ByteArrayOutputStream()
		val gos = GribOutputStream(bos)
		expected.writeTo(gos)

		// Then
		val byteArray = bos.toByteArray()
		val gis = GribInputStream(ByteArrayInputStream(byteArray))
		val actual = Grib1RecordBDS.readFromStream(gis, pds, gds, null)

		assertNotNull(expected)
		assertEquals(expected, actual)
		assertEquals(11 + ceil(data.size * nBits / 8.0).toInt() + (dataFlag and 15) / 8, expected.length)
		assertEquals(11 + ceil(data.size * nBits / 8.0).toInt() + (dataFlag and 15) / 8, actual.length)
	}

	private fun generateData(numberOfDataPoints: Int) = (0 until numberOfDataPoints)
			.map { Random.nextInt(0, 100).toFloat() / 10.0f }
			.toFloatArray()
}
