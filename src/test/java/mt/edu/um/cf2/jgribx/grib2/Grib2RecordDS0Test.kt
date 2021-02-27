package mt.edu.um.cf2.jgribx.grib2

import mt.edu.um.cf2.jgribx.GribInputStream
import mt.edu.um.cf2.jgribx.GribOutputStream
import mt.edu.um.cf2.jgribx.byteSpace
import org.junit.Assert
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.math.pow
import kotlin.random.Random

class Grib2RecordDS0Test {
	@Test
	fun testWriting() {
		// Given
		val numberOfDataPoints = Random.nextInt(10, 20)
		val data = generateData(numberOfDataPoints)
		val binaryScaleFactor = 0
		val decimalScaleFactor = 1
		val refValue = (data.minOrNull() ?: 0.0f) * 10.0f.pow(decimalScaleFactor)
		val nBits = 10
		val gds = Grib2RecordGDSLatLon(
				Random.nextInt(1.byteSpace()),
				numberOfDataPoints,
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
		val drs = Grib2RecordDRS0(
				Random.nextInt(5, 1000),
				refValue,
				binaryScaleFactor,
				decimalScaleFactor,
				nBits,
				Random.nextInt(1.byteSpace()))
		val bms = Grib2RecordBMS(Grib2RecordBMS.MISSING)

		val expected = Grib2RecordDS0(
				gds, drs, bms, data)

		// When
		val bos = ByteArrayOutputStream()
		val gos = GribOutputStream(bos)
		expected.writeTo(gos)

		// Then
		val byteArray = bos.toByteArray()
		val gis = GribInputStream(ByteArrayInputStream(byteArray))
		val actual = Grib2RecordDS.readFromStream(gis, gds, drs, bms)

		Assert.assertNotNull(expected)
		Assert.assertEquals(expected, actual)
		Assert.assertEquals(5 + expected.data.size, expected.length)
		Assert.assertEquals(5 + actual.data.size, actual.length)
		Assert.assertEquals(7, expected.number)
		Assert.assertEquals(7, actual.number)
		Assert.assertNotNull(expected.data)
		Assert.assertNotNull(actual.data)
	}

	private fun generateData(numberOfDataPoints: Int, nBits: Int): ByteArray {
		val bos = ByteArrayOutputStream()
		val gos = GribOutputStream(bos)
		repeat(numberOfDataPoints) {
			gos.writeUBits(Random.nextInt(), nBits)
		}
		gos.fillByte()
		return bos.toByteArray()
	}

	private fun generateData(numberOfDataPoints: Int) = (0 until numberOfDataPoints)
			.map { Random.nextInt(0, 1000).toFloat() / 10.0f }
			.toFloatArray()
}
