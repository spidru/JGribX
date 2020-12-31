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

/**
 *
 * @author AVLAB-USER3
 */
class Grib2RecordBMS private constructor(private val length: Int, internal val indicator: Indicator) {
	internal enum class Indicator {
		BITMAP_SPECIFIED,
		BITMAP_PREDETERMINED,
		BITMAP_PREDEFINED,
		BITMAP_NONE
	}

	companion object {
		fun readFromStream(gribInputStream: GribInputStream): Grib2RecordBMS? {
			/* [1-4] Length of section in octets */
			val length = gribInputStream.readUINT(4)

			/* [5] Section number */
			val section = gribInputStream.readUINT(1)
			if (section != 6) {
				Logger.error("BMS contains invalid section number ${section}!")
				return null
			}

			/* [6] Bitmap indicator */
			val bmIndicator = gribInputStream.readUINT(1)
			val indicator = when (bmIndicator) {
				0 -> Indicator.BITMAP_SPECIFIED
				in 1..253 -> Indicator.BITMAP_PREDETERMINED
				254 -> Indicator.BITMAP_PREDEFINED
				else -> Indicator.BITMAP_NONE
			}
			val bms = Grib2RecordBMS(length, indicator)
			if (bms.indicator != Indicator.BITMAP_NONE) {
				throw NotSupportedException("BMS bitmap not yet supported")
				// in.skip(bms.length - 6);
			}
			return bms
		}
	}
}
