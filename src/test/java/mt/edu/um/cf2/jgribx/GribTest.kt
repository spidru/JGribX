/*
 * Copyright (c) 2010-2020 Poterion. All rights reserved.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package mt.edu.um.cf2.jgribx

import mt.edu.um.cf2.jgribx.JGribX.setLoggingLevel
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
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
		Assert.assertTrue("Version string format is valid: $version", match.find())
	}

	@Test
	@Throws(NotSupportedException::class, IOException::class, NoValidGribException::class)
	fun testGrib1Gfs3() {
		// Define expected data
		val url = GribTest::class.java.getResource("/CF2_20150706_092531.grb")
		println("Path to file: $url")
		val gribFile = GribFile(url.openStream())
		Assert.assertEquals("Records read successfully", 304L, gribFile.recordCount.toLong())
		Assert.assertEquals("GRIB edition", 1L, gribFile.edition.toLong())
		Assert.assertArrayEquals("Weather centres", intArrayOf(7), gribFile.centreIDs.toIntArray())
		Assert.assertArrayEquals("Generating processes", intArrayOf(81, 96), gribFile.processIDs)
	}

	@Test
	fun testGrib1OpenSkiron() {
		val refTimes: MutableList<Calendar> = ArrayList()
		refTimes.add(GregorianCalendar(2021, Calendar.FEBRUARY, 27, 0, 0, 0)
				.apply { timeZone = TimeZone.getTimeZone("UTC") })
		val url = GribTest::class.java.getResource("/Azov_SKIRON_270221.grb")
		val file = GribFile(url.openStream())
		Assert.assertEquals("GRIB edition", 1L, file.edition.toLong())
		Assert.assertEquals("Reference time(s)", file.referenceTimes, refTimes)
		Assert.assertArrayEquals("Weather centres", intArrayOf(7), file.centreIDs.toIntArray())
		Assert.assertArrayEquals("Generating processes", intArrayOf(31), file.processIDs)
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
		Assert.assertEquals("GRIB edition", 2L, file.edition.toLong())
		Assert.assertEquals("Reference time(s)", file.referenceTimes, refTimes)
		Assert.assertArrayEquals("Weather centres", intArrayOf(7), file.centreIDs.toIntArray())
		Assert.assertArrayEquals("Generating processes", intArrayOf(81), file.processIDs)
	}

	@Test
	fun testGrib2OpenSkironEuNest() {
		val refTimes: MutableList<Calendar> = ArrayList()
		refTimes.add(GregorianCalendar(2021, Calendar.FEBRUARY, 27, 0, 0, 0)
				.apply { timeZone = TimeZone.getTimeZone("UTC") })
		val url = GribTest::class.java.getResource("/North_Sea_NE_ICON_EU_EWAM_20210227-00.grb2")
		val file = GribFile(url.openStream())
		Assert.assertEquals("GRIB edition", 2L, file.edition.toLong())
		Assert.assertEquals("Reference time(s)", file.referenceTimes, refTimes)
		Assert.assertArrayEquals("Weather centres", intArrayOf(78), file.centreIDs.toIntArray())
		Assert.assertArrayEquals("Generating processes", intArrayOf(2), file.processIDs)
	}

	@Test
	fun testGrib2OpenSkironIconD2() {
		val refTimes: MutableList<Calendar> = ArrayList()
		refTimes.add(GregorianCalendar(2021, Calendar.FEBRUARY, 27, 0, 0, 0)
				.apply { timeZone = TimeZone.getTimeZone("UTC") })
		val url = GribTest::class.java.getResource("/Copenhagen_ICON-D2_EWAM_20210227-00.grb2")
		val file = GribFile(url.openStream())
		Assert.assertEquals("GRIB edition", 2L, file.edition.toLong())
		Assert.assertEquals("Reference time(s)", file.referenceTimes, refTimes)
		Assert.assertArrayEquals("Weather centres", intArrayOf(78), file.centreIDs.toIntArray())
		Assert.assertArrayEquals("Generating processes", intArrayOf(11), file.processIDs)
	}
}
