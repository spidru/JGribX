package mt.edu.um.cf2.jgribx

import mt.edu.um.cf2.jgribx.JGribX.setLoggingLevel
import org.junit.Assert.*
import org.junit.BeforeClass
import org.junit.Test
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.net.URISyntaxException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class GribTest {
	companion object {
		@BeforeClass
		@JvmStatic
		fun setUpBeforeClass() {
			// Prepare format for reference times
			val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm:ss 'UTC'")
			sdf.timeZone = TimeZone.getTimeZone("UTC")

			// Configure logging
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

		// Compare values in each record against a "gold standard" (wgrib)
		for (r in 0 until gribFile.recordCount) {
			// Get values from GRIB file using JGribX
			val record = gribFile.records[r]
			val obtainedValues: FloatArray = record.values
			var gribFilepath = ""
			try {
				gribFilepath = File(url.toURI()).absolutePath
			} catch (e: URISyntaxException) {
				e.printStackTrace()
			}
			val pb = ProcessBuilder("wgrib", gribFilepath, "-d", "${(r + 1)}", "-text")
			val cmd = pb.command().joinToString(" ")
			println("Executing: ${cmd}")
			try {
				val process = pb.start()
				val exited = process.waitFor(2, TimeUnit.SECONDS)
				assertTrue("Process exited", exited)
				assertEquals("Process exited with code ${process.exitValue()}", 0, process.exitValue())
			} catch (e: IOException) {
				fail("Exception: ${e.message}")
			} catch (e: InterruptedException) {
				fail("Exception: ${e.message}")
			}

			// Read dump file
			try {
				BufferedReader(FileReader("dump")).use { reader ->
					var line = ""
					// Skip first line
					reader.readLine()
					var i = 0
					while (reader.readLine()?.also { line = it } != null) {
						var tolerance = 0.1
						val expectedValue = line.toFloat()

						/* WORKAROUND
						 * It seems that wgrib occasionally outputs values as integers for some reason.
						 * To avoid false positive assertions, we increase the tolerance to 0.5
						 */
						val expectedValueAsInt = expectedValue.toInt()
						if (expectedValue - expectedValueAsInt == 0f) {
							tolerance = 0.5
						}
						assertEquals(String.format("Record %d entry %d", r, i),
								expectedValue.toDouble(), obtainedValues[i].toDouble(), tolerance)
						i++
					}
				}
			} catch (e: IOException) {
				fail("Exception: ${e.message}")
			}
		}
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
	}

	@Test
	fun testGrib2OpenSkironEuNest() {
		val refTimes: MutableList<Calendar> = ArrayList()
		refTimes.add(GregorianCalendar(2021, Calendar.FEBRUARY, 27, 0, 0, 0)
				.apply { timeZone = TimeZone.getTimeZone("UTC") })
		val url = GribTest::class.java.getResource("/North_Sea_NE_ICON_EU_EWAM_20210227-00.grb2")
		val file = GribFile(url.openStream())
		assertEquals("GRIB edition", 2L, file.edition.toLong())
		assertEquals("Reference time(s)", file.referenceTimes, refTimes)
		assertArrayEquals("Weather centres", intArrayOf(78), file.centreIDs.toIntArray())
		assertArrayEquals("Generating processes", intArrayOf(2), file.processIDs)
	}

	@Test
	fun testGrib2OpenSkironIconD2() {
		val refTimes: MutableList<Calendar> = ArrayList()
		refTimes.add(GregorianCalendar(2021, Calendar.FEBRUARY, 27, 0, 0, 0)
				.apply { timeZone = TimeZone.getTimeZone("UTC") })
		val url = GribTest::class.java.getResource("/Copenhagen_ICON-D2_EWAM_20210227-00.grb2")
		val file = GribFile(url.openStream())
		assertEquals("GRIB edition", 2L, file.edition.toLong())
		assertEquals("Reference time(s)", file.referenceTimes, refTimes)
		assertArrayEquals("Weather centres", intArrayOf(78), file.centreIDs.toIntArray())
		assertArrayEquals("Generating processes", intArrayOf(11), file.processIDs)
	}
}
