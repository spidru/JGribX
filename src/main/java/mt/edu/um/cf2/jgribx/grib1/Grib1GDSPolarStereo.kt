/**
 * GribGDSPolarStereo.java  1.0  01/01/2001
 *
 * based on GribRecordGDS (C) Benjamin Stark
 * Heavily modified by Capt Richard D. Gonzalez to conform to GribGDSFactory
 * implementation - 4 Sep 02
 * Implements GDS Table D for Polar Stereo grid
 *
 */
package mt.edu.um.cf2.jgribx.grib1

import mt.edu.um.cf2.jgribx.Bytes2Number
import mt.edu.um.cf2.jgribx.GribInputStream
import mt.edu.um.cf2.jgribx.Logger
import mt.edu.um.cf2.jgribx.NoValidGribException
import mt.edu.um.cf2.jgribx.NotSupportedException
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * A class that represents the grid definition section (GDS) of a GRIB record.
 *
 * Modified 4 Sep 02 to be constructed by GribGDSFactory - Richard D. Gonzalez
 *
 * Constructs a <tt>GribRecordGDS</tt> object from a bit input stream.
 *
 * @param gribInputStream bit input stream with GDS content
 * @param header - int array with first six octets of the GDS
 * @throws java.io.IOException           if stream can not be opened etc.
 * @throws mt.edu.um.cf2.jgribx.NoValidGribException  if stream contains no valid GRIB file
 * @throws mt.edu.um.cf2.jgribx.NotSupportedException
 * @author  Benjamin Stark
 * @author  Capt Richard D. Gonzalez
 * @version 2.0
 */
class Grib1GDSPolarStereo(gribInputStream: GribInputStream, header: ByteArray) : Grib1RecordGDS(header) {
	companion object {
		/** Central Scale Factor.  Assumed 1.0 */
		protected const val SCALE_FACTOR = 1.0
	}

	/* start of attributes unique to the Polar Stereo GDS */
	/** Projection Center Flag. */
	var projCenterFlag: Int
		protected set

	/** starting x value using this projection. This is not a Longitude, but an x value based on the projection */
	var gridStartx = 0.0
		protected set

	/** starting y value using this projection. This is not a Latitude, but a y value based on the projection */
	var gridStarty = 0.0
		protected set

	/** Latitude of Center - assumed 60 N or 60 S based on note 2 of table D */
	var latitudeTrueScale = 60.0 //true scale
		protected set

	override val isUVEastNorth: Boolean
		get() = gridMode and 0x08 == 0

	/** East longitude parallel to y-axis */
	val gridLov: Double
		get() = gridLng2

	//=LOV
	val gridCenterLng: Double
		get() = gridLng2

	/** Latitude of the circle where grid lengths are defined */
	val gridCenterLat: Double
		get() = if (latitudeTrueScale > 0) 90.0 else -90.0

	override val xCoords: DoubleArray
		get() {
			val xCoords = DoubleArray(gridNX)
			val startX = gridStartx / 1000.0
			val dx = gridDX / 1000.0
			for (i in 0 until gridNX) {
				val x = startX + i * dx
				xCoords[i] = x
			}
			return xCoords
		}

	override val yCoords: DoubleArray
		get() {
			val yCoords = DoubleArray(gridNY)
			val startY = gridStarty / 1000.0
			val dy = gridDY / 1000.0
			for (j in 0 until gridNY) {
				val y = startY + j * dy
				yCoords[j] = y
			}
			return yCoords
		}

	override val gridCoords: DoubleArray
		get() {
			var count = 0
			var rho: Double
			var c: Double
			var cosC: Double
			var sinC: Double
			var lon: Double
			var lat: Double
			var x: Double
			var y: Double
			val coords = DoubleArray(gridNX * gridNY * 2)
			val cos60 = cos(Math.toRadians(latitudeTrueScale))
			val sin60 = sin(Math.toRadians(latitudeTrueScale))
			for (j in 0 until gridNY) {
				y = gridStarty + gridDY * j
				for (i in 0 until gridNX) {
					x = gridStartx + gridDX * i
					rho = sqrt(x * x + y * y)
					c = 2.0 * atan(rho / (2.0 * EARTH_RADIUS * SCALE_FACTOR))
					cosC = cos(Math.toRadians(c))
					sinC = sin(Math.toRadians(c))
					lon = asin(cosC * sin60 + y * sinC * cos60 / rho)
					lat = gridLng2 + atan(x * sinC / (rho * cos60 * cosC - y * cos60 * sinC))

					// move x-coordinates to the range -180..180
					if (lon >= 180.0) lon -= 360.0
					if (lon < -180.0) lon += 360.0
					if (lat > 90.0 || lat < -90.0) {
						Logger.error("GribGDSPolarStereo: latitude out of range (-90 to 90).")
					}
					coords[count++] = lon
					coords[count++] = lat
				}
			}
			return coords
		}

	init {
		Logger.debug("Discovered GDS type: PolarStereo")
		if (gridType != 5) throw NoValidGribException("GribGDSPolarStereo: gridType is not Polar Stereo (read grid type ${gridType} needed 5)")

		//read in the Grid Description (see Table D) of the GDS
		val data = gribInputStream.readUI8(length - header.size)

		// octets 7-8 (Nx - number of points along x-axis)
		gridNX = Bytes2Number.uint2(data[0], data[1])

		// octets 9-10 (Ny - number of points along y-axis)
		gridNY = Bytes2Number.uint2(data[2], data[3])

		// octets 11-13 (La1 - latitude of first grid point)
		gridLat1 = Bytes2Number.int3(data[4], data[5], data[6]) / 1000.0

		// octets 14-16 (Lo1 - longitude of first grid point)
		gridLng1 = Bytes2Number.int3(data[7], data[8], data[9]) / 1000.0

		// octet 17 (resolution and component flags).  See Table 7
		gridMode = data[10]

		// octets 18-20 (Lov - Orientation of the grid - east lon parallel to y axis)
		gridLng2 = Bytes2Number.int3(data[11], data[12], data[13]) / 1000.0

		// octets 21-23 (Dx - the X-direction grid length) See Note 2 of Table D
		gridDX = Bytes2Number.int3(data[14], data[15], data[16]).toDouble()

		// octets 24-26 (Dy - the Y-direction grid length) See Note 2 of Table D
		gridDY = Bytes2Number.uint3(data[17], data[18], data[19]).toDouble()

		// octets 27 (Projection Center flag) See Note 5 of Table D
		projCenterFlag = data[20]
		if (projCenterFlag and 128 == 128) { // if bit 1 set to 1, SP is on proj plane
			latitudeTrueScale = -60.0
		}

		// octet 28 (Scanning mode)  See Table 8
		gridScanmode = data[21]
		if (gridScanmode and 63 != 0) {
			throw NotSupportedException("GribRecordGDS: This scanning mode (" +
					gridScanmode + ") is not supported.")
		}
		// rdg = table 8 shows -i if bit set
		if (gridScanmode and 128 != 0) gridDX = -gridDX
		// rdg - changed to != 64 here because table 8 shows -j if bit NOT set
		if (gridScanmode and 64 != 64) gridDY = -gridDY
		//         if ((this.gridScanmode & 64) != 0) this.gridDY = -this.gridDY;

		// octets 29-32 are reserved
		prepProjection()
	}

	/** @see Grib1RecordGDS.compare */
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

	/**
	 * Prep the projection and determine the starting x and y values based on Lat1 and Lon1 relative to the origin for
	 * this grid.
	 *
	 * adapted from J.P. Snyder, Map Projections - A Working Manual, U.S. Geological Survey Professional Paper 1395,
	 * 1987 Maintained his symbols, so the code matches his work. Somewhat hard to follow, if interested, suggest
	 * looking up quick reference at http://mathworld.wolfram.com/LambertConformalConicProjection.html
	 *
	 * Origin is where Lov intersects 60 degrees (from note 2 of Table D) north or south (determined by bit 1 of the
	 * Projection Center Flag).
	 *
	 * This assumes a central scale factor of 1.
	 *
	 * @return latitide/longitude as doubles
	 */
	private fun prepProjection() {
		val k: Double
		// peg - variables pi2 and pi4 never used
		//double pi2;
		//double pi4; 
		val cosLat1 = cos(Math.toRadians(gridLat1))
		val sinLat1 = sin(Math.toRadians(gridLat1))
		val cos60 = cos(Math.toRadians(latitudeTrueScale))
		val sin60 = sin(Math.toRadians(latitudeTrueScale))
		val dLonRad = Math.toRadians(gridLng1 - gridLng2) //lon2 is lov
		k = 2.0 * SCALE_FACTOR /
				(1 + sin60 * sinLat1 + cos60 * cosLat1 * cos(dLonRad))
		gridStartx = EARTH_RADIUS * k * cosLat1 * sin(dLonRad)
		gridStarty = EARTH_RADIUS * k *
				(cos60 * sinLat1 - sin60 * cosLat1 * cos(dLonRad))
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
		if (other !is Grib1GDSPolarStereo) return false
		if (this === other) return true
		if (gridNX != other.gridNX) return false
		if (gridNY != other.gridNY) return false
		if (gridLat1 != other.gridLat1) return false
		if (gridLng1 != other.gridLng1) return false
		if (gridMode != other.gridMode) return false
		if (gridLat2 != other.gridLat2) return false
		if (gridDX != other.gridDX) return false
		if (gridDY != other.gridDY) return false
		if (gridType != other.gridType) return false
		if (projCenterFlag != other.projCenterFlag) return false
		return gridScanmode == other.gridScanmode
	}

	override fun toString(): String = listOfNotNull(
			"GRIB1 GDS section:",
			"\tPolar stereo grid (${gridNX}x${gridNY})",
			"\t1st point:  lat: ${gridLat1} lng: ${gridLng1}",
			"\tGrid start X: ${gridStartx}m; Y: ${gridStarty}m;",
			"\tGrid length: X-Direction ${gridDX}m;  Y-Direction: ${gridDY}m",
			"\tOrientation - East longitude parallel to y-axis: ${gridLov}",
			"\tResolution and Component Flags:",
			(if (gridMode and 128 == 128) "\t\tDirection increments given"
			else "\t\tDirection increments not given"),
			(if (gridMode and 64 == 64) "\t\tEarth assumed oblate spheroid 6378.16 km at equator,  6356.775 km at pole, f=1/297.0"
			else "\t\tEarth assumed spherical with radius = 6367.47 km"),
			(if (gridMode and 8 == 8) "\t\tu and v components are relative to the grid"
			else "\t\tu and v components are relative to easterly and northerly directions"),
			"\tScanning mode:",
			(if (gridScanmode and 128 == 128) "\t\tPoints scan in the -i direction"
			else "\t\tPoints scan in the +i direction"),
			(if (gridScanmode and 64 == 64) "\t\tPoints scan in the +j direction"
			else "\t\tPoints scan in the -j direction"),
			(if (gridScanmode and 32 == 32) "\t\tAdjacent points in j direction are consecutive"
			else "\t\tAdjacent points in i direction are consecutive"),
			"\tProjection center flag: ${projCenterFlag}",
			"\tLatitude true scale: ${latitudeTrueScale}",
			"\tCenter lat: ${gridCenterLat}",
			"\tCenter lng: ${gridCenterLng}")
			.joinToString("\n`")
}