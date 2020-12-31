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

import mt.edu.um.cf2.jgribx.Bytes2Number
import mt.edu.um.cf2.jgribx.GribInputStream
import mt.edu.um.cf2.jgribx.Logger
import mt.edu.um.cf2.jgribx.NotSupportedException
import java.util.*

/**
 * A class representing the product definition section (PDS) of a GRIB record.
 *
 * Constructs a [Grib1RecordPDS] object from a bit input stream.
 *
 * @param gribInputStream bit input stream with PDS content
 * @throws java.io.IOException if stream can not be opened etc.
 * @throws NotSupportedException
 */
class Grib1RecordPDS internal constructor(gribInputStream: GribInputStream) {

	/** Length in bytes of this PDS. */
	var length: Int

	/** Exponent of decimal scale. */
	var decimalScale: Int

	/** ID of grid type. */
	protected var gridId: Int // no pre-definied grids supported yet.

	/** True, if GDS exists. */
	internal var gdsExists: Boolean
		private set

	/** True, if BMS exists. */
	internal var bmsExists: Boolean
		private set

	/** The parameter as defined in the Parameter Table */
	var parameter: Grib1Parameter
		protected set

	/**
	 * Class containing the information about the level. This helps to actually use the data, otherwise the string for
	 * level will have to be parsed.
	 */
	var pDSLevel: Grib1Level?

	/** Model Run/Analysis/Reference time. */
	var referenceTime: Calendar
		protected set

	/** Forecast time. Also used as starting time when times represent a period */
	var localForecastTime: Calendar

	/** Ending time when times represent a period */
	protected var forecastTime2: Calendar

	/**
	 * String used in building a string to represent the time(s) for this PDS
	 * See the decoder for octet 21 to get an understanding
	 */
	protected var timeRange: String? = null

	/**
	 * String used in building a string to represent the time(s) for this PDS
	 * See the decoder for octet 21 to get an understanding
	 */
	protected var connector: String? = null

	/** Parameter Table Version number, currently 3 for international exchange. */
	val tableVersion: Int

	/** Identification of center e.g. 88 for Oslo */
	val centreId: Int

	/** Identification of subcenter */
	val subcenterId: Int

	/** Identification of Generating Process (i.e. the numerical model that created the data). */
	val processId: Int

	/**
	 * rdg - moved the Parameter table information and functionality into a class.
	 * See GribPDSParamTable class for details.
	 */
	var paramTable: Grib1PDSParamTable? = null

	/** The type of the parameter. */
	val parameterAbbreviation: String
		get() = parameter.abbreviation

	/** Descritpion of the parameter. */
	val parameterDescription: String
		get() = parameter.description

	/** Name of the unit of the parameter. */
	val parameterUnits: String
		get() = parameter.units

	// rdg - added the following getters for level information though they are
	//       just convenience methods.  You could do the same by getting the
	//       GribPDSLevel (with getPDSLevel) then calling its methods directly

	/** Name for the type of level for this forecast/analysis. */
	val levelName: String?
		get() = pDSLevel?.name

	/** The long description for this level of the forecast/analysis. */
	val levelDesc: String?
		get() = pDSLevel?.description

	/** The units for the level of the forecast/analysis. */
	val levelUnits: String?
		get() = pDSLevel?.units

	/** Get the numeric value for this level. */
	val levelValue: Float?
		get() = pDSLevel?.value1

	/** Get value 2 (if it exists) for this level (height or pressure) */
	val levelValue2: Float?
		get() = pDSLevel?.value2

	//ms to hours
	//put offset back
	// hopefully this DST offset adjusts to DST automatically

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

	//ms to hours
	//put offset back
	// hopefully this DST offset adjusts to DST automatically

	/**
	 * Get the time of the forecast. FIXME calls to this method somehow affect
	 * forecastTime (should be solved now, confirm)
	 *
	 * @return date and time
	 */
	val gMTForecastTime: Calendar
		get() {
			val gmtTime = localForecastTime.clone() as Calendar
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

	init {
		var offset = 0
		var offset2 = 0

		// All defined times are in UTC
		//TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

		/* [1-3] Section Length */
		length = gribInputStream.readUINT(3)

		/* [4] Table Version */
		tableVersion = gribInputStream.readUINT(1)

		/* [5] Originating Centre ID */
		centreId = gribInputStream.readUINT(1)

		/* [6] Generating Process */
		processId = gribInputStream.readUINT(1)

		/* [7] Grid Definition */
		gridId = gribInputStream.readUINT(1)

		/* [8] Flag (Presence of PDS and GDS) */
		val flag = gribInputStream.readUINT(1)
		gdsExists = flag and 128 == 128
		bmsExists = flag and 64 == 64

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
		var timeUnit = gribInputStream.readUINT(1)

		/* [19] Time Period P1 (time units) */
		var p1 = gribInputStream.readUINT(1)

		/* [20] Time Period P2 (time units) */
		var p2 = gribInputStream.readUINT(1)

		/* [21] Time Range */
		val timeRangeId = gribInputStream.readUINT(1)

		/* [22-23] Number included in average */
		/*val number = */gribInputStream.readUINT(2)

		/* Number missing from averages */
		/*val numberMissing = */gribInputStream.readUINT(1)

		/* [25] Reference Time Century */
		val century = gribInputStream.readUINT(1)

		/* [26] Originating Sub-centre ID */
		subcenterId = gribInputStream.readUINT(1)

		/* [27-28] Decimal Scale Factor */
		decimalScale = gribInputStream.readINT(2, Bytes2Number.INT_SM)
		/** */
		/* Data Processing */

		/* Parameter */
		parameter = if (tableVersion == 255) {
			// paramTable = null
			Grib1Parameter(255, "missing", "missing parameter", "")
		} else {
			// Before getting parameter table values, must get the appropriate table for this center, subcenter (not yet implemented) and parameter table.
			// paramTable = GribPDSParamTable.getParameterTable(center_id, subcenter_id, table_version);
			// parameter = parameter_table.getParameter(data[5]);
			// parameter = GribPDSParamTable.getParameterFromFile(centreId, subcenter_id, table_version, parameterId);
			Grib1Parameter.getParameter(tableVersion, parameterId, centreId)
					?: throw NotSupportedException("Unsupported Parameter $parameterId in Table $tableVersion")
		}

		/* Level */
		// this.level = GribTables.getLevel(data[6], data[7], data[8]);
		pDSLevel = Grib1Level.getLevel(levelType, levelData)

		// octets 13-17 (base time of forecast in UTC)
		referenceTime = GregorianCalendar(100 * (century - 1) + centuryYear, month - 1, day, hour, minute)
		when (timeUnit) {
			10 -> {
				p1 *= 3
				p2 *= 3
				timeUnit = 1
			}
			11 -> {
				p1 *= 6
				p2 *= 6
				timeUnit = 1
			}
			12 -> {
				p1 *= 12
				p2 *= 12
				timeUnit = 1
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
				timeRange = "product valid from "
				connector = " to "
				offset = p1
				offset2 = p2
			}
			3 -> {
				timeRange = "product is an average between "
				connector = " and "
				offset = p1
				offset2 = p2
			}
			4 -> {
				timeRange = "product is an accumulation from "
				connector = " to "
				offset = p1
				offset2 = p2
			}
			5 -> {
				timeRange = "product is the difference of "
				connector = " minus "
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
		when (timeUnit) {
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
		// All defined times are in UTC
		localForecastTime = GregorianCalendar(TimeZone.getTimeZone("UTC")).apply {
			set((century - 1) * 100 + year1, month1 - 1, day1, hour1, minute1)
		}
		forecastTime2 = GregorianCalendar(TimeZone.getTimeZone("UTC")).apply {
			set((century - 1) * 100 + year2, month2 - 1, day2, hour2, minute2)
		}
	}

	/**
	 * rdg - added this method to be used in a comparator for sorting while
	 * extracting records. Not currently used in the JGrib library, but is used
	 * in a library I'm using that uses JGrib. Compares numerous features from
	 * the PDS information to sort according to a time, level, level-type,
	 * y-axis, x-axis order
	 *
	 * @param pds - GribRecordPDS object
	 * @return - -1 if pds is "less than" this, 0 if equal, 1 if pds is "greater
	 * than" this.
	 */
	fun compare(pds: Grib1RecordPDS): Int {
		if (this == pds) return 0
		// not equal, so either less than or greater than.
		// check if pds is less; if not, then pds is greater
		if (gridId > pds.gridId) return -1
		if (referenceTime.time.time > pds.referenceTime.time.time) return -1
		if (localForecastTime.time.time > pds.localForecastTime.time.time) return -1
		if (forecastTime2.time.time > pds.forecastTime2.time.time) return -1
		if (centreId > pds.centreId) return -1
		if (subcenterId > pds.subcenterId) return -1
		if (tableVersion > pds.tableVersion) return -1
		if (decimalScale > pds.decimalScale) return -1
		if (length > pds.length) return -1
		if (parameter.compare(pds.parameter) < 0) return -1
		return if ((pDSLevel?.compare(pds.pDSLevel) ?: 1) < 0) -1 else 1
		// if here, then something must be greater than something else - doesn't matter what
	}

	override fun equals(other: Any?): Boolean {
		if (other !is Grib1RecordPDS) return false
		if (this === other) return true
		if (gridId != other.gridId) return false
		if (referenceTime !== other.referenceTime) return false
		if (localForecastTime !== other.localForecastTime) return false
		if (centreId != other.centreId) return false
		if (subcenterId != other.subcenterId) return false
		if (tableVersion != other.tableVersion) return false
		if (decimalScale != other.decimalScale) return false
		if (length != other.length) return false
		if (parameter != other.parameter) return false
		return pDSLevel == other.pDSLevel
	}

	override fun hashCode(): Int {
		var result = length
		result = 31 * result + decimalScale
		result = 31 * result + gridId
		result = 31 * result + gdsExists.hashCode()
		result = 31 * result + bmsExists.hashCode()
		result = 31 * result + parameter.hashCode()
		result = 31 * result + (pDSLevel?.hashCode() ?: 0)
		result = 31 * result + referenceTime.hashCode()
		result = 31 * result + localForecastTime.hashCode()
		result = 31 * result + forecastTime2.hashCode()
		result = 31 * result + (timeRange?.hashCode() ?: 0)
		result = 31 * result + (connector?.hashCode() ?: 0)
		result = 31 * result + tableVersion
		result = 31 * result + centreId
		result = 31 * result + subcenterId
		result = 31 * result + processId
		result = 31 * result + (paramTable?.hashCode() ?: 0)
		return result
	}

	private fun timeString(): String {
		val time1 = (localForecastTime[Calendar.DAY_OF_MONTH].toString() + "."
				+ (localForecastTime[Calendar.MONTH] + 1) + "."
				+ localForecastTime[Calendar.YEAR] + "  "
				+ localForecastTime[Calendar.HOUR_OF_DAY] + ":"
				+ localForecastTime[Calendar.MINUTE])
		val time2 = (forecastTime2[Calendar.DAY_OF_MONTH].toString() + "."
				+ (localForecastTime[Calendar.MONTH] + 1) + "."
				+ localForecastTime[Calendar.YEAR] + "  "
				+ localForecastTime[Calendar.HOUR_OF_DAY] + ":"
				+ localForecastTime[Calendar.MINUTE])
		return if (timeRange == null) time1 else "${timeRange}${time1}${connector}${time2}"
	}

	override fun toString(): String = listOfNotNull(
			"GRIB1 PDS header:",
			"\tCenter: ${centreId}",
			"\tSub-Center: ${subcenterId}",
			"\tTable: ${tableVersion}",
			"\tGrid ID: ${gridId}",
			"\tTime: ${timeString()} (dd.mm.yyyy hh:mm)",
			"\tType: ${parameterAbbreviation}",
			"\tDescription: ${parameterDescription}",
			"\tUnit: ${parameterUnits}",
			"\tTable version: ${tableVersion}",
			"\t${pDSLevel} decimal scale: ${decimalScale}",
			(if (gdsExists) "\tGDS exists" else null),
			(if (bmsExists) "\tBMS exists" else null))
			.joinToString("\n")
}