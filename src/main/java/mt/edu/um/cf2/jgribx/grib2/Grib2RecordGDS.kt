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
 * This constructor initialises a generic [Grib2RecordGDS] by only reading the header data from a given
 * [GribInputStream].
 *
 * @param gribInputStream
 * @throws java.io.IOException
 */
abstract class Grib2RecordGDS(gribInputStream: GribInputStream) {
	inner class ScanMode internal constructor(flags: Byte) {
		internal var iDirectionPositive: Boolean = flags.toInt() and 0x80 != 0x80
		internal var jDirectionPositive: Boolean = flags.toInt() and 0x40 == 0x40
		var iDirectionConsecutive: Boolean = flags.toInt() and 0x20 != 0x20
		var rowsZigzag: Boolean = flags.toInt() and 0x10 == 0x10
		var iDirectionEvenRowsOffset: Boolean = flags.toInt() and 0x08 == 0x08
		var iDirectionOddRowsOffset: Boolean = flags.toInt() and 0x04 == 0x04
		var jDirectionOffset: Boolean = flags.toInt() and 0x02 == 0x02
		var rowsNiNjPoints: Boolean = flags.toInt() and 0x01 != 0x01
	}

	companion object {
		fun readFromStream(gribInputStream: GribInputStream): Grib2RecordGDS {
			gribInputStream.mark(15)

			// [1-4] Length of section in octets
			gribInputStream.skip(4)

			// [5] Section number
			val section = gribInputStream.readUINT(1)
			if (section != 3) Logger.error("GDS contains invalid section number ${section}!")

			/* [6] Grid Definition Source */
			gribInputStream.skip(1)

			/* [7-10] Number of Data Points */
			gribInputStream.skip(4)

			/* [11] Number of Octets (for optional list of numbers defining number of points) */
			gribInputStream.skip(1)

			/* [12] Interpretation */
			gribInputStream.skip(1)

			/* [13-14] Grid Definition Template Number */
			val gridType = gribInputStream.readUINT(2)
			gribInputStream.reset() // required since constructors below will read the GDS from the beginning
			return when (gridType) {
				0 -> Grib2RecordGDSLatLon(gribInputStream) // Latitude/Longitude (also called Equidistant Cylindrical or Plate Caree)
				else -> throw NotSupportedException("Unsupported grid type: ${gridType}")
			}
		}
	}

	protected var lat1 = 0.0
	protected var lat2 = 0.0
	protected var lon1 = 0.0
	protected var lon2 = 0.0
	protected var length: Int

	/** Number of data points */
	var numberOfDataPoints: Int
		protected set

	protected var gridDi = 0.0
	protected var gridDj = 0.0
	var gridNi = 0
	var gridNj = 0
	private val gridType: Int
	protected var earthShape = 0
	var scanMode: ScanMode? = null

	abstract val gridCoords: Array<DoubleArray>
	protected abstract val gridXCoords: DoubleArray
	protected abstract val gridYCoords: DoubleArray
	abstract val gridDeltaX: Double
	abstract val gridDeltaY: Double
	abstract val gridLatStart: Double
	abstract val gridLonStart: Double
	protected abstract val gridSizeX: Int
	protected abstract val gridSizeY: Int

	init {
		// [1-4] Length of section in octets
		length = gribInputStream.readUINT(4)

		// [5] Section number
		gribInputStream.skip(1)

		/* [6] Grid Definition Source */
		val gridSource = gribInputStream.readUINT(1)
		if (gridSource != 0) {
			Logger.error("Unsupported grid definition source")
		}

		/* [7-10] Number of Data Points */numberOfDataPoints = gribInputStream.readUINT(4)

		/* [11] Number of Octets (for optional list of numbers defining number of points) */gribInputStream.skip(1)

		/* [12] Interpretation */gribInputStream.skip(1)

		/* [13-14] Grid Definition Template Number */gridType = gribInputStream.readUINT(2)

		/* [15-xx] Grid Definition Template */
		// This part will be processed by constructors of child classes
	}
}
