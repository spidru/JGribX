/*
 * ============================================================================
 * JGribX
 * ============================================================================
 * Written by Andrew Spiteri <andrew.spiteri@um.edu.mt>
 * Adapted from JGRIB
 * 
 * Licensed under MIT (https://github.com/spidru/JGribX/blob/master/LICENSE)
 * ============================================================================
 */
package mt.edu.um.cf2.jgribx.grib1

import mt.edu.um.cf2.jgribx.*
import mt.edu.um.cf2.jgribx.api.GribProductDefinitionSection
import java.util.*

/**
 * ### [Section 1: Product definition section](https://apps.ecmwf.int/codes/grib/format/grib1/sections/1/)
 *
 *     | Octets | # | Key                                      | Type      | Content                                  |
 *     |--------|---|------------------------------------------|-----------|------------------------------------------|
 *     | 1-3    | 3 | section4Length                           | unsigned  | Length of section                        |
 *     | 4      | 1 | table2Version                            | unsigned  | GRIB tables Version No. (currently 3 for |
 *     |        |   |                                          |           | international exchange) Version numbers  |
 *     |        |   |                                          |           | 128-254 are reserved for local use       |
 *     | 5      | 1 | centre                                   | codetable | Identification of originating/generating |
 *     |        |   |                                          |           | centre (see Code table 0 = Common Code   |
 *     |        |   |                                          |           | table C1 in Part C/c.)                   |
 *     | 6      | 1 | generatingProcessIdentifier              | unsigned  | Generating process identification number |
 *     |        |   |                                          |           | (allocated by originating centre)        |
 *     | 7      | 1 | gridDefinition                           | unsigned  | Grid definition (Number of grid used     |
 *     |        |   |                                          |           | from catalogue defined by originating    |
 *     |        |   |                                          |           | centre)                                  |
 *     | 8      | 1 | section1Flags                            | codeflag  | Flag (see Regulation 92.3.2 and Code     |
 *     |        |   |                                          |           | table 1)                                 |
 *     | 9      | 1 | indicatorOfParameter                     | codetable | Indicator of parameter (see Code         |
 *     |        |   |                                          |           | table 2)                                 |
 *     | 10     | 1 | indicatorOfTypeOfLevel                   | codetable | Indicator of type of level (see Code     |
 *     |        |   |                                          |           | table 3)                                 |
 *     | 11-12  | 2 |                                          |           | Height, pressure, etc. of levels (see    |
 *     |        |   |                                          |           | Code table 3)                            |
 *     | 13     | 1 | yearOfCentury                            | unsigned  | Year of century                          |
 *     | 14     | 1 | month                                    | unsigned  | Month; Reference time of data date and   |
 *     |        |   |                                          |           | time of                                  |
 *     | 15     | 1 | day                                      | unsigned  | Day; start of averaging or accumulation  |
 *     |        |   |                                          |           | period                                   |
 *     | 16     | 1 | hour                                     | unsigned  | Hour                                     |
 *     | 17     | 1 | minute                                   | unsigned  | Minute                                   |
 *     | 18     | 1 | unitOfTimeRange                          | codetable | Indicator of unit of time range (see     |
 *     |        |   |                                          |           | Code table 4)                            |
 *     | 19     | 1 | P1                                       | unsigned  | P1 Period of time (number of time units) |
 *     |        |   |                                          |           | (0 for analyses or initialized analyses) |
 *     |        |   |                                          |           | Units of time given by octet 18          |
 *     | 20     | 1 | P2                                       | unsigned  | P2 Period of time (number of time units);|
 *     |        |   |                                          |           | or Time interval between successive      |
 *     |        |   |                                          |           | analyses, initialized analyses or        |
 *     |        |   |                                          |           | forecasts, undergoing averaging or       |
 *     |        |   |                                          |           | accumulation. Units of time given by     |
 *     |        |   |                                          |           | octet 18                                 |
 *     | 21     | 1 | timeRangeIndicator                       | codetable | Time range indicator (see Code table 5)  |
 *     | 22-23  | 2 | numberIncludedInAverage                  | unsigned  | Number included in average, when octet   |
 *     |        |   |                                          |           | 21 (Code table 5) indicates an average   |
 *     |        |   |                                          |           | or accumulation; otherwise set to zero   |
 *     | 24     | 1 | numberMissingFromAveragesOrAccumulations | unsigned  | Number missing from averages or          |
 *     |        |   |                                          |           | accumulations                            |
 *     | 25     | 1 | centuryOfReferenceTimeOfData             | unsigned  | Century of reference time of data        |
 *     | 26     | 1 | subCentre                                | codetable | Sub-centre identification (see common    |
 *     |        |   |                                          |           | Code table C1 in Part C/c., Note (3))    |
 *     | 27-28  | 2 | decimalScaleFactor                       | signed    | Units decimal scale factor (D)           |
 *     | 29-40  |12 |                                          |           | Reserved: need not be present            |
 *     | 41-nn  |   |                                          |           | Reserved for originating centre use      |
 *
 * Constructs a [Grib1RecordPDS] object from a bit input stream.
 *
 * @param tableVersion  (`4`)     GRIB tables Version No. (currently 3 for international exchange) Version numbers 128-254 are reserved for local use
 * @param centre        (`5`)     Identification of originating/generating centre (see Code table 0 = Common Code table C1 in Part C/c.)
 * @param processId     (`6`)     Generating process identification number (allocated by originating centre)
 * @param gridId        (`7`)     Grid definition (Number of grid used from catalogue defined by originating centre)
 * @param flag          (`8`)     Flag (see Regulation 92.3.2 and Code table 1)
 * @param parameterId   (`9`)     Indicator of parameter (see Code Table 2)
 * @param levelType     (`10`)    Indicator of type of level (see Code table 3)
 * @param levelData     (`11-12`) Height, pressure, etc. of levels (see Code table 3)
 * @param referenceTime (`13-17,25`) Model Run/Analysis/Reference time.
 * @param timeUnit      (`18`)    Indicator of unit of time range (see Code table 4)
 * @param p1            (`19`)    P1 Period of time (number of time units) (0 for analyses or initialized analyses). Units of time given by octet 18
 * @param p2            (`20`)    P2 Period of time (number of time units); or Time interval between successive analyses, initialized analyses or forecasts, undergoing averaging or accumulation. Units of time given by octet 18
 * @param timeRangeId   (`21`)    Time range indicator (see Code table 5)
 * @param number        (`22-23`) Number included in average, when octet 21 (Code table 5) indicates an average or accumulation; otherwise set to zero
 * @param numberMissing (`24`)    Number missing from averages or accumulations
 * @param subCenterId   (`26`)    Sub-centre identification (see common Code table C1 in Part C/c., Note (3))
 * @param decimalScale  (`27-28`) Units decimal scale factor (D)
 */
class Grib1RecordPDS internal constructor(val tableVersion: Int,
										  val centre: Int,
										  val processId: Int,
										  private val gridId: Int,
										  private val flag: Int,
										  private val parameterId: Int,
										  private val levelType: Int,
										  private val levelData: Int,
										  val referenceTime: Calendar,
										  private val timeUnit: Int,
										  private val p1: Int,
										  private val p2: Int,
										  private val timeRangeId: Int,
										  val number: Int,
										  val numberMissing: Int,
										  val subCenterId: Int,
										  val decimalScale: Int,
										  private val additionalBytes: Int) : Grib1Section, GribProductDefinitionSection {

	companion object {
		internal fun calculateForecastTime(referenceTime: Calendar, timeUnit: Int, p1Input: Int, p2Input: Int,
										   timeRangeId: Int): Pair<Calendar, Calendar> {

			val century = referenceTime.get(Calendar.YEAR) / 100
			val centuryYear = referenceTime.get(Calendar.YEAR) % 100
			val month = referenceTime.get(Calendar.MONTH) + 1
			val day = referenceTime.get(Calendar.DAY_OF_MONTH)
			val hour = referenceTime.get(Calendar.HOUR_OF_DAY)
			val minute = referenceTime.get(Calendar.MINUTE)
			var p1 = p1Input
			var p2 = p2Input
			var offset = 0
			var offset2 = 0
			var shiftUnit = timeUnit
			when (timeUnit) {
				10 -> {
					p1 *= 3
					p2 *= 3
					shiftUnit = 1
				}
				11 -> {
					p1 *= 6
					p2 *= 6
					shiftUnit = 1
				}
				12 -> {
					p1 *= 12
					p2 *= 12
					shiftUnit = 1
				}
			}
			when (timeRangeId) {
				0 -> {
					offset = p1
					offset2 = 0
				}
				1 -> {
					offset = 0
					offset2 = 0
				}
				2 -> {
					offset = p1
					offset2 = p2
				}
				3 -> {
					offset = p1
					offset2 = p2
				}
				4 -> {
					offset = p1
					offset2 = p2
				}
				5 -> {
					offset = p2
					offset2 = p1
				}
				10 -> offset = Bytes2Number.uint2(p1, p2)
				else -> Logger.error("GribRecordPDS: Time Range Indicator ${timeRangeId} is not yet supported" +
						" - continuing, but time of data is not valid")
			}

			// prep for adding the offset - get the base values
			var minute1 = minute
			var minute2 = minute
			var hour1 = hour
			var hour2 = hour
			var day1 = day
			var day2 = day
			var month1 = month
			var month2 = month
			var year1 = centuryYear
			var year2 = centuryYear
			when (shiftUnit) {
				0 -> {
					minute1 += offset
					minute2 += offset2
				}
				1 -> {
					hour1 += offset
					hour2 += offset2
				}
				2 -> {
					day1 += offset
					day2 += offset
				}
				3 -> {
					month1 += offset
					month2 += offset2
				}
				4 -> {
					year1 += offset
					year2 += offset2
				}
				else -> Logger.error("GribRecordPDS: Forecast time unit, index of ${timeUnit}," +
						" is not yet supported - continuing, but time of data is not valid")
			}

			// octets 13-17 (time of forecast)
			val localForecastTime = GregorianCalendar(TimeZone.getTimeZone("UTC"))
					.apply { timeZone = TimeZone.getTimeZone("UTC") }
					.apply { set((century - 1) * 100 + year1, month1 - 1, day1, hour1, minute1) }
			val forecastTime2 = GregorianCalendar(TimeZone.getTimeZone("UTC"))
					.apply { timeZone = TimeZone.getTimeZone("UTC") }
					.apply { set((century - 1) * 100 + year2, month2 - 1, day2, hour2, minute2) }

			return localForecastTime to forecastTime2
		}


		internal fun readFromStream(gribInputStream: GribInputStream): Grib1RecordPDS {
			/* [1-3] Section Length */
			val length = Grib1Section.readFromStream(gribInputStream)

			/* [4] Table Version */
			val tableVersion = gribInputStream.readUINT(1)

			/* [5] Originating Centre ID */
			val centre = gribInputStream.readUINT(1)

			/* [6] Generating Process */
			val processId = gribInputStream.readUINT(1)

			/* [7] Grid Definition */
			val gridId = gribInputStream.readUINT(1)

			/* [8] Flag (Presence of PDS and GDS) */
			val flag = gribInputStream.readUINT(1)

			/* [9] Parameter Indicator */
			val parameterId = gribInputStream.readUINT(1)

			/* [10] Level Type */
			val levelType = gribInputStream.readUINT(1)

			/* [11-12] Level Data (height, pressure, etc.) */
			val levelData = gribInputStream.readUINT(2)

			/* [13] Year of Century */
			val centuryYear = gribInputStream.readUINT(1)

			/* [14] Month */
			val month = gribInputStream.readUINT(1)

			/* [15] Day */
			val day = gribInputStream.readUINT(1)

			/* [16] Hour */
			val hour = gribInputStream.readUINT(1)

			/* [17] Minute */
			val minute = gribInputStream.readUINT(1)

			/* [18] Time Unit */
			val timeUnit = gribInputStream.readUINT(1)

			/* [19] Time Period P1 (time units) */
			val p1 = gribInputStream.readUINT(1)

			/* [20] Time Period P2 (time units) */
			val p2 = gribInputStream.readUINT(1)

			/* [21] Time Range */
			val timeRangeId = gribInputStream.readUINT(1)

			/* [22-23] Number included in average */
			val number = gribInputStream.readUINT(2)

			/* [24] Number missing from averages */
			val numberMissing = gribInputStream.readUINT(1)

			/* [25] Reference Time Century */
			val century = gribInputStream.readUINT(1)
			val referenceTime = GregorianCalendar(100 * (century - 1) + centuryYear, month - 1, day, hour, minute, 0)
					.apply { timeZone = TimeZone.getTimeZone("UTC") }

			/* [26] Originating Sub-centre ID */
			val subcenterId = gribInputStream.readUINT(1)

			/* [27-28] Decimal Scale Factor */
			val decimalScale = gribInputStream.readINT(2, Bytes2Number.INT_SM)

			return Grib1RecordPDS(tableVersion, centre, processId, gridId, flag, parameterId, levelType, levelData,
					referenceTime, timeUnit, p1, p2, timeRangeId, number, numberMissing, subcenterId,
					decimalScale, length - 28)
					.takeIf { it.length == length }
					?: throw NoValidGribException("PDS length mismatch")
		}
	}

	override val length: Int
		get() = 28 + additionalBytes

	/** True, if GDS exists. */
	internal val gdsExists: Boolean
		get() = flag and 128 == 128

	/** True, if BMS exists. */
	internal val bmsExists: Boolean
		get() = flag and 64 == 64

	override val parameter: Grib1Parameter = if (tableVersion == 255) {
		// paramTable = null
		Grib1Parameter(255, "missing", "missing parameter", "")
	} else {
		// Before getting parameter table values, must get the appropriate table for this center, subcenter (not yet implemented) and parameter table.
		// paramTable = GribPDSParamTable.getParameterTable(center_id, subcenter_id, table_version);
		// parameter = parameter_table.getParameter(data[5]);
		// parameter = GribPDSParamTable.getParameterFromFile(centreId, subcenter_id, table_version, parameterId);
		Grib1Parameter.getParameter(tableVersion, parameterId, centre)
				?: throw NotSupportedException("Unsupported Parameter $parameterId in Table $tableVersion")
	}

	/**
	 * Containing the information about the level. This helps to actually use the data, otherwise the string for
	 * level will have to be parsed.
	 */
	override val level: Grib1Level? = Grib1Level.getLevel(levelType, levelData)

	/** Forecast time. Also used as starting time when times represent a period */
	override val forecastTime: Calendar

	/** Ending time when times represent a period */
	private val forecastTime2: Calendar

	init {
		calculateForecastTime(referenceTime, timeUnit, p1, p2, timeRangeId).also { (f1, f2) ->
			forecastTime = f1
			forecastTime2 = f2
		}
	}

	/** Get the base (analysis) time of the forecast in GMT. */
	val gMTBaseTime: Calendar
		get() {
			val gmtTime = referenceTime.clone() as Calendar
			// hopefully this DST offset adjusts to DST automatically
			val dstOffset = gmtTime[Calendar.DST_OFFSET] / 3600000
			val gmtOffset = gmtTime[Calendar.ZONE_OFFSET] / 3600000 //ms to hours
			Logger.debug("offset is ${gmtOffset}")
			Logger.debug("dst offset is ${dstOffset}")
			//put offset back
			gmtTime[Calendar.HOUR] = gmtTime[Calendar.HOUR] - gmtOffset - dstOffset
			Logger.debug("new time is ${gmtTime.time}")
			return gmtTime
		}

	/**
	 * Get the time of the forecast. FIXME calls to this method somehow affect
	 * forecastTime (should be solved now, confirm)
	 *
	 * @return date and time
	 */
	val gMTForecastTime: Calendar
		get() {
			val gmtTime = forecastTime.clone() as Calendar
			Logger.debug("forecast time = ${gmtTime.time}")
			// hopefully this DST offset adjusts to DST automatically
			val dstOffset = gmtTime[Calendar.DST_OFFSET] / 3600000
			val gmtOffset = gmtTime[Calendar.ZONE_OFFSET] / 3600000 //ms to hours
			Logger.debug("offset is ${gmtOffset}")
			Logger.debug("dst offset is ${dstOffset}")
			gmtTime[Calendar.HOUR] = gmtTime[Calendar.HOUR] - gmtOffset - dstOffset //put offset back
			Logger.debug("new time is ${gmtTime.time}")
			return gmtTime
		}

	override fun writeTo(outputStream: GribOutputStream) {
		super.writeTo(outputStream) // [1-3]
		outputStream.writeUInt(tableVersion, bytes = 1) // [4]
		outputStream.writeUInt(centre, bytes = 1) // [5]
		outputStream.writeUInt(processId, bytes = 1) // [6]
		outputStream.writeUInt(gridId, bytes = 1) // [7]
		outputStream.writeUInt(flag, bytes = 1) // [8]
		outputStream.writeUInt(parameterId, bytes = 1) // [9]
		outputStream.writeUInt(levelType, bytes = 1) // [10]
		outputStream.writeUInt(levelData, bytes = 2) // [11-12]
		outputStream.writeUInt(referenceTime.get(Calendar.YEAR) % 100, bytes = 1) // [13]
		outputStream.writeUInt(referenceTime.get(Calendar.MONTH) + 1, bytes = 1) // [14]
		outputStream.writeUInt(referenceTime.get(Calendar.DAY_OF_MONTH), bytes = 1) // [15]
		outputStream.writeUInt(referenceTime.get(Calendar.HOUR_OF_DAY), bytes = 1) // [16]
		outputStream.writeUInt(referenceTime.get(Calendar.MINUTE), bytes = 1) // [17]
		outputStream.writeUInt(timeUnit, bytes = 1) // [18]
		outputStream.writeUInt(p1, bytes = 1) // [19]
		outputStream.writeUInt(p2, bytes = 1) // [20]
		outputStream.writeUInt(timeRangeId, bytes = 1) // [21]
		outputStream.writeUInt(number, bytes = 2) // [22-23]
		outputStream.writeUInt(numberMissing, bytes = 1) // [24]
		outputStream.writeUInt((referenceTime.get(Calendar.YEAR) / 100) + 1, bytes = 1) // [25]
		outputStream.writeUInt(subCenterId, bytes = 1) // [26]
		outputStream.writeSMInt(decimalScale, bytes = 2) // [27-28]
		outputStream.write(ByteArray(additionalBytes)) // [29-40], [41-nn]
	}

	/**
	 * rdg - added this method to be used in a comparator for sorting while
	 * extracting records. Not currently used in the JGrib library, but is used
	 * in a library I'm using that uses JGrib. Compares numerous features from
	 * the PDS information to sort according to a time, level, level-type,
	 * y-axis, x-axis order
	 *
	 * @param pds - GribRecordPDS object
	 * @return - -1 if pds is "less than" this, 0 if equal, 1 if pds is "greater than" this.
	 */
	fun compare(pds: Grib1RecordPDS): Int {
		if (this == pds) return 0
		// not equal, so either less than or greater than.
		// check if pds is less; if not, then pds is greater
		if (gridId > pds.gridId) return -1
		if (referenceTime.time.time > pds.referenceTime.time.time) return -1
		if (forecastTime.time.time > pds.forecastTime.time.time) return -1
		if (forecastTime2.time.time > pds.forecastTime2.time.time) return -1
		if (centre > pds.centre) return -1
		if (subCenterId > pds.subCenterId) return -1
		if (tableVersion > pds.tableVersion) return -1
		if (decimalScale > pds.decimalScale) return -1
		if (length > pds.length) return -1
		if (parameter.compare(pds.parameter) < 0) return -1
		return level?.compare(pds.level) ?: 1
		// if here, then something must be greater than something else - doesn't matter what
	}

	override fun equals(other: Any?) = this === other
			|| other is Grib1RecordPDS
			&& tableVersion == other.tableVersion
			&& centre == other.centre
			&& processId == other.processId
			&& gridId == other.gridId
			&& flag == other.flag
			&& parameter == other.parameter
			&& levelType == other.levelType
			&& levelData == other.levelData
			&& referenceTime == other.referenceTime
			&& timeUnit == other.timeUnit
			&& p1 == other.p1
			&& p2 == other.p2
			&& timeRangeId == other.timeRangeId
			&& number == other.number
			&& numberMissing == other.numberMissing
			&& subCenterId == other.subCenterId
			&& decimalScale == other.decimalScale

	override fun hashCode(): Int {
		var result = length
		result = 31 * result + tableVersion
		result = 31 * result + centre
		result = 31 * result + processId
		result = 31 * result + gridId
		result = 31 * result + flag
		result = 31 * result + parameter.hashCode()
		result = 31 * result + levelType
		result = 31 * result + levelData
		result = 31 * result + referenceTime.hashCode()
		result = 31 * result + timeUnit
		result = 31 * result + p1
		result = 31 * result + p2
		result = 31 * result + timeRangeId
		result = 31 * result + number
		result = 31 * result + numberMissing
		result = 31 * result + subCenterId
		result = 31 * result + decimalScale
		return result
	}

	private fun timeString(): String {
		val (timeRange, connector) = when (timeRangeId) {
			2 -> "product valid from " to " to "
			3 -> "product is an average between " to " and "
			4 -> "product is an accumulation from " to " to "
			5 -> "product is the difference of " to " minus "
			else -> {
				Logger.error("GribRecordPDS: Time Range Indicator ${timeRangeId} is not yet supported" +
						" - continuing, but time of data is not valid")
				null to null
			}
		}

		val time1 = (forecastTime[Calendar.DAY_OF_MONTH].toString() + "."
				+ (forecastTime[Calendar.MONTH] + 1) + "."
				+ forecastTime[Calendar.YEAR] + "  "
				+ forecastTime[Calendar.HOUR_OF_DAY] + ":"
				+ forecastTime[Calendar.MINUTE])
		val time2 = (forecastTime2[Calendar.DAY_OF_MONTH].toString() + "."
				+ (forecastTime[Calendar.MONTH] + 1) + "."
				+ forecastTime[Calendar.YEAR] + "  "
				+ forecastTime[Calendar.HOUR_OF_DAY] + ":"
				+ forecastTime[Calendar.MINUTE])
		return if (timeRange == null) time1 else "${timeRange}${time1}${connector}${time2}"
	}

	override fun toString(): String = listOfNotNull(
			"GRIB1 PDS header:",
			"\tCenter: ${centre}",
			"\tSub-Center: ${subCenterId}",
			"\tTable: ${tableVersion}",
			"\tGrid ID: ${gridId}",
			"\tTime: ${timeString()} (dd.mm.yyyy hh:mm)",
			"\tType: ${parameter.code}",
			"\tDescription: ${parameter.description}",
			"\tUnit: ${parameter.units}",
			"\tTable version: ${tableVersion}",
			"\t${level} decimal scale: ${decimalScale}",
			(if (gdsExists) "\tGDS exists" else null),
			(if (bmsExists) "\tBMS exists" else null))
			.joinToString("\n")
}
