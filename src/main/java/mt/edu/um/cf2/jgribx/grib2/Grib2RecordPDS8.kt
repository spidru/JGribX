package mt.edu.um.cf2.jgribx.grib2

import mt.edu.um.cf2.jgribx.GribInputStream
import mt.edu.um.cf2.jgribx.GribOutputStream
import java.util.*

/**
 * ### [Product Definition Template 4.0: Analysis or forecast at a horizontal level or in a horizontal layer at a point in time](https://www.nco.ncep.noaa.gov/pmb/docs/grib2/grib2_doc/grib2_temp4-0.shtml)
 *
 *    | Octet |  #  | Value                                                                                           |
 *    |-------|-----|-------------------------------------------------------------------------------------------------|
 *    | 10    | `1` | Parameter category (see Code Table 4.1)                                                         |
 *    | 11    | `1` | Parameter number (see Code table 4.2)                                                           |
 *    | 12    | `1` | Type of generating process (see Code table 4.3)                                                 |
 *    | 13    | `1` | Background generating process identifier (defined by originating centre)                        |
 *    | 14    | `1` | Analysis or forecast generating process identified (see Code ON388 Table A)                     |
 *    | 15-16 | `2` | Hours of observational data cutoff after reference time (see Note)                              |
 *    | 17    | `1` | Minutes of observational data cutoff after reference time (see Note)                            |
 *    | 18    | `1` | Indicator of unit of time range (see Code table 4.4)                                            |
 *    | 19-22 | `4` | Forecast time in units defined by octet 18                                                      |
 *    | 23    | `1` | Type of first fixed surface (see Code table 4.5)                                                |
 *    | 24    | `1` | Scale factor of first fixed surface                                                             |
 *    | 25-28 | `4` | Scaled value of first fixed surface                                                             |
 *    | 29    | `1` | Type of second fixed surfaced (see Code table 4.5)                                              |
 *    | 30    | `1` | Scale factor of second fixed surface                                                            |
 *    | 31-34 | `4` | Scaled value of second fixed surfaces                                                           |
 *    | 35-36 | `2` | Year  ― Time of end of overall time interval                                                    |
 *    | 37    | `1` | Month  ― Time of end of overall time interval                                                   |
 *    | 38    | `1` | Day  ― Time of end of overall time interval                                                     |
 *    | 39    | `1` | Hour  ― Time of end of overall time interval                                                    |
 *    | 40    | `1` | Minute  ― Time of end of overall time interval                                                  |
 *    | 41    | `1` | Second  ― Time of end of overall time interval                                                  |
 *    | 42    | `1` | n ― number of time ranges specifications describing the time intervals used to calculate the    |
 *    |       |     | statistically-processed field                                                                   |
 *    | 43-46 | `4` | Total number of data values missing in statistical process                                      |
 *    |-------|-----|-------------------------------------------------------------------------------------------------|
 *    | 47 - 58 Specification of the outermost (or only) time range over which statistical processing is done         |
 *    |-------|-----|-------------------------------------------------------------------------------------------------|
 *    | 47    | `1` | Statistical process used to calculate the processed field from the field at each time increment |
 *    |       |     | during the time range (see Code Table 4.10)                                                     |
 *    | 48    | `1` | Type of time increment between successive fields used in the statistical processing (see Code   |
 *    |       |     | Table 4.11)                                                                                     |
 *    | 49    | `1` | Indicator of unit of time for time range over which statistical processing is done (see Code    |
 *    |       |     | Table 4.4)                                                                                      |
 *    | 50-53 | `4` | Length of the time range over which statistical processing is done, in units defined by the     |
 *    |       |     | previous octet                                                                                  |
 *    | 54    | `1` | Indicator of unit of time for the increment between the successive fields used (see Code        |
 *    |       |     | Table 4.4)                                                                                      |
 *    | 55-58 | `4` | Time increment between successive fields, in units defined by the previous octet (see Notes 3   |
 *    |       |     | and 4)                                                                                          |
 *    |-------|-----|-------------------------------------------------------------------------------------------------|
 *    | 59 - nn These octets are included only if n>1, where nn = 46 + 12 x n                                         |
 *    |-------|-----|-------------------------------------------------------------------------------------------------|
 *    | 59-70 | `12`| As octets 47 to 58, next innermost step of processing                                           |
 *    | 71-nn |     | Additional time range specifications, included in accordance with the value of n. Contents as   |
 *    |       |     | octets 47 to 58, repeated as necessary                                                          |
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
 * @param overallTimeIntervalEnd        (`35-41`) Time of end of overall time interval
 * @param nMissingValues                (`43-46`) Total number of data values missing in statistical process
 * @param statsProcess                  (`47`)    Statistical process used to calculate the processed field from the
 *                                                field at each time increment during the time range (see
 *                                                [Code Table 4.10](https://www.nco.ncep.noaa.gov/pmb/docs/grib2/grib2_doc/grib2_table4-10.shtml))
 * @param timeIncrementType             (`48`)    Type of time increment between successive fields used in the
 *                                                statistical processing (see
 *                                                [Code Table 4.11](https://www.nco.ncep.noaa.gov/pmb/docs/grib2/grib2_doc/grib2_table4-11.shtml))
 * @param timeUnitIndicator1            (`49`)    Indicator of unit of time for time range over which statistical
 *                                                processing is done (see
 *                                                [Code Table 4.4](https://www.nco.ncep.noaa.gov/pmb/docs/grib2/grib2_doc/grib2_table4-4.shtml))
 * @param timeRangeLength               (`50-53`) Length of the time range over which statistical processing is done,
 *                                                in units defined by the previous octet
 * @param timeUnitIndicator2            (`54`)    Indicator of unit of time for the increment between the successive
 *                                                fields used (see
 *                                                [Code Table 4.4](https://www.nco.ncep.noaa.gov/pmb/docs/grib2/grib2_doc/grib2_table4-4.shtml))
 * @param timeIncrement                 (`55-58`) Time increment between successive fields, in units defined by the
 *                                                previous octet (see Notes 3 and 4)
 *
 * @author Jan Kubovy [jan@kubovy.eu]
 */
class Grib2RecordPDS8 internal constructor(
		numberOfCoordinates: Int,
		parameter: Grib2Parameter,
		genProcessType: Int,
		backgroundGeneratingProcessId: Int,
		processId: Int,
		observationalHours: Int,
		cutoffMinutes: Int,
		timeRangeUnitIndicator: Int,
		forecastTimeAhead: Int,
		referenceTime: Calendar,
		level1Type: Int,
		level1ScaleFactor: Int,
		level1ScaledValue: Int,
		level2Type: Int,
		level2ScaleFactor: Int,
		level2ScaledValue: Int,

		private val overallTimeIntervalEnd: Calendar,
		private val nMissingValues: Int,
		private val statsProcess: IntArray,
		private val timeIncrementType: IntArray,
		private val timeUnitIndicator1: IntArray,
		private val timeRangeLength: IntArray,
		private val timeUnitIndicator2: IntArray,
		private val timeIncrement: IntArray) :

		Grib2RecordPDS0(numberOfCoordinates, parameter, genProcessType, backgroundGeneratingProcessId, processId,
				observationalHours, cutoffMinutes, timeRangeUnitIndicator, forecastTimeAhead, referenceTime, level1Type,
				level1ScaleFactor, level1ScaledValue, level2Type, level2ScaleFactor, level2ScaledValue) {

	companion object {
		fun readFromStream(gribInputStream: GribInputStream,
						   discipline: ProductDiscipline,
						   referenceTime: Calendar,
						   numberOfCoordinates: Int): Grib2RecordPDS8 {
			val pds0 = Grib2RecordPDS0.readFromStream(gribInputStream, discipline, referenceTime, numberOfCoordinates)

			/* [35-36] Year  ― Time of end of overall time interval */
			val year = gribInputStream.readUInt(2)

			/* [37] Month  ― Time of end of overall time interval */
			val month = gribInputStream.readUInt(1)

			/* [38] Day  ― Time of end of overall time interval */
			val day = gribInputStream.readUInt(1)

			/* [39] Hour  ― Time of end of overall time interval */
			val hour = gribInputStream.readUInt(1)

			/* [40] Minute  ― Time of end of overall time interval */
			val minute = gribInputStream.readUInt(1)

			/* [41] Second  ― Time of end of overall time interval */
			val second = gribInputStream.readUInt(1)
			val overallTimeIntervalEnd = GregorianCalendar(year, month - 1, day, hour, minute, second)
					.apply { timeZone = TimeZone.getTimeZone("UTC") }

			/* [42] n ― number of time ranges specifications describing the time intervals used to calculate the statistically-processed field */
			val nTimeRanges = gribInputStream.readUInt(1)

			/* [43-46] Total number of data values missing in statistical process */
			val nMissingValues = gribInputStream.readUInt(4)

			val statsProcess = IntArray(nTimeRanges)
			val timeIncrementType = IntArray(nTimeRanges)
			val timeUnitIndicator1 = IntArray(nTimeRanges)
			val timeRangeLength = IntArray(nTimeRanges)
			val timeUnitIndicator2 = IntArray(nTimeRanges)
			val timeIncrement = IntArray(nTimeRanges)
			repeat(nTimeRanges) {
				/* [47] Statistical process used to calculate the processed field from the field at each time increment during the time range (see Code Table 4.10) */
				statsProcess[it] = gribInputStream.readUInt(1)

				/* [48] Type of time increment between successive fields used in the statistical processing (see Code Table 4.11) */
				timeIncrementType[it] = gribInputStream.readUInt(1)

				/* [49] Indicator of unit of time for time range over which statistical processing is done (see Code Table 4.4) */
				timeUnitIndicator1[it] = gribInputStream.readUInt(1)

				/* [50-53] Length of the time range over which statistical processing is done, in units defined by the previous octet */
				timeRangeLength[it] = gribInputStream.readUInt(4)

				/* [54] Indicator of unit of time for the increment between the successive fields used (see Code Table 4.4) */
				timeUnitIndicator2[it] = gribInputStream.readUInt(1)

				/* [55-58] Time increment between successive fields, in units defined by the previous octet (see Notes 3 and 4)  */
				timeIncrement[it] = gribInputStream.readUInt(4)
			}
			return Grib2RecordPDS8(pds0, overallTimeIntervalEnd, nMissingValues, statsProcess, timeIncrementType,
					timeUnitIndicator1, timeRangeLength, timeUnitIndicator2, timeIncrement)
		}
	}

	override val length: Int
		get() = 46 + 12 * nTimeRanges

	override val templateId: Int = 8

	private val nTimeRanges: Int
		get() = statsProcess.size


	private constructor(pds0: Grib2RecordPDS0, overallTimeIntervalEnd: Calendar, nMissingValues: Int,
						statsProcess: IntArray, timeIncrementType: IntArray, timeUnitIndicator1: IntArray,
						timeRangeLength: IntArray, timeUnitIndicator2: IntArray, timeIncrement: IntArray) :
			this(pds0.numberOfCoordinates, pds0.parameter, pds0.genProcessType, pds0.backgroundGeneratingProcessId,
					pds0.processId, pds0.observationalHours, pds0.cutoffMinutes, pds0.timeRangeUnitIndicator,
					pds0.forecastTimeAhead, pds0.referenceTime, pds0.level1Type, pds0.level1ScaleFactor,
					pds0.level1ScaledValue, pds0.level2Type, pds0.level2ScaleFactor, pds0.level2ScaledValue,

					overallTimeIntervalEnd, nMissingValues, statsProcess, timeIncrementType, timeUnitIndicator1,
					timeRangeLength, timeUnitIndicator2, timeIncrement)

	override fun writeTo(outputStream: GribOutputStream) {
		super.writeTo(outputStream) // [1-5] length, section number, [6-9] PDS global stuff

		outputStream.writeUInt(overallTimeIntervalEnd.get(Calendar.YEAR), bytes = 2) // [35-36]
		outputStream.writeUInt(overallTimeIntervalEnd.get(Calendar.MONTH) + 1, bytes = 1) // [37]
		outputStream.writeUInt(overallTimeIntervalEnd.get(Calendar.DAY_OF_MONTH), bytes = 1) // [38]
		outputStream.writeUInt(overallTimeIntervalEnd.get(Calendar.HOUR_OF_DAY), bytes = 1) // [39]
		outputStream.writeUInt(overallTimeIntervalEnd.get(Calendar.MINUTE), bytes = 1) // [40]
		outputStream.writeUInt(overallTimeIntervalEnd.get(Calendar.SECOND), bytes = 1) // [41]

		outputStream.writeUInt(nTimeRanges, bytes = 1) // [42]
		outputStream.writeUInt(nMissingValues, bytes = 4) // [43-46]

		statsProcess.indices.forEach {
			outputStream.writeUInt(statsProcess[it], bytes = 1) // [47]
			outputStream.writeUInt(timeIncrementType[it], bytes = 1) // [48]
			outputStream.writeUInt(timeUnitIndicator1[it], bytes = 1) // [49]
			outputStream.writeUInt(timeRangeLength[it], bytes = 4) // [50-53]
			outputStream.writeUInt(timeUnitIndicator2[it], bytes = 1) // [54]
			outputStream.writeUInt(timeIncrement[it], bytes = 4) // [55-58]
		}
	}


	override fun equals(other: Any?) = this === other
			|| super.equals(other)
			&& other is Grib2RecordPDS8
			&& overallTimeIntervalEnd == other.overallTimeIntervalEnd
			&& nTimeRanges == other.nTimeRanges
			&& nMissingValues == other.nMissingValues
			&& statsProcess.contentEquals(other.statsProcess)
			&& timeIncrementType.contentEquals(other.timeIncrementType)
			&& timeUnitIndicator1.contentEquals(other.timeUnitIndicator1)
			&& timeRangeLength.contentEquals(other.timeRangeLength)
			&& timeUnitIndicator2.contentEquals(other.timeUnitIndicator2)
			&& timeIncrement.contentEquals(other.timeIncrement)

	override fun hashCode(): Int {
		var result = super.hashCode()
		result = 31 * result + overallTimeIntervalEnd.hashCode()
		result = 31 * result + nTimeRanges
		result = 31 * result + nMissingValues
		result = 31 * result + cutoffMinutes
		result = 31 * result + statsProcess.contentHashCode()
		result = 31 * result + timeIncrementType.contentHashCode()
		result = 31 * result + timeUnitIndicator1.contentHashCode()
		result = 31 * result + timeRangeLength.contentHashCode()
		result = 31 * result + timeUnitIndicator2.contentHashCode()
		result = 31 * result + timeIncrement.contentHashCode()
		return result
	}
}
