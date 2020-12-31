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
import java.util.*

/**
 * @property length The length of the [Grib2RecordIDS] record in bytes.
 * @property centreId The ID of the originating centre.
 * @author spidru
 */
class Grib2RecordIDS private constructor(val length: Int,
										 val number: Int,
										 val centreId: Int,
										 val origSubCentreId: Int,
										 val masterTableVersion: Int,
										 val localTableVersion: Int,
										 val referenceTime: Calendar,
										 val dataProdStatus: Int,
										 val dataType: Int) {
	companion object {
		fun readFromStream(gribInputStream: GribInputStream): Grib2RecordIDS {
			/* [1-4] Section Length */
			val length = gribInputStream.readUINT(4)

			/* [5] Section Number */
			val number = gribInputStream.readUINT(1)

			/* [6-7] Originating Centre ID */
			val centreId = gribInputStream.readUINT(2)

			/* [8-9] Originating Sub-centre ID */
			val origSubCentreId = gribInputStream.readUINT(2)

			/* [10] Master Tables Version Number */
			val masterTableVersion = gribInputStream.readUINT(1)

			/* [11] Local Tables Version Number */
			val localTableVersion = gribInputStream.readUINT(1)

			/* [12] Reference Time Significance */
			/*val refTimeSig = */gribInputStream.readUINT(1)

			/* [13-14] Reference Year */
			val year = gribInputStream.readUINT(2)

			/* [15] Reference Month */
			val month = gribInputStream.readUINT(1)

			/* [16] Reference Day */
			val day = gribInputStream.readUINT(1)

			/* [17] Hour */
			val hour = gribInputStream.readUINT(1)

			/* [18] Minute */
			val minute = gribInputStream.readUINT(1)

			/* [19] Second */
			val second = gribInputStream.readUINT(1)
			TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
			val referenceTime = GregorianCalendar(year, month - 1, day, hour, minute, second)

			/* Data Production Status */
			val dataProdStatus = gribInputStream.readUINT(1)

			/* Data Type */
			val dataType = gribInputStream.readUINT(1)

			/* Additional Data */
			if (length > 21) Logger.error("Length is greater than 21. Please review code.")
			return Grib2RecordIDS(length, number, centreId, origSubCentreId, masterTableVersion, localTableVersion,
					referenceTime, dataProdStatus, dataType)
		}
	}

	//private val year = 0

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