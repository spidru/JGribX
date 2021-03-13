package mt.edu.um.cf2.jgribx

import mt.edu.um.cf2.jgribx.grib2.*
import org.junit.Assert.assertEquals
import org.junit.BeforeClass
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.*

class GribFileTest {
	companion object {
		@BeforeClass
		@JvmStatic
		fun setUpBeforeClass() {
			Logger.mode = Logger.LoggingMode.CONSOLE
			JGribX.setLoggingLevel(Logger.DEBUG)
		}
	}

	@Test
	fun assertBinaryRepresentation() {
		val url = GribTest::class.java.getResource("/nomads-1p00-f003-24h-s12.grb2")
		val inputStream = GribInputStream(url.openStream())
		var count = 0

		while (inputStream.available() > 0) {
			GribRecordIS.seekNext(inputStream)
			val indicatorSection = try {
				GribRecordIS.readFromStream(inputStream)
			} catch (e: NoValidGribException) {
				break
			}
			var messageLength = indicatorSection.messageLength - indicatorSection.length

			val discipline: ProductDiscipline = indicatorSection.discipline!!
			var referenceTime: Calendar? = null
			var gds: Grib2RecordGDS? = null
			var drs: Grib2RecordDRS? = null
			var bms: Grib2RecordBMS? = null


			while (messageLength > 4) {
				if (messageLength == 4L) break

				inputStream.mark(5)
				val sectionLength = inputStream.readUInt(4)
				val sectionNumber = inputStream.readUInt(1)
				inputStream.resetBitCounter()
				inputStream.reset()
				val expected = ByteArray(sectionLength)
				val actual: ByteArray
				inputStream.read(expected)

				GribInputStream(ByteArrayInputStream(expected)).use { gis ->
					val section = when (sectionNumber) {
						1 -> Grib2RecordIDS.readFromStream(gis).also { referenceTime = it.referenceTime }
						2 -> Grib2RecordLUS.readFromStream(gis)
						3 -> Grib2RecordGDS.readFromStream(gis).also { gds = it }
						4 -> Grib2RecordPDS.readFromStream(gis, discipline, referenceTime!!)
						5 -> Grib2RecordDRS.readFromStream(gis).also { drs = it }
						6 -> Grib2RecordBMS.readFromStream(gis).also { bms = it }
						7 -> Grib2RecordDS.readFromStream(gis, gds!!, drs!!, bms!!)
						else -> null
					}
					if (section != null) ByteArrayOutputStream().use { bos ->
						try {
							GribOutputStream(bos).use { gos ->
								section.writeTo(gos)
								actual = bos.toByteArray()
							}
							assertContentEquals("Message ${count}: ${section::class.simpleName}", expected, actual)
						} catch (ignore: NotImplementedError) {
							// ignore
						}
					}
				}

				messageLength -= sectionLength.toLong()
			}
			GribRecordES.readFromStream(inputStream)
			count++
		}
		assertEquals(18, count)
	}
}
