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
import java.io.BufferedInputStream
import java.io.FileInputStream
import java.io.InputStream
import java.io.PrintStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.math.abs

/**
 * The GribFile class represents a GRIB file containing any number of records,
 * stored within the class as a [List]`<`[GribRecord]`>`.
 *
 * This class can be considered as a top-level class which does not deal with
 * the underlying format of the records within the file. This means that this
 * class remains valid for all formats such as GRIB-1 and GRIB-2.
 *
 * Constructs a GribFile object from a bit input stream.
 *
 * @param gribInputStream bit input stream with GRIB content
 *
 * @throws java.io.IOException if stream can not be opened etc.
 * @throws NotSupportedException if file contains features not yet supported
 * @throws NoValidGribException  if stream does not contain a valid GRIB file
 */
class GribFile(gribInputStream: GribInputStream) {
	/** Returns the GRIB filename. */
	private var filename: String? = null

	/** Returns the number of records skipped due to them being invalid or not supported. */
	var recordsSkippedCount = 0

	/** List of GRIB records */
	val records: List<GribRecord>
		get() = recordsInternal

	private val recordsInternal = mutableListOf<GribRecord>()

	/** The different originating centre IDs found in the GRIB file. */
	val centreIDs: List<Int>
		get() = recordsInternal.map { it.centreId }.distinct()

	// Check if GRIB file contains different editions
	// TODO not sure if different editions within one file should be allowed
	val edition: Int
		get() {
			val edition = recordsInternal[0].indicatorSection.gribEdition
			for (i in 1 until recordsInternal.size) {
				if (recordsInternal[i].indicatorSection.gribEdition != edition) {
					Logger.warning("GRIB file contains different editions")
					break
				}
			}
			return edition
		}

	// Compare dates
	val forecastTimes: List<Calendar>
		get() = recordsInternal.map { it.forecastTime }.distinctBy { it.time }.sorted()

	/** Sorted list of different parameter codes present within the GRIB file. */
	val parameterCodes: List<String>
		get() = recordsInternal.map { it.parameterCode }.distinct().sorted()

	/** Sorted list of different parameter levels as textual descriptions. */
	val parameterLevelDescriptions: List<String>
		get() = recordsInternal.map { it.levelDescription }.distinct().sorted()

	/** The different generating process IDs found in the GRIB file. */
	val processIDs: IntArray
		get() = recordsInternal.map { it.processId }.distinct().toIntArray()

	/** Number of records this GRIB file contains. */
	val recordCount: Int
		get() = recordsInternal.size

	/**
	 * Return a List of different reference times present in the GRIB file.
	 * @return a sorted list of different reference times
	 */
	val referenceTimes: List<Calendar>
		get() = recordsInternal.map { it.referenceTime }.distinct().sorted()

	/**
	 * Constructs a [GribFile] object from a file.
	 *
	 * @param filename name of the GRIB file
	 * @throws java.io.FileNotFoundException if file cannot be found
	 * @throws java.io.IOException           if file cannot be opened etc.
	 * @throws NotSupportedException if file contains features not yet supported
	 * @throws NoValidGribException  if file is no valid GRIB file
	 */
	constructor(filename: String, onRead: (Long) -> Unit = {}) : this(FileInputStream(filename), onRead) {
		this.filename = filename
	}

	/**
	 * Constructs a [GribFile] object from an input stream.
	 *
	 * note: the BufferedInputStream enables mark/reset functionality
	 *
	 * @param inputStream input stream with GRIB content
	 * @throws java.io.IOException           if stream cannot be opened etc.
	 * @throws NotSupportedException if file contains features not yet supported
	 * @throws NoValidGribException  if stream does not contain a valid GRIB file
	 */
	constructor(inputStream: InputStream, onRead: (Long) -> Unit = {}) :
			this(GribInputStream(BufferedInputStream(inputStream), onRead))

	init {
		// Initialise fields
		/**
		 * Initialise the Parameter Tables with the information in the parameter
		 * table lookup file.  See GribPDSParamTable for details
		 */
		//GribPDSParamTable.readParameterTableLookup(); done in static initializer
		var count = 0
		while (gribInputStream.available() > 0) {
			count++
			try {
				val record = GribRecord.readFromStream(gribInputStream)
				Logger.info("GRIB Record $count")
				Logger.info("\tReference Time: ${record.referenceTime.time}")
				Logger.info("\tParameter: ${record.parameterCode} (${record.parameterDescription})")
				Logger.info("\tLevel: ${record.levelCode} (${record.levelDescription})")
				recordsInternal.add(record)
			} catch (e: NotSupportedException) {
				Logger.warning("Skipping GRIB record ${count} (${e.message})", e)
				recordsSkippedCount++
				continue
			} catch (e: NoValidGribException) {
				Logger.warning("Skipping GRIB record ${count} (${e.message})", e)
				recordsSkippedCount++
			} finally {
				GribRecordIS.seekNext(gribInputStream) // Skip to end of current record
			}
		}
		gribInputStream.close()
		if (recordsInternal.isEmpty()) throw NoValidGribException("No valid GRIB records found.")
		else Logger.info("Reached end of file: ${recordsInternal.size} of ${count} records read successfully")
	}

	fun getParameterLevelDescriptions(paramCode: String): List<String?> {
		val descList = mutableListOf<String>()
		for (record in recordsInternal) {
			if (record.parameterCode == paramCode) {
				if (descList.contains(record.levelIdentifier)) {
					Logger.error("Record contains duplicate level IDs")
				} else descList.add(record.levelDescription)
			}
		}
		return descList
	}

	fun getParameterLevelIdentifiers(paramCode: String): List<String?> = recordsInternal
			.asSequence()
			.filter { it.parameterCode == paramCode }
			.map { it.levelIdentifier }
			.distinct()
			.toList()

	/**
	 * Search for a record using the forecast time, parameter and level.
	 *
	 *
	 * Searches through all records which have been successfully read.
	 * The search works by first finding the closest forecast time to the one specified
	 * in the list of records. This forecast time is then used together with the
	 * specified parameter and level to find a record which matches these values.
	 *
	 * @param time Forecast time to search for
	 * @param parameterAbbrev Parameter to search for
	 * @param levelCode Level to search for
	 * @return The found record, null if no record has been found
	 */
	fun getRecord(time: Calendar, parameterAbbrev: String, levelCode: String): GribRecord? {
		// Find closest forecast time
		var deltaMs: Long
		var deltaminMs = Long.MAX_VALUE
		var closestTime: Calendar? = null
		for (record in recordsInternal) {
			deltaMs = abs(time.timeInMillis - record.forecastTime.timeInMillis)
			if (deltaMs < deltaminMs) {
				deltaminMs = deltaMs
				closestTime = record.forecastTime
			}
		}

		// Match patterns such as "ISBL:200" and "SFC"
		val pattern = Pattern.compile("(\\w+)(?::(\\d+))?")
		var matcher: Matcher
		for (record in recordsInternal) {
			matcher = pattern.matcher(levelCode)
			if (matcher.find()) {
				val code = matcher.group(1)
				if (record.forecastTime == closestTime
						&& record.parameterCode == parameterAbbrev
						&& record.levelCode == code
						&& (matcher.group(2) == null
								|| record.levelValues[0].toInt() == matcher.group(2).toInt())) {
					return record
				}
			}
		}
		return null
	}

	/**
	 * Prints out a summary of the GRIB file.
	 * @param out
	 */
	fun getSummary(out: PrintStream) {
		val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm:ss 'UTC'", Locale.ROOT)
		sdf.timeZone = TimeZone.getTimeZone("UTC")
		val centreIds = centreIDs
		val processIds = processIDs
		val refDates = referenceTimes
		val forecastDates = forecastTimes

		// Print out generic GRIB file info
		out.println("---------------------------------------")
		out.println("Reading file: ${filename}")
		out.println("GRIB Edition: ${edition}")
		out.println("Records successfully read: " + recordCount + " of "
				+ (recordCount + recordsSkippedCount))
		out.println("---------------------------------------")

		// Print out originating centre info
		out.print("Weather Centre(s): ")
		for (i in centreIds.indices) {
			out.print(centreIds[i]
					.toString() + " [" + getCentreName(centreIds[i]) + "]")
			if (i != centreIds.size - 1) out.print(",")
		}
		out.println()

		// Print out generating process info
		out.print("Generating Process(es): ")
		for (i in processIds.indices) {
			out.print(processIds[i]
					.toString() + " [" + getProcessName(processIds[i]) + "]")
			if (i != processIds.size - 1) out.print(",")
		}
		out.println()

		// Get reference time
		println("Reference Time: ")
		for (date in refDates) {
			println("\t" + sdf.format(date.time))
		}

		// Get forecast times
		println("Forecast Time(s): ")
		for (date in forecastDates) {
			println("\t" + sdf.format(date.time))
		}
	}

	override fun toString(): String = "GRIB file (" + recordsInternal.size + " records)"
}
