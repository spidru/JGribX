/**
 * ===============================================================================
 * $Id: GribGDSLambert.java,v 1.3 2006/07/25 13:46:23 frv_peg Exp $
 * ===============================================================================
 * JGRIB library
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Authors:
 * See AUTHORS file
 * ===============================================================================
 */
/**
 * GribGDSLambert.java  1.0  10/01/2002
 *
 * based on GribRecordGDS (C) Benjamin Stark
 * Heavily modified by Richard D. Gonzalez to conform to GribGDSFactory
 * implementation - 4 Sep 02
 * Implements GDS Table D for Lambert grid
 *
 */
package mt.edu.um.cf2.jgribx.grib1

import mt.edu.um.cf2.jgribx.Bytes2Number
import mt.edu.um.cf2.jgribx.GribInputStream
import mt.edu.um.cf2.jgribx.Logger
import mt.edu.um.cf2.jgribx.NoValidGribException
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

/**
 * A class that represents the Grid Definition Section (GDS) of a GRIB record
 * using the Lambert Conformal projection.
 *
 * Modified 4 Sep 02 to be constructed by GribGDSFactory
 *
 * Constructs a <tt>GribRecordGDS</tt> object from a bit input stream.
 *
 * @param gribInputStream bit input stream with GDS content
 * @param header - int array with first six octets of the GDS
 * @throws java.io.IOException           if stream can not be opened etc.
 * @throws NoValidGribException  if stream contains no valid GRIB file
 * @author  Capt Richard D. Gonzalez
 * @version 2.0
 */
class Grib1GDSLambert(gribInputStream: GribInputStream, header: ByteArray) : Grib1RecordGDS(header) {
	/* start of attributes unique to the Lambert GDS */
	/**
	 * Projection Center Flag.
	 */
	protected var proj_center: Int
	/**
	 * Get first latitude from the pole at which cone cuts spherical earth -
	 * see note 8 of Table D
	 *
	 * @return latitude of south pole
	 */
	/**
	 * Latin 1 - The first latitude from pole at which secant cone cuts the
	 * sperical earth.  See Note 8 of ON388.
	 */
	var gridLatin1: Double
		protected set
	/**
	 * Get second latitude from the pole at which cone cuts spherical earth -
	 * see note 8 of Table D
	 *
	 * @return latitude of south pole
	 */
	/**
	 * Latin 2 - The second latitude from pole at which secant cone cuts the
	 * sperical earth.  See Note 8 of ON388.
	 */
	var gridLatin2: Double
		protected set

	/**
	 * latitude of south pole.
	 */
	protected var gridLatsp: Double = 0.0

	/**
	 * longitude of south pole.
	 */
	protected var gridLonsp: Double = 0.0
	/**
	 * Get starting x value for this grid - THIS IS NOT A LONGITUDE, but an x value
	 * calculated for this specific projection, based on an origin of latin1, lov.
	 *
	 * @return x grid value of first point of this grid.
	 */
	/**
	 * starting x value using this projection.
	 * This is NOT a lat or lon, but a grid position in this projection
	 */
	var startX = 0.0
		protected set
	/**
	 * Get starting y value for this grid - THIS IS NOT A LATITUDE, but an y value
	 * calculated for this specific projection, based on an origin of latin1, lov.
	 *
	 * @return y grid value of first point of this grid.
	 */
	/**
	 * starting y value using this projection.
	 * This is NOT a lat or lon, but a grid position in this projection
	 */
	var startY = 0.0
		protected set

	/**
	 * Variable used in calculating the projection - see prepProjection
	 */
	private var f = 0.0

	/**
	 * Variable used in calculating the projection - see prepProjection
	 */
	private var rhoRef = 0.0

	/**
	 * Variable used in calculating the projection - see prepProjection
	 */
	private var n = 0.0

	/** Orientation of the grid */
	val gridLov: Double
		get() = gridLng2

	/**
	 * Get all longitide coordinates
	 * @returns longtitude as double
	 */
	// doesn't work yet - need to create a projToLL method and convert each point
	//public double[] getXLons() {
	//	// alloc
	//	double[] coords = new double[gridNX];
	//	int k = 0;
	//	for (int x = 0; x < gridNX; x++) {
	//		double longi = gridLon1 +x * gridDX;
	//		// move x-coordinates to the range -180..180
	//		if (longi >= 180.0) longi = longi - 360.0;
	//		if (longi < -180.0) longi = longi + 360.0;
	//		coords[k++] = longi;
	//	}
	//	return coords;
	//}

	override val xCoords: DoubleArray
		get() {
			val xCoords = DoubleArray(gridNX)
			val startx = startX / 1000.0
			val dx = gridDX / 1000.0
			for (i in 0 until gridNX) {
				val x = startx + i * dx
				xCoords[i] = x
			}
			return xCoords
		}

	/**
	 * Get y Axis grid coordinates
	 * @returns latitude as double
	 */
	// doesn't work yet - need to create a projToLL method and convert each point
	//public double[] getYLats() {
	//	// alloc
	//	double[] coords = new double[gridNY];
	//	int k = 0;
	//	for (int y = 0; y < gridNY; y++) {
	//		double lati = gridLat1 + y * gridDY;
	//		// if (lati > 90.0 || lati < -90.0)
	//		// 	System.err.println("GribGDSLambert: latitude out of range (-90 to 90).");
	//		// coords[k++] = lati;
	//	}
	//	return coords;
	//}

	override val yCoords: DoubleArray
		get() {
			val yCoords = DoubleArray(gridNY)
			val starty = startY / 1000.0
			val dy = gridDY / 1000.0
			for (j in 0 until gridNY) {
				val y = starty + j * dy
				yCoords[j] = y
			}
			return yCoords
		}

	/**
	 * Prep the projection and determine the starting x and y values based on the
	 * Lat1 and Lon1 for this grid
	 *
	 * adapted from J.P. Snyder, Map Projections - A Working Manual,
	 * U.S. Geological Survey Professional Paper 1395, 1987
	 * Maintained his symbols, so the code matches his work.
	 * Somewhat hard to follow, if interested, suggest looking up quick reference
	 * at http://mathworld.wolfram.com/LambertConformalConicProjection.html
	 *
	 * Origin is where Lov intersects Latin1.
	 *
	 * Note:  In GRIB table D, the first standard parallel (Latin1) is the one
	 * closest to the pole.  In the mathematical formulas, the first
	 * standard parallel is the one closest to the equator.  Therefore,
	 * the math looks backwards here, but it isn't.
	 *
	 * @returns latitide/longitude as doubles
	 */
	private fun prepProjection() {
		//double pi2; - peg - never used
		//pi2=Math.PI/2; - peg - never used
		val pi4 = Math.PI / 4
		val latin1r = Math.toRadians(gridLatin1)
		val latin2r = Math.toRadians(gridLatin2)

		// compute the common parameters
		n = ln(cos(latin1r) / cos(latin2r)) / ln(tan(pi4 + latin2r / 2) / tan(pi4 + latin1r / 2))
		f = cos(latin1r) * tan(pi4 + latin1r / 2).pow(n) / n
		val rho = EARTH_RADIUS * f * tan(pi4 + Math.toRadians(gridLat1) / 2).pow(-n)
		rhoRef = EARTH_RADIUS * f * tan(pi4 + Math.toRadians(gridLatin1) / 2).pow(-n)

		// compute the starting x and starting y coordinates for this projection
		// the grid_lon2 here is the lov - the reference longitude
		val theta: Double = n * Math.toRadians(gridLng1 - gridLng2)
		startX = rho * sin(theta)
		startY = rhoRef - rho * cos(theta)
	}

	override val gridCoords: DoubleArray
		get() {
			var rho: Double
			var theta: Double
			// peg - variables: pi4, latin1r and latin2r never used
			//double pi4;
			//double latin1r;
			//double latin2r;
			var lat: Double
			var lon: Double
			var x: Double
			var y: Double
			val pi2: Double = Math.PI / 2
			//pi4=Math.PI/4;
			//latin1r = Math.toRadians(grid_latin1);
			//latin2r = Math.toRadians(grid_latin2);

			// need space for a lat and lon for each grid point
			val coords = DoubleArray(gridNY * gridNX * 2)

			// compute the lat and lon for each grid point
			// note - grid points are NOT the indices of the arrays, they are computed
			//        from the projection
			var k = 0
			for (j in 0 until gridNY) {
				y = startY + gridDY * j
				for (i in 0 until gridNX) {
					x = startX + gridDX * i
					theta = atan(x / (rhoRef - y))
					rho = sqrt(x.pow(2.0) + (rhoRef - y).pow(2.0))
					if (n < 0) {
						rho = -rho
					}
					lon = gridLng2 + Math.toDegrees(theta / n)
					lat = Math.toDegrees(2.0 *
							atan((EARTH_RADIUS * f / rho).pow(1 / n)) - pi2)

					// move x-coordinates to the range -180..180
					if (lon >= 180.0) lon -= 360.0
					if (lon < -180.0) lon += 360.0
					if (lat > 90.0 || lat < -90.0) Logger.error("GribGDSLambert: latitude out of range (-90 to 90).")
					//coords[gridNX * y + x] =
					coords[k++] = lon
					coords[k++] = lat
				}
			}
			return coords
		}

	init {
		if (gridType != 3) {
			throw NoValidGribException("GribGDSLambert: gridType is not " +
					"Lambert Conformal (read grid type " + gridType + " needed 3)")
		}

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
		proj_center = data[20]

		// octet 28 (Scanning mode)  See Table 8
		gridScanmode = data[21]
		if (gridScanmode and 63 != 0) throw NoValidGribException("GribRecordGDS: This scanning mode (" + gridScanmode +
				") is not supported.")
		if (gridScanmode and 128 != 0) gridDX = -gridDX
		// rdg - changed to != 64 here because table 8 shows -j if bit NOT set
		if (gridScanmode and 64 != 64) gridDY = -gridDY
		//         if ((this.gridScanmode & 64) != 0) this.gridDY = -this.gridDY;

		// octets 29-31 (Latin1 - first lat where secant cone cuts spherical earth)
		gridLatin1 = Bytes2Number.int3(data[22], data[23], data[24]) / 1000.0

		// octets 32-34 (Latin2 - second lat where secant cone cuts spherical earth)
		gridLatin2 = Bytes2Number.int3(data[25], data[26], data[27]) / 1000.0

		// octets 35-37 (lat of southern pole)
		this.gridLatSP = Bytes2Number.int3(data[28], data[29], data[30]) / 1000.0

		// octets 36-38 (lon of southern pole)
		this.gridLngSP = Bytes2Number.int3(data[31], data[32], data[33]) / 1000.0

		// calculate what you can about the projection from what we have
		prepProjection()
	}

	/** Projection Center flag - see note 5 of Table D. */
	val projCenter: Double
		get() = proj_center.toDouble()

	override val isUVEastNorth: Boolean
		get() = gridMode and 0x08 == 0

	override fun compare(gds: Grib1RecordGDS): Int {
		if (this == gds) {
			return 0
		}

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

	override fun equals(other: Any?): Boolean {
		if (other !is Grib1GDSLambert) return false
		if (this === other) return true
		if (gridType != other.gridType) return false
		if (gridNX != other.gridNX) return false
		if (gridNY != other.gridNY) return false
		if (gridLat1 != other.gridLat1) return false
		if (gridLng1 != other.gridLng1) return false
		if (gridMode != other.gridMode) return false
		if (gridLat2 != other.gridLat2) return false
		if (gridDX != other.gridDX) return false
		if (gridDY != other.gridDY) return false
		if (proj_center != other.proj_center) if (gridScanmode != other.gridScanmode) return false
		if (gridLatin1 != other.gridLatin1) return false
		if (gridLatin2 != other.gridLatin2) return false
		if (gridLatSP != other.gridLatSP) return false
		return gridLngSP == other.gridLngSP
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

	override fun toString(): String = listOf(
			"GRIB1 GDS section:",
			"\tLambert conformal grid (${gridNX}x${gridNY})",
			"\t1st point:  Lat: ${gridLat1} Lng: ${gridLng1}",
			"\tGrid length: X-Direction ${gridDX}m; Y-Direction: ${gridDY}m",
			"\tOrientation - East longitude parallel to y-axis: ${gridLat2}",
			"\tResolution and Component Flags:",
			(if (gridMode and 128 == 128) "\t\tDirection increments given"
			else "\t\tDirection increments not given"),
			(if (gridMode and 64 == 64) "\t\tEarth assumed oblate spheroid 6378.16 km at equator, 6356.775 km at pole, f=1/297.0"
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
			"\tThe first latitude from pole at which the secant cone cuts the spherical earth: ${gridLatin1}",
			"\tThe second latitude from pole at which the secant cone cuts the spherical earth: ${gridLatin2}",
			"\tLatitude of the southern pole: ${gridLatSP}",
			"\tLongitude of the southern pole: ${gridLngSP}")
			.joinToString("\n")
}