package mt.edu.um.cf2.jgribx.grib2

import mt.edu.um.cf2.jgribx.GribInputStream
import mt.edu.um.cf2.jgribx.GribOutputStream
import mt.edu.um.cf2.jgribx.Logger
import mt.edu.um.cf2.jgribx.NotSupportedException
import java.util.*
import kotlin.math.pow

/**
 * ### [Product Definition Template 4.0: Analysis or forecast at a horizontal level or in a horizontal layer at a point in time](https://www.nco.ncep.noaa.gov/pmb/docs/grib2/grib2_doc/grib2_temp4-0.shtml)
 *
 *    | Octet |  #  | Value                                                                                   |
 *    |-------|-----|-----------------------------------------------------------------------------------------|
 *    | 10    | `1` | Parameter category (see Code table 4.1)                                                 |
 *    | 11    | `1` | Parameter number (see Code table 4.2)                                                   |
 *    | 12    | `1` | Type of generating process (see Code table 4.3)                                         |
 *    | 13    | `1` | Background generating process identifier (defined by originating centre)                |
 *    | 14    | `1` | Analysis or forecast generating process identified (see Code ON388 Table A)             |
 *    | 15-16 | `2` | Hours of observational data cutoff after reference time (see Note)                      |
 *    | 17    | `1` | Minutes of observational data cutoff after reference time (see Note)                    |
 *    | 18    | `1` | Indicator of unit of time range (see Code table 4.4)                                    |
 *    | 19-22 | `4` | Forecast time in units defined by octet 18                                              |
 *    | 23    | `1` | Type of first fixed surface (see Code table 4.5)                                        |
 *    | 24    | `1` | Scale factor of first fixed surface                                                     |
 *    | 25-28 | `4` | Scaled value of first fixed surface                                                     |
 *    | 29    | `1` | Type of second fixed surfaced (see Code table 4.5)                                      |
 *    | 30    | `1` | Scale factor of second fixed surface                                                    |
 *    | 31-34 | `4` | Scaled value of second fixed surfaces                                                   |
 *
 * @param numberOfCoordinates           (`6-7`)   Number of coordinate values after template (see note 1 below)
 * @param parameter                               Parameter
 * @param genProcessType                (`12`)    Type of generating process (see
 *                                                [Code table 4.3](https://www.nco.ncep.noaa.gov/pmb/docs/grib2/grib2_doc/grib2_table4-3.shtml))
 * @param backgroundGeneratingProcessId (`13`)    Background generating process identifier (defined by originating centre)
 * @param processId                     (`14`)    Analysis or forecast generating process identified (see
 *                                                [Code ON388 Table A](https://www.nco.ncep.noaa.gov/pmb/docs/on388/tablea.html))
 * @param observationalHours            (`15-16`) Hours of observational data cutoff after reference time (see Note)
 * @param cutoffMinutes                 (`17`)    Minutes of observational data cutoff after reference time (see Note)
 * @param timeRangeUnitIndicator        (`18`)    Indicator of unit of time range (see
 *                                                [Code table 4.4](https://www.nco.ncep.noaa.gov/pmb/docs/grib2/grib2_doc/grib2_table4-4.shtml))
 * @param forecastTimeAhead             (`19-22`) Forecast time in units defined by octet `18`
 * @param referenceTime                           [Reference time][Grib2RecordIDS.referenceTime] for forecast time
 *                                                calculation
 * @param level1Type                    (`23`)    Type of first fixed surface (see
 *                                                [Code table 4.5](https://www.nco.ncep.noaa.gov/pmb/docs/grib2/grib2_doc/grib2_table4-5.shtml))
 * @param level1ScaleFactor             (`24`)    Scale factor of first fixed surface
 * @param level1ScaledValue             (`25-28`) Scaled value of first fixed surface
 * @param level2Type                    (`29`)    Type of second fixed surfaced (see
 *                                                [Code table 4.5](https://www.nco.ncep.noaa.gov/pmb/docs/grib2/grib2_doc/grib2_table4-5.shtml))
 * @param level2ScaleFactor             (`30`)    Scale factor of second fixed surface
 * @param level2ScaledValue             (`31-34`) Scaled value of second fixed surfaces
 *
 * @author Jan Kubovy [jan@kubovy.eu]
 */
class Grib2RecordPDS0 internal constructor(numberOfCoordinates: Int,
										   parameter: Grib2Parameter,
										   private val genProcessType: Int,
										   private val backgroundGeneratingProcessId: Int,
										   processId: Int,
										   private val observationalHours: Int,
										   private val cutoffMinutes: Int,
										   private val timeRangeUnitIndicator: Int,
										   private val forecastTimeAhead: Int,
										   referenceTime: Calendar,
										   private val level1Type: Int,
										   private val level1ScaleFactor: Int,
										   private val level1ScaledValue: Int,
										   private val level2Type: Int,
										   private val level2ScaleFactor: Int,
										   private val level2ScaledValue: Int) :
		Grib2RecordPDS(numberOfCoordinates, parameter, processId) {


	companion object {
		internal fun calculateForecastTime(referenceTime: Calendar, timeRangeUnitIndicator: Int, forecastTimeAhead: Int): Calendar {
			val forecastTime = referenceTime.clone() as Calendar
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
			return forecastTime
		}

		fun readFromStream(gribInputStream: GribInputStream,
						   discipline: ProductDiscipline,
						   referenceTime: Calendar,
						   numberOfCoordinates: Int): Grib2RecordPDS0 {
			/* [10] Parameter category */
			val paramCategory = gribInputStream.readUINT(1)

			/* [11] Parameter number */
			val paramNumber = gribInputStream.readUINT(1)
			if (!Grib2Parameter.isDefaultLoaded) Grib2Parameter.loadDefaultParameters()
			val parameter = Grib2Parameter.getParameter(discipline, paramCategory, paramNumber)
					?: throw NotSupportedException("Unsupported parameter: D:${discipline} C:${paramCategory} N:${paramNumber}")

			/* [12] Type of generating process */
			val genProcessType = gribInputStream.readUINT(1)

			/* [13] Background generating process identifier (defined by originating centre) */
			val backgroundGeneratingProcessId = gribInputStream.readUINT(1)

			/* [14] Analysis or forecast generating process identifier */
			val processId = gribInputStream.readUINT(1)

			/* [15-16] Hours of observational data cutoff after reference time (see Note) */
			val observationalHours = gribInputStream.readUINT(2)

			/* [17] Minutes of observational data cutoff after reference time (see Note) */
			val cutoffMinutes = gribInputStream.readUINT(1)

			/* [18] Indicator of unit of time range (see Code table 4.4) */
			val timeRangeUnitIndicator = gribInputStream.readUINT(1)

			/* [19-22] Forecast time in units defined in octet 18 */
			val forecastTimeAhead = gribInputStream.readUINT(4)

			/* [23] Type of first fixed surface (see Code table 4.5) */
			val level1Type = gribInputStream.readUINT(1)

			/* [24] Scale factor of first fixed surface */
			val level1ScaleFactor = gribInputStream.readUINT(1)

			/* [25-28] Scaled value of first fixed surface */
			val level1ScaledValue = gribInputStream.readUINT(4)

			/* [29] Type of second fixed surface */
			val level2Type = gribInputStream.readUINT(1)

			/* [30] Scale factor of second fixed surface */
			val level2ScaleFactor = gribInputStream.readUINT(1)

			/* [31-34] Scaled value of second fixed surface */
			val level2ScaledValue = gribInputStream.readUINT(4)
			if (level2Type != 255) Logger.error("Second surface is not yet supported")

			return Grib2RecordPDS0(numberOfCoordinates, parameter, genProcessType,
					backgroundGeneratingProcessId, processId, observationalHours, cutoffMinutes, timeRangeUnitIndicator,
					forecastTimeAhead, referenceTime,
					level1Type, level1ScaleFactor, level1ScaledValue,
					level2Type, level2ScaleFactor, level2ScaledValue)
		}
	}

	override val length: Int = 34

	override val templateId: Int = 0

	override val forecastTime: Calendar = calculateForecastTime(referenceTime, timeRangeUnitIndicator, forecastTimeAhead)

	override val level1: Grib2Level
	override val level2: Grib2Level?

	private val generatingProcessType: String
		get() = when (genProcessType) {
			0 -> "Analysis"
			1 -> "Initialisation"
			2 -> "Forecast"
			3 -> "Bias Corrected Forecast"
			4 -> "Ensemble Forecast"
			else -> "Unsupported"
		}

	init {
		val level1Value = level1ScaledValue / 10.0.pow(level1ScaleFactor.toDouble()).toFloat()
		level1 = Grib2Level.getLevel(level1Type, level1Value)
				?: throw NotSupportedException("Unsupported level of type ${level1Type}")
		val level2Value = level2ScaledValue / 10.0.pow(level2ScaleFactor.toDouble()).toFloat()
		level2 = Grib2Level.getLevel(level2Type, level2Value)
	}

	override fun writeTo(outputStream: GribOutputStream) {
		super.writeTo(outputStream) // [1-5] length, section number, [6-9] PDS global stuff
		outputStream.writeUInt(parameter.category.value, bytes = 1) // [10]
		outputStream.writeUInt(parameter.id, bytes = 1) // [11]
		outputStream.writeUInt(genProcessType, bytes = 1) // [12]
		outputStream.writeUInt(backgroundGeneratingProcessId, bytes = 1) // [13]
		outputStream.writeUInt(processId, bytes = 1) // [14]
		outputStream.writeUInt(observationalHours, bytes = 2) // [15-16]
		outputStream.writeUInt(cutoffMinutes, bytes = 1) // [17]
		outputStream.writeUInt(timeRangeUnitIndicator, bytes = 1) // [18]
		outputStream.writeUInt(forecastTimeAhead, bytes = 4) // [19-22]
		outputStream.writeUInt(level1Type, bytes = 1) // [23]
		outputStream.writeUInt(level1ScaleFactor, bytes = 1) // [24]
		outputStream.writeUInt(level1ScaledValue, bytes = 4) // [25-28]
		outputStream.writeUInt(level2Type, bytes = 1) // [29]
		outputStream.writeUInt(level2ScaleFactor, bytes = 1) // [30]
		outputStream.writeUInt(level2ScaledValue, bytes = 4) // [31-34]
	}


	override fun equals(other: Any?) = this === other
			|| super.equals(other)
			&& other is Grib2RecordPDS0
			&& genProcessType == other.genProcessType
			&& backgroundGeneratingProcessId == other.backgroundGeneratingProcessId
			&& observationalHours == other.observationalHours
			&& cutoffMinutes == other.cutoffMinutes
			&& timeRangeUnitIndicator == other.timeRangeUnitIndicator
			&& forecastTimeAhead == other.forecastTimeAhead
			&& level1Type == other.level1Type
			&& level1ScaleFactor == other.level1ScaleFactor
			&& level1ScaledValue == other.level1ScaledValue
			&& level2Type == other.level2Type
			&& level2ScaleFactor == other.level2ScaleFactor
			&& level2ScaledValue == other.level2ScaledValue

	override fun hashCode(): Int {
		var result = super.hashCode()
		result = 31 * result + genProcessType
		result = 31 * result + backgroundGeneratingProcessId
		result = 31 * result + observationalHours
		result = 31 * result + cutoffMinutes
		result = 31 * result + timeRangeUnitIndicator
		result = 31 * result + forecastTimeAhead
		result = 31 * result + level1Type
		result = 31 * result + level1ScaleFactor
		result = 31 * result + level1ScaledValue
		result = 31 * result + level2Type
		result = 31 * result + level2ScaleFactor
		result = 31 * result + level2ScaledValue
		return result
	}


	override fun toString(): String = listOfNotNull(
			"GRIB2 PDS section:",
			"\tAbbrev: ${parameter.code}",
			"\tDescription: ${parameter.description}",
			"\tUnits: ${parameter.units}",
			"\tType: ${generatingProcessType}")
			.joinToString("\n")
}
