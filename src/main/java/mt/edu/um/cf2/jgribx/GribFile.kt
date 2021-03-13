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
import mt.edu.um.cf2.jgribx.api.GribMessage
import java.io.BufferedInputStream
import java.io.FileInputStream
import java.io.InputStream
import java.io.PrintStream
import java.text.SimpleDateFormat
import java.util.*
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
	var messagesSkippedCount = 0

	/** List of GRIB records */
	val records: List<GribRecord>
		get() = messages.flatMap { it.records }

	//private val recordsInternal = mutableListOf<GribRecord>()
	private val messages = mutableListOf<GribMessage>()

	/** The different originating centre IDs found in the GRIB file. */
	val centreIDs: List<Int>
		get() = records.asSequence().map { it.centreId }.distinct().toList()

	// Check if GRIB file contains different editions
	// TODO not sure if different editions within one file should be allowed
	val edition: Int
		get() {
			val edition = records.firstOrNull()?.indicatorSection?.gribEdition
					?: throw NoValidGribException("No GRIB edition found")
			if (records.filterIndexed { i, record -> i > 0 && record.indicatorSection.gribEdition != edition }.any()) {
				Logger.warning("GRIB file contains different editions")
			}
			return edition
		}

	// Compare dates
	val forecastTimes: List<Calendar>
		get() = messages
				.asSequence()
				.flatMap { it.records }
				.map { it.forecastTime }
				.distinctBy { it.time }
				.sorted()
				.toList()

	/** Sorted list of different parameter codes present within the GRIB file. */
	val parameterCodes: List<String>
		get() = records.asSequence().map { it.parameterCode }.distinct().sorted().toList()

	/** Sorted list of different parameter levels as textual descriptions. */
	val parameterLevelDescriptions: List<String>
		get() = records.asSequence().map { it.levelDescription }.distinct().sorted().toList()

	/** The different generating process IDs found in the GRIB file. */
	val processIDs: IntArray
		get() = records.asSequence().map { it.processId }.distinct().toList().toIntArray()

	/** Number of records this GRIB file contains. */
	val recordCount: Int
		get() = messages.sumBy { it.records.size }

	/**
	 * Return a List of different reference times present in the GRIB file.
	 * @return a sorted list of different reference times
	 */
	val referenceTimes: List<Calendar>
		get() = records.asSequence().map { it.referenceTime }.distinct().sorted().toList()

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
		/**
		 * Initialise the Parameter Tables with the information in the parameter
		 * table lookup file.  See GribPDSParamTable for details
		 */
		var count = 0
		while (gribInputStream.available() > 0) {
			// There may be non-GRIB data on the beginning of the file or between GRIB messages, e.g. when using
			// get_gfs.pl script (https://www.cpc.ncep.noaa.gov/products/wesley/get_gfs.html)
			if (GribRecordIS.seekNext(gribInputStream)) try {
				count++
				Logger.info("GRIB Message $count")

				val message = GribMessage.readFromStream(gribInputStream)
				message.records.forEachIndexed { index, record ->
					Logger.info("\tGRIB Record $index")
					Logger.info("\t\tReference Time: ${record.referenceTime.time}")
					Logger.info("\t\tParameter: ${record.parameterCode} (${record.parameterDescription})")
					Logger.info("\t\tLevel: ${record.levelCode} (${record.levelDescription})")
				}
				messages.add(message)
			} catch (e: NotImplementedError) {
				Logger.warning("Skipping GRIB record ${count} (${e.message})", e)
				messagesSkippedCount++
			} catch (e: NotSupportedException) {
				Logger.warning("Skipping GRIB record ${count} (${e.message})", e)
				messagesSkippedCount++
			} catch (e: NoValidGribException) {
				Logger.warning("Skipping GRIB record ${count} (${e.message})", e)
				messagesSkippedCount++
			}
		}
		gribInputStream.close()
		if (messages.isEmpty()) throw NoValidGribException("No valid GRIB message found")
		else Logger.info("Reached end of file: ${messages.size} of ${count} messages read successfully")
	}

	fun getParameterLevelDescriptions(paramCode: String): List<String?> {
		val params = messages
				.asSequence()
				.flatMap(GribMessage::records)
				.filter { it.parameterCode == paramCode }
				.map { it.levelIdentifier to it.levelDescription }
				.groupBy(Pair<String, String>::first)
				.mapValues { it.value.map(Pair<String, String>::second) }

		if (params.values.any { it.size > 1 }) Logger.error("Record contains duplicate level IDs")
		return params.values.map { it.first() }
	}

	fun getParameterLevelIdentifiers(paramCode: String): List<String?> = messages
			.asSequence()
			.flatMap { it.records }
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
	 * @param parameterCode Parameter to search for
	 * @param level Level to search for
	 * @return The found record, null if no record has been found
	 */
	fun getRecord(time: Calendar, parameterCode: String, level: String): GribRecord? {
		// Find closest forecast time
		val closestForecastTime: Calendar? = messages.asSequence()
				.flatMap { it.records }
				.map { it.forecastTime }
				.minByOrNull { abs(time.timeInMillis - it.timeInMillis) }

		// Match patterns such as "ISBL:200" and "SFC"
		val matcher = Pattern.compile("(\\w+)(?::(\\d+))?").matcher(level)
		if (matcher.find()) {
			val levelCode = matcher.group(1)
			val levelValue = matcher.group(2)?.toIntOrNull()
			return messages.asSequence()
					.flatMap { it.records }
					.filter { it.forecastTime == closestForecastTime }
					.filter { it.parameterCode == parameterCode }
					.filter { it.levelCode == levelCode }
					.filter { levelValue == null || it.levelValues[0].toInt() == levelValue }
					.firstOrNull()
		}
		return null
	}

	/**
	 * Prints out a summary of the GRIB file.
	 * @param out
	 */
	fun getSummary(out: PrintStream = System.out) {
		val simpleDateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss 'UTC'", Locale.ROOT)
				.apply { timeZone = TimeZone.getTimeZone("UTC") }
		val centreIds = centreIDs
		val processIds = processIDs
		val refDates = referenceTimes
		val forecastDates = forecastTimes

		// Print out generic GRIB file info
		out.println("---------------------------------------")
		out.println("Reading file: ${filename}")
		out.println("GRIB Edition: ${edition}")
		out.println("Messages successfully read: ${messages.size} of ${messages.size + messagesSkippedCount}")
		out.println("Records successfully read: ${recordCount}")
		out.println("---------------------------------------")

		// Print out originating centre info
		out.print("Weather Centre(s): ")
		for (i in centreIds.indices) {
			out.print("${centreIds[i]} [${getCentreName(centreIds[i])}]")
			if (i != centreIds.size - 1) out.print(",")
		}
		out.println()

		// Print out generating process info
		out.print("Generating Process(es): ")
		for (i in processIds.indices) {
			out.print("${processIds[i]} [${getProcessName(processIds[i])}]")
			if (i != processIds.size - 1) out.print(",")
		}
		out.println()

		// Get reference time
		out.println("Reference Time: ")
		for (date in refDates) {
			out.println("\t${simpleDateFormat.format(date.time)}")
		}

		// Get forecast times
		out.println("Forecast Time(s): ")
		for (date in forecastDates) {
			out.println("\t${simpleDateFormat.format(date.time)}")
		}
	}

	fun writeTo(gribOutputStream: GribOutputStream) = messages.forEach { it.writeTo(gribOutputStream) }

	override fun toString(): String = "GRIB file (${recordCount} records)"
}
