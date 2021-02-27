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
import mt.edu.um.cf2.jgribx.GribOutputStream
import mt.edu.um.cf2.jgribx.Logger
import mt.edu.um.cf2.jgribx.NoValidGribException
import java.util.*

/**
 * ### [Section 1: Identification section](https://www.nco.ncep.noaa.gov/pmb/docs/grib2/grib2_doc/grib2_sect1.shtml)
 *
 *    | Octet | # | Value                                                                             |
 *    |-------|---|-----------------------------------------------------------------------------------|
 *    | 1-4   | 4 | Length of the section in octets (21 or N)                                         |
 *    | 5     | 1 | Number of the section (1)                                                         |
 *    | 6-7   | 2 | Identification of originating/generating center (See Table 0) (see note 4)        |
 *    | 8-9   | 2 | Identification of originating/generating subcenter (see Table C)                  |
 *    | 10    | 1 | GRIB master tables version number (currently 2) (See Table 1.0) (see note 1)      |
 *    | 11    | 1 | Version number of GRIB local tables used to augment Master Tables (see Table 1.1) |
 *    | 12    | 1 | Significance of reference time (see Table 1.2)                                    |
 *    | 13-14 | 2 | Year (4 digits)                                                                   |
 *    | 15    | 1 | Month                                                                             |
 *    | 16    | 1 | Day                                                                               |
 *    | 17    | 1 | Hour                                                                              |
 *    | 18    | 1 | Minute                                                                            |
 *    | 19    | 1 | Second                                                                            |
 *    | 20    | 1 | Production Status of Processed data in the GRIB message (see Table 1.3)           |
 *    | 21    | 1 | Type of processed data in this GRIB message (see Table 1.4)                       |
 *    | 22-N  |   | Reserved                                                                          |
 *
 * @param centreId           (`6-7`)   Identification of originating/generating center (see
 *                                     [Table 0](https://www.nco.ncep.noaa.gov/pmb/docs/on388/table0.html)) (see note 4)
 * @param origSubCentreId    (`8-9`)   Identification of originating/generating subcenter (see
 *                                     [Table C](https://www.nco.ncep.noaa.gov/pmb/docs/on388/tablec.html))
 * @param masterTableVersion (`10`)    GRIB master tables version number (currently 2) (see
 *                                     [Table 1.0](https://www.nco.ncep.noaa.gov/pmb/docs/grib2/grib2_doc/grib2_table1-0.shtml))
 *                                     (see note 1)
 * @param localTableVersion  (`11`)    Version number of GRIB local tables used to augment Master Tables (see
 *                                     [Table 1.1](https://www.nco.ncep.noaa.gov/pmb/docs/grib2/grib2_doc/grib2_table1-1.shtml))
 * @param refTimeSig         (`12`)    Significance of reference time (see
 *                                     [Table 1.2](https://www.nco.ncep.noaa.gov/pmb/docs/grib2/grib2_doc/grib2_table1-2.shtml))
 * @param referenceTime      (`13-14`) Year (4 digits)
 *                           (`15`)    Month
 *                           (`16`)    Day
 *                           (`17`)    Hour
 *                           (`18`)    Minute
 *                           (`19`)    Second
 * @param dataProdStatus     (`20`)    Production Status of Processed data in the GRIB message (see
 *                                     [Table 1.3](https://www.nco.ncep.noaa.gov/pmb/docs/grib2/grib2_doc/grib2_table1-3.shtml))
 * @param dataType           (`21`)    Type of processed data in this GRIB message (see
 *                                     [Table 1.4](https://www.nco.ncep.noaa.gov/pmb/docs/grib2/grib2_doc/grib2_table1-4.shtml))
 * @param reserved           (`22-N`)  Reserved (here just for writing purposes)
 *
 * @author spidru
 * @author Jan Kubovy [jan@kubovy.eu]
 */
class Grib2RecordIDS internal constructor(val centreId: Int,
										  internal val origSubCentreId: Int,
										  internal val masterTableVersion: Int,
										  internal val localTableVersion: Int,
										  internal val refTimeSig: Int,
										  val referenceTime: Calendar,
										  internal val dataProdStatus: Int,
										  internal val dataType: Int,
										  internal val reserved: ByteArray?) : Grib2Section {

	companion object {
		internal fun readFromStream(gribInputStream: GribInputStream, readEntire: Boolean = false): Grib2RecordIDS {
			/* [1-5] Length, section number */
			val length = Grib2Section.readFromStream(gribInputStream, 1)

			/* [6-7] Originating Centre ID */
			val centreId = gribInputStream.readUInt(2)

			/* [8-9] Originating Sub-centre ID */
			val origSubCentreId = gribInputStream.readUInt(2)

			/* [10] Master Tables Version Number */
			val masterTableVersion = gribInputStream.readUInt(1)

			/* [11] Local Tables Version Number */
			val localTableVersion = gribInputStream.readUInt(1)

			/* [12] Reference Time Significance */
			val refTimeSig = gribInputStream.readUInt(1)

			/* [13-14] Reference Year */
			val year = gribInputStream.readUInt(2)

			/* [15] Reference Month */
			val month = gribInputStream.readUInt(1)

			/* [16] Reference Day */
			val day = gribInputStream.readUInt(1)

			/* [17] Hour */
			val hour = gribInputStream.readUInt(1)

			/* [18] Minute */
			val minute = gribInputStream.readUInt(1)

			/* [19] Second */
			val second = gribInputStream.readUInt(1)
			val referenceTime = GregorianCalendar(year, month - 1, day, hour, minute, second)
					.apply { timeZone = TimeZone.getTimeZone("UTC") }

			/* [20] Data Production Status */
			val dataProdStatus = gribInputStream.readUInt(1)

			/* [21] Data Type */
			val dataType = gribInputStream.readUInt(1)

			/* [22-N] Additional Data */
			var reserved: ByteArray? = null
			if (readEntire) {
				reserved = ByteArray(length - 21)
				gribInputStream.read(reserved)
			} else if (length > 21) {
				Logger.error("Length is greater than 21. Please review code.")
				gribInputStream.skip(length - 21L)
			}

			return Grib2RecordIDS(centreId, origSubCentreId, masterTableVersion, localTableVersion,
					refTimeSig, referenceTime, dataProdStatus, dataType, reserved)
					.takeIf { it.length == length || !readEntire } ?: throw NoValidGribException("IDS length mismatch")
		}
	}

	override val length: Int
		get() = 21 + (reserved?.size ?: 0)

	override val number: Int = 1

	override fun writeTo(outputStream: GribOutputStream) {
		super.writeTo(outputStream) // [1-5] length, section number
		outputStream.writeUInt(centreId, bytes = 2) // [6-7] Originating Centre ID
		outputStream.writeUInt(origSubCentreId, bytes = 2) // [8-9] Originating Sub-centre ID
		outputStream.writeUInt(masterTableVersion, bytes = 1) // [10] Master Tables Version Number
		outputStream.writeUInt(localTableVersion, bytes = 1) // [11] Local Tables Version Number
		outputStream.writeUInt(refTimeSig, bytes = 1) // [12] Reference Time Significance
		outputStream.writeUInt(referenceTime.get(Calendar.YEAR), bytes = 2) // [13-14] Reference Year
		outputStream.writeUInt(referenceTime.get(Calendar.MONTH) + 1, bytes = 1) // [15] Reference Month
		outputStream.writeUInt(referenceTime.get(Calendar.DAY_OF_MONTH), bytes = 1) // [16] Reference Day
		outputStream.writeUInt(referenceTime.get(Calendar.HOUR_OF_DAY), bytes = 1) // [17] Hour
		outputStream.writeUInt(referenceTime.get(Calendar.MINUTE), bytes = 1) // [18] Minute
		outputStream.writeUInt(referenceTime.get(Calendar.SECOND), bytes = 1) // [19] Second
		outputStream.writeUInt(dataProdStatus, bytes = 1) // [20] Data Production Status
		outputStream.writeUInt(dataType, bytes = 1) // [21] Data Type
		reserved?.also { outputStream.write(it) } // [22-N] Additional Data
	}

	override fun equals(other: Any?) = this === other
			|| other is Grib2RecordIDS
			&& length == other.length
			&& number == other.number
			&& centreId == other.centreId
			&& origSubCentreId == other.origSubCentreId
			&& masterTableVersion == other.masterTableVersion
			&& localTableVersion == other.localTableVersion
			&& refTimeSig == other.refTimeSig
			&& referenceTime == other.referenceTime
			&& dataProdStatus == other.dataProdStatus
			&& dataType == other.dataType
			&& reserved.contentEquals(other.reserved)

	override fun hashCode(): Int {
		var result = length
		result = 31 * result + number
		result = 31 * result + centreId
		result = 31 * result + origSubCentreId
		result = 31 * result + masterTableVersion
		result = 31 * result + localTableVersion
		result = 31 * result + refTimeSig
		result = 31 * result + referenceTime.hashCode()
		result = 31 * result + dataProdStatus
		result = 31 * result + dataType
		result = 31 * result + (reserved?.contentHashCode() ?: 0)
		return result
	}

	override fun toString(): String = listOfNotNull(
			"GRIB2 IDS section:",
			"\tWeather centre ${centreId}",
			when (dataType) {
				0 -> "\tAnalysis products"
				1 -> "\tForecast products"
				2 -> "\tAnalysis and forecast products"
				3 -> "\tControl forecast products"
				else -> "\tUnsupported type"
			})
			.joinToString("\n")
}
