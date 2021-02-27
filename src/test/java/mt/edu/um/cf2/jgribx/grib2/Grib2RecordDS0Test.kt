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
		val gridNi = Random.nextInt(5, 10)
		val gridNj = Random.nextInt(5, 10)
		val numberOfDataPoints = gridNi * gridNj
		val data = generateData(numberOfDataPoints)
		val binaryScaleFactor = 0
		val decimalScaleFactor = 1
		val refValue = (data.minOrNull() ?: 0.0f) * 10.0f.pow(decimalScaleFactor)
		val nBits = 10

		val lat1 = Random.nextInt(-85_000_000, 85_000_000) / 1.0e6
		val lng1 = Random.nextInt(-180_000_000, 180_000_000) / 1.0e6
		val gridDj = (if (lat1 <= 0) 1 else -1) * Random.nextInt(1_000) * 1_000 / 1.0e6
		val gridDi = (if (lng1 <= 0) 1 else -1) * Random.nextInt(1_000) * 1_000 / 1.0e6
		val lat2 = lat1 + gridDj * gridNj
		val lng2 = lng1 + gridDi * gridNi

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
				gridNi,
				gridNj,
				0,
				Random.nextInt(4.byteSpace()),
				lat1,
				lng1,
				0x10 or 0x20,
				lat2,
				lng2,
				gridDi,
				gridDj,
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
		Assert.assertEquals(5 + expected.values.size, expected.length)
		Assert.assertEquals(5 + actual.values.size, actual.length)
		Assert.assertEquals(7, expected.number)
		Assert.assertEquals(7, actual.number)
		Assert.assertNotNull(expected.values)
		Assert.assertNotNull(actual.values)
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
