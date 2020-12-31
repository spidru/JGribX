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
package mt.edu.um.cf2.jgribx.grib1

import mt.edu.um.cf2.jgribx.Bytes2Number
import mt.edu.um.cf2.jgribx.GribInputStream
import mt.edu.um.cf2.jgribx.Logger
import mt.edu.um.cf2.jgribx.NoValidGribException
import kotlin.math.abs

/**
 * A class that represents the grid definition section (GDS) of a GRIB record
 * with a Lat/Lon grid projection.
 *
 * Constructs a <tt>GribRecordGDS</tt> object from a bit input stream.
 *
 * See Table D of NCEP Office Note 388 for details
 *
 * @param gribInputStream bit input stream with GDS content
 *
 * @throws java.io.IOException           if stream can not be opened etc.
 * @throws NoValidGribException  if stream contains no valid GRIB file
 * @throws mt.edu.um.cf2.jgribx.NotSupportedException
 * @author  Richard Gonzalez
 * based heavily on the original GribRecordGDS
 * @version 1.0
 */
class Grib1GDSLatLon(gribInputStream: GribInputStream) : Grib1RecordGDS(gribInputStream.read(6)) {

	/** @see Grib1RecordGDS.isUVEastNorth */
	override val isUVEastNorth: Boolean
		get() = gridMode and 0x08 == 0

	val isRotatedGrid: Boolean
		get() = gridType == 10 // Implicit IF-THEN

	/** Longitide coordinates converted to the range +/- 180 */
	override val xCoords: DoubleArray
		get() = getXCoords(true)

	/** Longitide coordinates */
	private fun getXCoords(convertTo180: Boolean): DoubleArray {
		val coords = DoubleArray(gridNX)
		for (x in 0 until gridNX) {
			var longi: Double = gridLng1 + x * gridDX
			if (convertTo180) { // move x-coordinates to the range -180..180
				if (longi >= 180.0) longi -= 360.0
				if (longi < -180.0) longi += 360.0
			} else { // handle wrapping at 360
				if (longi >= 360.0) longi -= 360.0
			}
			coords[x] = longi
		}
		return coords
	}

	/** Get all latitude coordinates */
	override val yCoords: DoubleArray
		get() {
			val coords = DoubleArray(gridNY)
			for (y in 0 until gridNY) {
				val lati: Double = gridLat1 + y * gridDY
				if (lati > 90.0 || lati < -90.0) Logger.error("GribGDSLatLon.getYCoords: latitude out of range (-90 to 90).")
				coords[y] = lati
			}
			return coords
		}// move x-coordinates to the range -180..180

	/**
	 * Get grid coordinates in longitude/latitude pairs Longitude is returned in the range +/- 180 degrees
	 *
	 * @see Grib1RecordGDS.gridCoords
	 * @return longitide/latituide as doubles
	 */
	override val gridCoords: DoubleArray
		get() {
			val coords = DoubleArray(gridNY * gridNX * 2)
			var k = 0
			for (y in 0 until gridNY) {
				for (x in 0 until gridNX) {
					var longi: Double = gridLng1 + x * gridDX
					val lati: Double = gridLat1 + y * gridDY

					// move x-coordinates to the range -180..180
					if (longi >= 180.0) longi -= 360.0
					if (longi < -180.0) longi += 360.0
					if (lati > 90.0 || lati < -90.0) Logger.error(
							"GribGDSLatLon.getGridCoords: latitude out of range (-90 to 90).")
					coords[k++] = longi
					coords[k++] = lati
				}
			}
			return coords
		}

	// Attributes for Lat/Lon grid not included in GribRecordGDS
	// None!  The Lat/Lon grid is the most basic, and all attributes match
	//   the original GribRecordGDS

	init {
		if (this.gridType != 0 && this.gridType != 10) throw NoValidGribException(
				"GribGDSLatLon: gridType is not Latitude/Longitude (read grid type ${gridType}, needed 0 or 10)")

		// octets 7-8 (number of points along a parallel)
		gridNX = gribInputStream.readUINT(2)

		// octets 9-10 (number of points along a meridian)
		gridNY = gribInputStream.readUINT(2)

		// octets 11-13 (latitude of first grid point)
		gridLat1 = gribInputStream.readINT(3, Bytes2Number.INT_SM) / 1000.0

		// octets 14-16 (longitude of first grid point)
		gridLng1 = gribInputStream.readINT(3, Bytes2Number.INT_SM) / 1000.0

		// octet 17 (resolution and component flags -> 128 == 0x80 == increments given.)
		gridMode = gribInputStream.readUINT(1)

		// TABLE 7 - RESOLUTION AND COMPONENT FLAGS (GDS Octet 17)
		// Bit 		Value 		Meaning
		// 1	0	Direction increments not given
		//    1	Direction increments given
		// 2	0	Earth assumed spherical with radius = 6367.47 km
		//    1	Earth assumed oblate spheroid with size
		//      as determined by IAU in 1965:
		//      6378.160 km, 6356.775 km, f = 1/297.0
		// 3-4		reserved (set to 0)
		// 5	0	u- and v-components of vector quantities resolved relative to easterly and northerly directions
		//    1	u and v components of vector quantities resolved relative to the defined grid in the direction of increasing x and y (or i and j) coordinates respectively
		// 6-8		reserved (set to 0)
		// octets 18-20 (latitude of last grid point)
		gridLat2 = gribInputStream.readINT(3, Bytes2Number.INT_SM) / 1000.0

		// octets 21-23 (longitude of last grid point)
		gridLng2 = gribInputStream.readINT(3, Bytes2Number.INT_SM) / 1000.0
		val incrementsGiven = this.gridMode and 0x80 == 0x80
		val earthShapeSpheroid = this.gridMode and 0x40 == 0x40
		val uvResolvedToGrid = this.gridMode and 0x08 == 0x08
		if (incrementsGiven) {
			// MSB seems to indicate signedness, but this is taken care of by scanMode, so we use abs()
			gridDX = abs(gribInputStream.readINT(2, Bytes2Number.INT_SM) / 1000.0)
			gridDY = abs(gribInputStream.readINT(2, Bytes2Number.INT_SM) / 1000.0)
		} else {
			// calculate increments
			this.gridDX = (gridLng2 - this.gridLng1) / this.gridNX
			this.gridDY = (gridLat2 - this.gridLat1) / this.gridNY
		}
		if (earthShapeSpheroid) Logger.error(
				"GRIB record assumes Earth is an oblate spheroid. This is not supported yet.")
		if (uvResolvedToGrid) Logger.error(
				"GRIB record resolves u- and v-components of vector quantities are relative to the defined" +
						" grid. This is not supported yet.")

		/* [28] Scan Mode */gridScanmode = gribInputStream.readUINT(1)
		val iPositiveDirection = gridScanmode and 0x80 != 0x80
		val jPositiveDirection = gridScanmode and 0x40 == 0x40
		if (!iPositiveDirection) this.gridDX *= -1.0
		if (!jPositiveDirection) this.gridDY *= -1.0
		gribInputStream.read(4) // dummy read
		when (this.gridType) {
			0 -> {
				// Standard Lat/Lon grid, no rotation
				gridLatSP = -90.0
				gridLngSP = 0.0
				gridRotAngle = 0.0
			}
			10 -> {
				// Rotated Lat/Lon grid, Lat (octets 33-35), Lon (octets 36-38), rotang (octets 39-42)
				//NB offset = 7 (octet = array index + 7)
				Logger.warning("GRIB Record uses rotated LatLon grid. This is untested.")
				gridLatSP = gribInputStream.readINT(3, Bytes2Number.INT_SM) / 1000.0
				gridLngSP = gribInputStream.readINT(3, Bytes2Number.INT_SM) / 1000.0
				gridRotAngle = gribInputStream.readFloat(4, Bytes2Number.FLOAT_IBM).toDouble()
			}
			else -> {
				// No knowledge yet
				// NEED to fix this later, if supporting other grid types
				Logger.error("GRIB Record uses an unsupported type of LatLon grid.")
				gridLatSP = Double.NaN
				gridLngSP = Double.NaN
				gridRotAngle = Double.NaN
			}
		}
	}

	/** @see Grib1RecordGDS.compare     */
	override fun compare(gds: Grib1RecordGDS): Int {
		if (this == gds) return 0
		// not equal, so either less than or greater than.
		// check if gds is less, if not, then gds is greater
		if (gridType > gds.gridType) return -1
		if (gridMode > gds.gridMode) return -1
		if (gridScanmode > gds.gridScanmode) return -1
		if (gridNX > gds.gridNX) return -1
		if (gridNY > gds.gridNY) return -1
		if (gridDX > gds.gridDX) return -1
		if (gridDY > gds.gridDY) return -1
		if (gridLat1 > gds.gridLat1) return -1
		if (gridLat2 > gds.gridLat2) return -1
		if (gridLatSP > gds.gridLatSP) return -1
		if (gridLng1 > gds.gridLng1) return -1
		if (gridLng2 > gds.gridLng2) return -1
		if (gridLngSP > gds.gridLngSP) return -1
		return if (gridRotAngle > gds.gridRotAngle) -1 else 1

		// if here, then something must be greater than something else - doesn't matter what
	}

	override fun hashCode(): Int {
		var result = 17
		result = 37 * result + gridNX
		result = 37 * result + gridNY
		val intLat1 = java.lang.Float.floatToIntBits(gridLat1.toFloat())
		result = 37 * result + intLat1
		val intLon1 = java.lang.Float.floatToIntBits(gridLng1.toFloat())
		result = 37 * result + intLon1
		return result
	}

	override fun equals(other: Any?): Boolean {
		if (other !is Grib1RecordGDS) return false
		if (this === other) return true
		if (gridType != other.gridType) return false
		if (gridMode != other.gridMode) return false
		if (gridScanmode != other.gridScanmode) return false
		if (gridNX != other.gridNX) return false
		if (gridNY != other.gridNY) return false
		if (gridDX != other.gridDX) return false
		if (gridDY != other.gridDY) return false
		if (gridLat1 != other.gridLat1) return false
		if (gridLat2 != other.gridLat2) return false
		if (gridLatSP != other.gridLatSP) return false
		if (gridLng1 != other.gridLng1) return false
		if (gridLng2 != other.gridLng2) return false
		if (gridLngSP != other.gridLngSP) return false
		return gridRotAngle == other.gridRotAngle
	}

	// TODO include more information about this projection
	override fun toString(): String = listOfNotNull(
			"GRIB1 GDS section (${this.gridNX}x${this.gridNY}):",
			(if (this.gridType == 0) "\tLatLng Grid" else null),
			(if (this.gridType == 10) "\tRotated LatLng Grid" else null),
			"\t\tLat: ${gridLat1} to ${gridLat2} (dy ${gridDY}))",
			"\t\tLng: ${gridLng1} to ${gridLng2} (dx ${gridDX})",
			(if (this.gridType == 10) "\t\tSouth pole: lat=${gridLatSP} lng=${gridLngSP}" else null),
			(if (this.gridType == 10) "\t\tRotation angle: ${gridRotAngle}" else null))
			.joinToString("\n")
}