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

import mt.edu.um.cf2.jgribx.Bytes2Number
import mt.edu.um.cf2.jgribx.GribInputStream
import mt.edu.um.cf2.jgribx.Logger
import mt.edu.um.cf2.jgribx.NotSupportedException

/**
 *
 * @author spidru
 */
class Grib2RecordGDSLatLon(gribInputStream: GribInputStream) : Grib2RecordGDS(gribInputStream) {
	override val gridCoords: Array<DoubleArray>
		get() {
			val coords = Array(gridNi * gridNj) { DoubleArray(2) }
			var k = 0
			for (j in 0 until gridNj) {
				for (i in 0 until gridNi) {
					var lon = lon1 + i * gridDi
					val lat = lat1 + j * gridDj

					// move x-coordinates to the range -180..180
					if (lon >= 180.0) lon -= 360.0
					if (lon < -180.0) lon += 360.0
					if (lat > 90.0 || lat < -90.0) {
						Logger.error("GribGDSLatLon.getGridCoords: latitude out of range (-90 to 90).")
					}
					coords[k][0] = lon
					coords[k][1] = lat
					k++
				}
			}
			return coords
		}

	override val gridXCoords: DoubleArray
		get() {
			val coords = DoubleArray(gridNi)
			val convertTo180 = true
			for (x in 0 until gridNi) {
				var lon = lon1 + x * gridDi
				if (convertTo180) {
					// move x-coordinates to the range -180..180
					if (lon >= 180.0) lon -= 360.0
					if (lon < -180.0) lon += 360.0
				} else { // handle wrapping at 360
					if (lon >= 360.0) lon -= 360.0
				}
				coords[x] = lon
			}
			return coords
		}

	override val gridYCoords: DoubleArray
		get() {
			val coords = DoubleArray(gridNj)
			for (y in 0 until gridNj) {
				val lat = lat1 + y * gridDj
				if (lat > 90.0 || lat < -90.0) System.err.println("GribGDSLatLon.getYCoords: latitude out of range (-90 to 90).")
				coords[y] = lat
			}
			return coords
		}

	override val gridDeltaX: Double
		get() = gridDi

	override val gridDeltaY: Double
		get() = gridDj

	override val gridLatStart: Double
		get() = lat1

	override val gridLonStart: Double
		get() = lon1

	override val gridSizeX: Int
		get() = gridNi

	override val gridSizeY: Int
		get() = gridNj

	init {
		earthShape = gribInputStream.readUINT(1)
		/*val radiusScaleFactor = */gribInputStream.readUINT(1)
		/*val radiusScaledValue = */gribInputStream.readUINT(4)
		/*val majorScaleFactor = */gribInputStream.readUINT(1)
		/*val majorScaledValue = */gribInputStream.readUINT(4)
		/*val minorScaleFactor = */gribInputStream.readUINT(1)
		/*val minorScaledValue = */gribInputStream.readUINT(4)
		gridNi = gribInputStream.readUINT(4)
		gridNj = gribInputStream.readUINT(4)
		val basicAngle = gribInputStream.readUINT(4)
		/*val basicAngleSubdiv = */gribInputStream.readUINT(4)
		if (basicAngle == 0) {
			lat1 = gribInputStream.readINT(4, Bytes2Number.INT_SM) / 1.0e6
			lon1 = gribInputStream.readINT(4, Bytes2Number.INT_SM) / 1.0e6
		}
		val flags = gribInputStream.readUINT(1)
		val iDirectionIncrementsGiven = flags and 0x20 == 0x20
		val jDirectionIncrementsGiven = flags and 0x10 == 0x10
		if (basicAngle == 0) {
			lat2 = gribInputStream.readINT(4, Bytes2Number.INT_SM) / 1.0e6
			lon2 = gribInputStream.readINT(4, Bytes2Number.INT_SM) / 1.0e6
		}
		/* [64-67] i-Direction Increment Di */if (iDirectionIncrementsGiven) {
			if (basicAngle == 0) {
				gridDi = gribInputStream.readUINT(4) / 1.0e6
			}
		}
		/* [68-71] j-Direction Increment Dj */if (jDirectionIncrementsGiven) {
			if (basicAngle == 0) {
				gridDj = gribInputStream.readUINT(4) / 1.0e6
			}
		}
		/* [72] Scanning Mode */
		// int scanMode = in.readUINT(1);
		scanMode = ScanMode(gribInputStream.readUINT(1).toByte()).also { scanMode ->
			// boolean iPositiveDirection = (scanMode & 0x80) != 0x80;
			// boolean jPositiveDirection = (scanMode & 0x40) == 0x40;
			// iDirectionConsecutive = (scanMode & 0x20) != 0x20;
			// rowsZigzag = (scanMode & 0x10) == 0x10;
			if (!scanMode.iDirectionPositive) gridDi *= -1.0
			if (!scanMode.jDirectionPositive) gridDj *= -1.0
			if (scanMode.iDirectionEvenRowsOffset
					|| scanMode.iDirectionOddRowsOffset
					|| scanMode.jDirectionOffset
					|| !scanMode.rowsNiNjPoints
					|| scanMode.rowsZigzag) throw NotSupportedException("Unsupported scan mode found")
		}
	}
}