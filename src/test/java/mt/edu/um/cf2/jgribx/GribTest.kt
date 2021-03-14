package mt.edu.um.cf2.jgribx

import mt.edu.um.cf2.jgribx.JGribX.setLoggingLevel
import org.junit.Assert.*
import org.junit.BeforeClass
import org.junit.Test
import java.io.IOException
import java.util.*
import java.util.regex.Pattern

class GribTest {
	companion object {
		@BeforeClass
		@JvmStatic
		fun setUpBeforeClass() {
			Logger.mode = Logger.LoggingMode.CONSOLE
			setLoggingLevel(Logger.DEBUG)
		}
	}

	@Test
	fun testVersion() {
		// Check that version string format conforms to SemVer (taken from: https://semver.org/)
		val pattern = Pattern
				.compile("^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)(?:-((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?$")
		val version: String = JGribX.VERSION
		val match = pattern.matcher(version)
		assertTrue("Version string format is valid: $version", match.find())
	}

	@Test
	@Throws(NotSupportedException::class, IOException::class, NoValidGribException::class)
	fun testGrib1Gfs3() {
		// Define expected data
		val url = GribTest::class.java.getResource("/CF2_20150706_092531.grb")
		println("Path to file: $url")
		val gribFile = GribFile(url.openStream())
		assertEquals("Records read successfully", 304L, gribFile.recordCount.toLong())
		assertEquals("GRIB edition", 1L, gribFile.edition.toLong())
		assertArrayEquals("Weather centres", intArrayOf(7), gribFile.centreIDs.toIntArray())
		assertArrayEquals("Generating processes", intArrayOf(81, 96), gribFile.processIDs)

		// TODO assertCoordinatesWithGoldStandard(url, gribFile)
		assertDataWithGoldStandard(url, gribFile)
	}

	@Test
	fun testGrib1OpenSkiron() {
		val refTimes: MutableList<Calendar> = ArrayList()
		refTimes.add(GregorianCalendar(2021, Calendar.FEBRUARY, 27, 0, 0, 0)
				.apply { timeZone = TimeZone.getTimeZone("UTC") })
		val url = GribTest::class.java.getResource("/Azov_SKIRON_270221.grb")
		val file = GribFile(url.openStream())
		assertEquals("GRIB edition", 1L, file.edition.toLong())
		assertEquals("Reference time(s)", file.referenceTimes, refTimes)
		assertArrayEquals("Weather centres", intArrayOf(7), file.centreIDs.toIntArray())
		assertArrayEquals("Generating processes", intArrayOf(31), file.processIDs)

		// TODO assertCoordinatesWithGoldStandard(url, file)
		assertDataWithGoldStandard(url, file, skipGribRecords = listOf(438, 445)) // FIXME
	}

	@Test
	@Throws(IOException::class, NoValidGribException::class, NotSupportedException::class)
	fun testGrib2Gfs3() {
		/* TODO
		 * Unsupported parameter: D:LAND_SURFACE C:4 N:2
		 * Unsupported parameter: D:LAND_SURFACE C:0 N:2
		 * Unsupported parameter: D:LAND_SURFACE C:0 N:192
		 * Unsupported parameter: D:LAND_SURFACE C:0 N:201
		 * Unsupported parameter: D:LAND_SURFACE C:3 N:203
		 * Unsupported parameter: D:METEOROLOGICAL C:6 N:201
		 * Unsupported parameter: D:METEOROLOGICAL C:7 N:192
		 * Unsupported parameter: D:METEOROLOGICAL C:7 N:6
		 * Unsupported parameter: D:METEOROLOGICAL C:7 N:7
		 * Skipping GRIB record 274 (Record does not have a valid GRIB header)
		 * Unsupported level of type 104
		 * Skipping GRIB record 337 (Record does not have a valid GRIB header)
		 * Unsupported parameter: D:OCEANOGRAPHIC C:2 N:0
		 * Unsupported level of type 109
		 */

		// Define expected data
		val refTimes: MutableList<Calendar> = ArrayList()
		refTimes.add(GregorianCalendar(2017, Calendar.MAY, 12, 0, 0, 0)
				.apply { timeZone = TimeZone.getTimeZone("UTC") })
		val url = GribTest::class.java.getResource("/gfsanl_3_20170512_0000_000.grb2")
		val file = GribFile(url.openStream())
		assertEquals("GRIB edition", 2L, file.edition.toLong())
		assertEquals("Reference time(s)", file.referenceTimes, refTimes)
		assertArrayEquals("Weather centres", intArrayOf(7), file.centreIDs.toIntArray())
		assertArrayEquals("Generating processes", intArrayOf(81), file.processIDs)

		assertCoordinatesWithGoldStandard(url, file)
		assertDataWithGoldStandard(url, file)
	}

	@Test
	fun testGrib2OpenSkironEuNest() {
		val refTimes: MutableList<Calendar> = ArrayList()
		refTimes.add(GregorianCalendar(2021, Calendar.FEBRUARY, 27, 0, 0, 0)
				.apply { timeZone = TimeZone.getTimeZone("UTC") })
		val url = GribTest::class.java.getResource("/North_Sea_NE_ICON_EU_EWAM_20210227-00.grb2")
		// TODO JK: Data Representation type 40 not supported yet
		val ignoredIndices = readIgnoredRecordIndices("/North_Sea_NE_ICON_EU_EWAM_20210227-00.grb2.ignore")

		val file = GribFile(url.openStream())
		assertEquals(ignoredIndices.size, file.messagesSkippedCount)
		assertEquals("GRIB edition", 2L, file.edition.toLong())
		assertEquals("Reference time(s)", file.referenceTimes, refTimes)
		assertArrayEquals("Weather centres", intArrayOf(78), file.centreIDs.toIntArray())
		assertArrayEquals("Generating processes", intArrayOf(2), file.processIDs)

		assertCoordinatesWithGoldStandard(url, file)
		assertDataWithGoldStandard(url, file, skipWGribRecords = ignoredIndices)
	}

	@Test
	fun testGrib2OpenSkironIconD2() {
		val refTimes: MutableList<Calendar> = ArrayList()
		refTimes.add(GregorianCalendar(2021, Calendar.FEBRUARY, 27, 0, 0, 0)
				.apply { timeZone = TimeZone.getTimeZone("UTC") })
		val url = GribTest::class.java.getResource("/Copenhagen_ICON-D2_EWAM_20210227-00.grb2")
		// TODO JK: Data Representation type 40 not supported yet
		val ignoredIndices = readIgnoredRecordIndices("/Copenhagen_ICON-D2_EWAM_20210227-00.grb2.ignore")

		val file = GribFile(url.openStream())
		//assertEquals(ignoredIndices.size, file.messagesSkippedCount)
		assertEquals("GRIB edition", 2L, file.edition.toLong())
		assertEquals("Reference time(s)", file.referenceTimes, refTimes)
		assertArrayEquals("Weather centres", intArrayOf(78), file.centreIDs.toIntArray())
		assertArrayEquals("Generating processes", intArrayOf(11), file.processIDs)

		assertCoordinatesWithGoldStandard(url, file)
		assertDataWithGoldStandard(url, file, skipWGribRecords = ignoredIndices)
	}
}
