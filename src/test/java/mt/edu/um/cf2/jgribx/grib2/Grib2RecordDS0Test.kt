package mt.edu.um.cf2.jgribx.grib2

import mt.edu.um.cf2.jgribx.*
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.math.pow
import kotlin.random.Random

class Grib2RecordDS0Test {
	companion object {
		@BeforeClass
		@JvmStatic
		fun setUpBeforeClass() {
			Logger.mode = Logger.LoggingMode.CONSOLE
			JGribX.setLoggingLevel(Logger.DEBUG)
		}
	}

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
				Random.nextInt(1.bytesSpace()),
				numberOfDataPoints,
				Random.nextInt(1.bytesSpace()),
				Random.nextInt(1.bytesSpace()),

				Random.nextInt(1.bytesSpace()),
				Random.nextInt(1.bytesSpace()),
				Random.nextInt(4.bytesSpace()),
				Random.nextInt(1.bytesSpace()),
				Random.nextInt(4.bytesSpace()),
				Random.nextInt(1.bytesSpace()),
				Random.nextInt(4.bytesSpace()),
				Random.nextInt(4.bytesSpace()),
				Random.nextInt(4.bytesSpace()),
				0,
				Random.nextInt(4.bytesSpace()),
				Random.nextInt(4.bytesSpace()) / 1.0e6,
				Random.nextInt(4.bytesSpace()) / 1.0e6,
				0x10 or 0x20,
				Random.nextInt(4.bytesSpace()) / 1.0e6,
				Random.nextInt(4.bytesSpace()) / 1.0e6,
				Random.nextInt(4.bytesSpace()) / 1.0e6,
				Random.nextInt(4.bytesSpace()) / 1.0e6,
				Grib2RecordGDS.ScanMode(0x40),
				Random.nextBytes(Random.nextInt(10)))
		val drs = Grib2RecordDRS0(
				Random.nextInt(5, 1000),
				refValue,
				binaryScaleFactor,
				decimalScaleFactor,
				nBits,
				Random.nextInt(1.bytesSpace()))
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
