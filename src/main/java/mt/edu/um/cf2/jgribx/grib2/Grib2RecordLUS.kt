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

/**
 * GRIB record Local Use Section (LUS).
 * @author spidru
 */
class Grib2RecordLUS(gribInputStream: GribInputStream) {
	internal var length: Int

	init {
		gribInputStream.mark(10)
		length = gribInputStream.readUINT(4)
		val section = gribInputStream.readUINT(1)

		if (section != 2) {
			Logger.error("LUS contains invalid section number ${section}!")
			gribInputStream.reset()
			length = 0
		}
	}
}
