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
package mt.edu.um.cf2.jgribx.grib2

import mt.edu.um.cf2.jgribx.GribInputStream
import mt.edu.um.cf2.jgribx.Logger
import mt.edu.um.cf2.jgribx.NotSupportedException
import java.util.*
import kotlin.math.pow

/**
 * A class representing the product definition section (PDS) of a GRIB record.
 *
 * Constructs a [Grib2RecordPDS] object from a bit input stream.
 *
 * @param gribInputStream bit input stream with PDS content
 * @param discipline
 * @param referenceTime
 *
 * @throws java.io.IOException if stream can not be opened etc.
 * @throws NotSupportedException
 */
class Grib2RecordPDS(gribInputStream: GribInputStream, discipline: ProductDiscipline, referenceTime: Calendar) {
	/** The time at which the forecast applies. */
	var forecastTime: Calendar

	/** Length in bytes of this PDS. */
	var length: Int
		protected set

	/** The parameter in the form of [Grib2Parameter]. */
	var parameter: Grib2Parameter?
		protected set

	/** Analysis or forecast generating process identified. E.g. 81 (Analysis from GFS) */
	var processId = 0

	/** Type of generating process. E.g. 2 (Forecast) */
	private var genProcessType = 0

	var level: Grib2Level? = null

	/** The number of coordinates. */
	val numberOfCoordinates: Int

	private var paramCategory = 0

	/** Parameter Number (used when the appropriate parameter is not found) */
	private var paramNumber = 0

	private val templateId: Int

	val generatingProcessType: String
		get() = when (genProcessType) {
			0 -> "Analysis"
			1 -> "Initialisation"
			2 -> "Forecast"
			3 -> "Bias Corrected Forecast"
			4 -> "Ensemble Forecast"
			else -> "Unsupported"
		}

	/** The abbreviation representing the parameter. */
	val parameterAbbrev: String
		get() = parameter?.code ?: ""

	/** Description of the parameter. */
	val parameterDescription: String
		get() = parameter?.description ?: "Unknown Parameter: Category ${paramCategory} Number ${paramNumber}"

	val parameterUnits: String?
		get() = parameter?.units

	val levelCode: String
		get() = level?.code ?: ""

	val levelDescription: String
		get() = level?.description ?: ""

	val levelIdentifier: String
		get() = level?.levelIdentifier ?: ""

	init {
		/* [1-4] Section Length */
		length = gribInputStream.readUINT(4)

		/* [5] Section Number */
		val section = gribInputStream.readUINT(1)
		if (section != 4) Logger.warning("PDS contains invalid section number ${section}!")

		/* [6-7] Number of coordinate values after template */numberOfCoordinates = gribInputStream.readUINT(2)
		if (numberOfCoordinates > 0) throw NotSupportedException("Hybrid coordinate values are not yet supported")

		/* [8-9] Template number */
		templateId = gribInputStream.readUINT(2)
		when (templateId) {
			0 -> {
				/* [10] Parameter category */
				paramCategory = gribInputStream.readUINT(1)

				/* [11] Parameter number */
				paramNumber = gribInputStream.readUINT(1)
				if (!Grib2Parameter.isDefaultLoaded) Grib2Parameter.loadDefaultParameters()
				parameter = Grib2Parameter.getParameter(discipline, paramCategory, paramNumber)
				if (parameter == null) {
					//Logger.error("Unsupported parameter: D:${discipline} C:${paramCategory} N:${paramNumber}")
					//gribInputStream.skip(23)
					throw NotSupportedException("Unsupported parameter: D:${discipline} C:${paramCategory} N:${paramNumber}")
				}

				/* [12] Type of generating process */
				genProcessType = gribInputStream.readUINT(1)

				/* [13] Background generating process identifier (defined by originating centre) */
				/*val backgroundGeneratingProcessId = */gribInputStream.readUINT(1)

				/* [14] Analysis or forecast generating process identifier */
				processId = gribInputStream.readUINT(1)

				/* [15-16] Hours of observational data cutoff after reference time (see Note) */
				/*val observationalHours = */gribInputStream.readUINT(2)

				/* [17] Minutes of observational data cutoff after reference time (see Note) */
				gribInputStream.readUINT(1)

				/* [18] Indicator of unit of time range (see Code table 4.4) */
				val timeRangeUnitIndicator = gribInputStream.readUINT(1)

				/* [19-22] Forecast time in units defined in octet 18 */
				val forecastTimeAhead = gribInputStream.readUINT(4)
				forecastTime = referenceTime.clone() as Calendar
				when (timeRangeUnitIndicator) {
					0 -> forecastTime.add(Calendar.MINUTE, forecastTimeAhead) // Minute
					1 -> forecastTime.add(Calendar.HOUR_OF_DAY, forecastTimeAhead) // Hour
					2 -> forecastTime.add(Calendar.DAY_OF_MONTH, forecastTimeAhead) // Day
					3 -> forecastTime.add(Calendar.MONTH, forecastTimeAhead) // Month
					4 -> forecastTime.add(Calendar.YEAR, forecastTimeAhead) // Year
					5 -> forecastTime.add(Calendar.YEAR, forecastTimeAhead * 10) // Decade
					6 -> forecastTime.add(Calendar.YEAR, forecastTimeAhead * 30) // Normal (30 years)
					7 -> forecastTime.add(Calendar.YEAR, forecastTimeAhead * 100) // Century
					10 -> forecastTime.add(Calendar.HOUR_OF_DAY, forecastTimeAhead * 3) // 3 Hours
					11 -> forecastTime.add(Calendar.HOUR_OF_DAY, forecastTimeAhead * 6) // 6 Hours
					12 -> forecastTime.add(Calendar.HOUR_OF_DAY, forecastTimeAhead * 12) // 12 Hours
					13 -> forecastTime.add(Calendar.SECOND, forecastTimeAhead) // Second
					else -> throw NotSupportedException("Time range ${timeRangeUnitIndicator} is not supported yet")
				}

				/* [23] Type of first fixed surface (see Code table 4.5) */
				val level1Type = gribInputStream.readUINT(1)

				/* [24] Scale factor of first fixed surface */
				val level1ScaleFactor = gribInputStream.readUINT(1)

				/* [25-28] Scaled value of first fixed surface */
				val level1ScaledValue = gribInputStream.readUINT(4)

				/* [29] Type of second fixed surface */
				val level2Type = gribInputStream.readUINT(1)

				/* [30] Scale factor of second fixed surface */
				/*val level2ScaleFactor = */gribInputStream.readUINT(1)

				/* [31-34] Scaled value of second fixed surface */
				/*val level2ScaledValue = */gribInputStream.readUINT(4)

				/* PROCESSING */
				val level1Value = level1ScaledValue / 10.0.pow(level1ScaleFactor.toDouble()).toFloat()
				//val level2Value = level2ScaledValue / 10.0.pow(level2ScaleFactor.toDouble()).toFloat()
				level = Grib2Level.getLevel(level1Type, level1Value)
				if (level == null) {
					//Logger.error("Unsupported level of type ${level1Type}")
					throw NotSupportedException("Unsupported level of type ${level1Type}")
				}
				if (level2Type != 255) Logger.error("Second surface is not yet supported")
			}
			else -> {
				gribInputStream.skip(length - 9L)
				throw NotSupportedException("Unsupported template number: ${templateId}")
			}
		}
	}

	override fun equals(other: Any?): Boolean = this === other
			|| other is Grib2RecordPDS
			&& other.length == length
			&& other.parameter == parameter

	override fun hashCode(): Int {
		var result = forecastTime.hashCode()
		result = 31 * result + length
		result = 31 * result + (parameter?.hashCode() ?: 0)
		result = 31 * result + processId
		result = 31 * result + genProcessType
		result = 31 * result + (level?.hashCode() ?: 0)
		result = 31 * result + numberOfCoordinates
		result = 31 * result + paramCategory
		result = 31 * result + paramNumber
		result = 31 * result + templateId
		return result
	}

	override fun toString(): String = listOfNotNull(
			"GRIB2 PDS section:",
			"\tAbbrev: ${parameterAbbrev}",
			"\tDescription: ${parameterDescription}",
			"\tUnits: ${parameterUnits}",
			"\tType: ${generatingProcessType}")
			.joinToString("\n")
}
