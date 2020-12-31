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
/*
 * ============================================================================
 * JGribX
 * ============================================================================
 * Written by Andrew Spiteri <andrew.spiteri@um.edu.mt>
 * Adapted from JGRIB: http://jgrib.sourceforge.net/
 * 
 * Licensed under MIT: https://github.com/spidru/JGribX/blob/master/LICENSE
 * ============================================================================
 */
package mt.edu.um.cf2.jgribx

import mt.edu.um.cf2.jgribx.GribCodes.getCentreName
import mt.edu.um.cf2.jgribx.GribCodes.getProcessName
import mt.edu.um.cf2.jgribx.Logger.level
import java.io.FileNotFoundException
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * This example demonstrates how a quick summary of the contents of the GRIB
 * file can be obtained.
 */
object GribExample1 {
	@JvmStatic
	fun main(args: Array<String>) {
		println("JGribX version " + JGribX.VERSION)
		Logger.mode = Logger.LoggingMode.CONSOLE
		level = Logger.INFO
		/**
		 * type: GRIB1 NCEP GFS
		 * issues:
		 * - Table 3 level 220 is not implemented yet
		 * - Unsupported Parameter 219 in Table 130
		 * - Unsupported Parameter 220 in Table 130
		 * - Unsupported Parameter 191 in Table 133
		 */
		// String gribFilename = "test/gfsanl_3_20160324_0600_000.grb";
		/**
		 * type: GRIB1 NCEP GFS
		 * issues:
		 * - Table 3 level 220 is not implemented yet
		 * - Unsupported Parameter 219 in Table 130
		 * - Unsupported Parameter 220 in Table 130
		 * - Unsupported Parameter 191 in Table 133
		 */
		// String gribFilename = "test/gfs_3_20170101_0000_000.grb";
		/**
		 * type: GRIB2 NCEP GFS
		 * issues:
		 * - BMS bitmap not yet supported
		 * - second surface not yet supported
		 */
		// String gribFilename = "test/gfsanl_3_20170512_0000_000.grb2";

		// String gribFilename = "test/200601010000.pgbh06.gdas.20051226-20051231.grb2";
		/**
		 * type: GRIB2 NCEP GFS
		 * issues:
		 * - Data Representation type 0 not supported
		 * - Missing Value Management is not supported
		 * - causes out of memory error
		 */
		// String gribFilename = "test/gfs_4_20171112_0000_024.grb2";

		// String gribFilename = "test/A_HWXE85ECEM210000_C_ECMF_20160721000000_24h_em_ws_850hPa_global_0p5deg_grib2.bin";     // testing files from ECMWF

		// String gribFilename = "cached_gfs_4_20180520_0000_000_m.grb2";
		val gribFilename = "gfsanl_3_20170512_0000_000.grb2"

		// Prepare format for reference times
		// SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm:ss 'UTC'")
		sdf.timeZone = TimeZone.getTimeZone("UTC")
		try {
			// System.out.println(GribExample1.class.getClassLoader().getResource(gribFilename));
			val gribFile = GribFile("src/test/resources/$gribFilename")

			// Get info
			val nRecords = gribFile.recordCount
			val refDates = gribFile.referenceTimes
			val forecastDates = gribFile.forecastTimes
			val params = gribFile.parameterCodes
			val centreIds = gribFile.centreIDs
			val processIds = gribFile.processIDs

			// Print out generic GRIB file info
			println("---------------------------------------")
			println("Reading file: $gribFilename")
			println("GRIB Edition: " + gribFile.edition)
			println("Records successfully read: " + nRecords + " of "
					+ (nRecords + gribFile.recordsSkippedCount))
			println("---------------------------------------")

			// Print out originating centre info
			print("Weather Centre(s): ")
			for (i in centreIds.indices) {
				print(centreIds[i]
						.toString() + " [" + getCentreName(centreIds[i]) + "]")
				if (i != centreIds.size - 1) print(",")
			}
			println()

			// Print out generating process info
			print("Generating Process(es): ")
			for (i in processIds.indices) {
				print(processIds[i]
						.toString() + " [" + getProcessName(processIds[i]) + "]")
				if (i != processIds.size - 1) print(",")
			}
			println()

			// Get forecast times
			println("Forecast Time(s): ")
			for (date in forecastDates) {
				println("\t" + sdf.format(date.time))
			}

			// Get reference time
			println("Reference Time: ")
			for (date in refDates) {
				println("\t" + sdf.format(date.time))
			}
			println("Data: ")
			for (paramCode in params) {
				print("\t$paramCode: ")
				val descList = gribFile.getParameterLevelIdentifiers(paramCode)
				for (i in descList.indices) {
					print(descList[i])
					if (i != descList.size - 1) print(", ")
				}
				println()
			}
		} catch (e: FileNotFoundException) {
			System.err.println("Cannot find file: $gribFilename")
		} catch (e: IOException) {
			System.err.println("Cannot access file: $gribFilename")
		} catch (e: NotSupportedException) {
			System.err.println("GRIB file contains unsupported features: $e")
		} catch (e: NoValidGribException) {
			System.err.println("GRIB file is invalid: $e")
		}
	}
}