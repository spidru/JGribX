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
import mt.edu.um.cf2.jgribx.NoValidGribException

/**
 * ### [Section 2: Local use section](https://www.nco.ncep.noaa.gov/pmb/docs/grib2/grib2_doc/grib2_sect2.shtml)
 *
 *    | Octet | # | Value                               |
 *    |-------|---|-------------------------------------|
 *    | 1-4   | 4 | Length of the section in octets (N) |
 *    | 5     | 1 | Number of the section (2)           |
 *    | 6-N   |   | Local Use                           |
 *
 * @param data (`6-N`) Local Use
 *
 * @author spidru
 * @author Jan Kubovy [jan@kubovy.eu]
 */
class Grib2RecordLUS internal constructor(internal val data: ByteArray?) : Grib2Section {

	companion object {
		internal fun readFromStream(gribInputStream: GribInputStream, readEntire: Boolean = false): Grib2RecordLUS {
			/* [1-5] Length, section number */
			val length = Grib2Section.readFromStream(gribInputStream, 2)

			/* [6-N] Local Use */
			var data: ByteArray? = null
			if (readEntire) {
				data = ByteArray(length - 5)
				gribInputStream.read(data)
			} else {
				gribInputStream.skip(length - 5L)
			}

			return Grib2RecordLUS(data).takeIf { it.length == length || !readEntire }
					?: throw NoValidGribException("LUS length mismatch")
		}
	}

	override val length: Int
		get() = 5 + (data?.size ?: 0)

	override val number: Int = 2

	override fun writeTo(outputStream: GribOutputStream) {
		data?.also { data -> // Only write if data are present
			super.writeTo(outputStream) // [1-5] length, section number
			outputStream.write(data) // [6-N] Local Use
		}
	}

	override fun equals(other: Any?) = this === other
			|| other is Grib2RecordLUS
			&& length == other.length
			&& number == other.number
			&& data.contentEquals(other.data)

	override fun hashCode(): Int {
		var result = length
		result = 31 * result + number
		result = 31 * result + (data?.contentHashCode() ?: 0)
		return result
	}
}
