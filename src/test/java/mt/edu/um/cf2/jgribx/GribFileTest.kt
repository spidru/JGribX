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

	@Test
	fun testCutOutGrib2WithDRS3ToDRS0Conversion() {
		val url = GribTest::class.java.getResource("/nomads-1p00-f003-24h-s12.grb2")
		val bos = ByteArrayOutputStream()
		val gos = GribOutputStream(bos)

		val file1 = GribFile(url.openStream())
		file1.records
				.asSequence()
				.filterIsInstance<Grib2Record>()

				// Given
				.onEach { println("(A) ${it.dump()}") }
				.onEach { record ->
					record.productDefinition.dataSections.forEach { ds ->
						assertEquals(Grib2RecordDRS3::class, ds.drs::class)
						assertEquals(Grib2RecordDS3::class, ds::class)
					}
				}
				.onEach { assertEquals(65160, it.gridDefinition.coords.size) }
				.onEach { assertEquals(65160, it.values.size) }
				.onEach { assertEquals(-90.0, it.gridDefinition.yCoords.minOrNull()) }
				.onEach { assertEquals(90.0, it.gridDefinition.yCoords.maxOrNull()) }
				.onEach { assertEquals(-180.0, it.gridDefinition.xCoords.minOrNull()) }
				.onEach { assertEquals(179.0, it.gridDefinition.xCoords.maxOrNull()) }

				// When
				.onEach { it.convertDataRepresentationTo(Grib2RecordDRS0::class) }

				// Then
				.onEach { record ->
					record.productDefinition.dataSections.forEach { ds ->
						assertEquals(Grib2RecordDRS0::class, ds.drs::class)
						assertEquals(Grib2RecordDS0::class, ds::class)
					}
				}
				.onEach { assertEquals(65160, it.gridDefinition.coords.size) }
				.onEach { assertEquals(65160, it.values.size) }
				.onEach { assertEquals(-90.0, it.gridDefinition.yCoords.minOrNull()) }
				.onEach { assertEquals(90.0, it.gridDefinition.yCoords.maxOrNull()) }
				.onEach { assertEquals(-180.0, it.gridDefinition.xCoords.minOrNull()) }
				.onEach { assertEquals(179.0, it.gridDefinition.xCoords.maxOrNull()) }
				.forEach { println("(B) ${it.dump()}") }
		file1.writeTo(gos)

		// Given
		val file2 = GribFile(GribInputStream(ByteArrayInputStream(bos.toByteArray())))
		file2.records
				.asSequence()
				.filterIsInstance<Grib2Record>()
				.onEach { assertEquals(65160, it.gridDefinition.coords.size) }
				.onEach { assertEquals(65160, it.values.size) }
				.onEach { assertEquals(-90.0, it.gridDefinition.yCoords.minOrNull()) }
				.onEach { assertEquals(90.0, it.gridDefinition.yCoords.maxOrNull()) }
				.onEach { assertEquals(-180.0, it.gridDefinition.xCoords.minOrNull()) }
				.onEach { assertEquals(179.0, it.gridDefinition.xCoords.maxOrNull()) }
				.forEach { println("(C) ${it.dump()}") }

		// When
		file2.cutOut(50.0, 20.0, 40.0, 10.0)

		// Then
		file2.records
				.asSequence()
				.filterIsInstance<Grib2Record>()
				.onEach { assertEquals(121, it.gridDefinition.coords.size) }
				.onEach { assertEquals(121, it.data.data.size) }
				.onEach { assertEquals(40.0, it.gridDefinition.yCoords.minOrNull()) }
				.onEach { assertEquals(50.0, it.gridDefinition.yCoords.maxOrNull()) }
				.onEach { assertEquals(10.0, it.gridDefinition.xCoords.minOrNull()) }
				.onEach { assertEquals(20.0, it.gridDefinition.xCoords.maxOrNull()) }
				.forEach { println("(D) ${it.dump()}") }
		bos.reset()
		file2.writeTo(gos)

		val file3 = GribFile(GribInputStream(ByteArrayInputStream(bos.toByteArray())))
		file3.records
				.asSequence()
				.filterIsInstance<Grib2Record>()
				.onEach { assertEquals(121, it.gridDefinition.coords.size) }
				.onEach { assertEquals(121, it.data.data.size) }
				.onEach { assertEquals(40.0, it.gridDefinition.yCoords.minOrNull()) }
				.onEach { assertEquals(50.0, it.gridDefinition.yCoords.maxOrNull()) }
				.onEach { assertEquals(10.0, it.gridDefinition.xCoords.minOrNull()) }
				.onEach { assertEquals(20.0, it.gridDefinition.xCoords.maxOrNull()) }
				.forEach { println("(E) ${it.dump()}") }
	}

	private fun Grib2Record.dump() = "${parameter}: coords=${gridDefinition.coords.size}, data=${values.size}"
}
