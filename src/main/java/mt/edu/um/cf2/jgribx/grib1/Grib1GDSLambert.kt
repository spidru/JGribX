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

import mt.edu.um.cf2.jgribx.GribInputStream
import mt.edu.um.cf2.jgribx.GribOutputStream
import mt.edu.um.cf2.jgribx.Logger
import mt.edu.um.cf2.jgribx.NoValidGribException
import java.util.*
import kotlin.math.*

/**
 * ### [Grid 3: Lambert conformal, secant or tangent, conic or bi-polar (normal or oblique)](https://apps.ecmwf.int/codes/grib/format/grib1/grids/3/)
 *
 *     | Octets | # | Key                         | Type     | Content                                               |
 *     |--------|---|-----------------------------|----------|-------------------------------------------------------|
 *     | 7-8    | 2 | Nx                          | unsigned | Nx number of points along x-axis                      |
 *     | 9-10   | 2 | Ny                          | unsigned | Ny number of points along y-axis                      |
 *     | 11-13  | 3 | latitudeOfFirstGridPoint    | signed   | La1 latitude of first grid point                      |
 *     | 14-16  | 3 | longitudeOfFirstGridPoint   | signed   | Lo1 longitude of first grid point                     |
 *     | 17     | 1 | resolutionAndComponentFlags | codeflag | Resolution and component flags (see Code table 7)     |
 *     | 18-20  | 3 | LoV                         | signed   | LoV orientation of the grid; i.e. the east longitude  |
 *     |        |   |                             |          | value of the meridian which is parallel to the y-axis |
 *     |        |   |                             |          | (or columns of the grid) along which latitude         |
 *     |        |   |                             |          | increases as the y-coordinate increases (the          |
 *     |        |   |                             |          | orientation longitude may or may not appear on a      |
 *     |        |   |                             |          | particular grid)                                      |
 *     | 21-23  | 3 | DxInMetres                  | unsigned | Dx x-direction grid length (see Note (2))             |
 *     | 24-26  | 3 | DyInMetres                  | unsigned | Dy y-direction grid length (see Note (2))             |
 *     | 27     | 1 | projectionCenterFlag        | unsigned | Projection centre flag (see Note (5))                 |
 *     | 28     | 1 | scanningMode                | codeflag | Scanning mode (flags see Flag/Code table 8)           |
 *     | 29-31  | 3 | Latin1                      | signed   | Latin 1 first latitude from the pole at which the     |
 *     |        |   |                             |          | secant cone cuts the sphere                           |
 *     | 32-34  | 3 | Latin2                      | signed   | Latin 2 second latitude from the pole at which the    |
 *     |        |   |                             |          | secant cone cuts the sphere                           |
 *     | 35-37  | 3 | latitudeOfSouthernPole      | signed   | Latitude of the southern pole in millidegrees         |
 *     |        |   |                             |          | (integer)                                             |
 *     | 38-40  | 3 | longitudeOfSouthernPole     | signed   | Longitude of the southern pole in millidegrees        |
 *     |        |   |                             |          | (integer)                                             |
 *     | 41-42  | 2 |                             |          | Set to zero (reserved)                                |
 *
 *
 * A class that represents the Grid Definition Section (GDS) of a GRIB record
 * using the Lambert Conformal projection.
 *
 * Modified 4 Sep 02 to be constructed by GribGDSFactory
 *
 * Constructs a <tt>GribRecordGDS</tt> object from a bit input stream.
 *
 * @param numberOfVerticalCoordinateValues (`4`) NV number of vertical coordinate parameters
 * @param pvlLocation                      (`5`) PV location (octet number) of the list of vertical coordinate
 *                                               parameters, if present; or PL location (octet number) of the list of
 *                                               numbers of points in each row (if no vertical coordinate parameters
 *                                               are present), if present; or 255 (all bits set to 1) if neither are
 *                                               present
 * @param gridNx                      (`7-8`)   Nx number of points along x-axis
 * @param gridNy                      (`9-10`)  Ny number of points along y-axis
 * @param latitudeOfFirstGridPoint    (`11-13`) La1 latitude of first grid point
 * @param longitudeOfFirstGridPoint   (`14-16`) Lo1 longitude of first grid point
 * @param resolutionAndComponentFlags (`17`)    Resolution and component flags (see Code table 7)
 * @param loV                         (`18-20`) LoV orientation of the grid; i.e. the longitude value of the meridian
 *                                              which is parallel to the y-axis (or columns of the grid) along which
 *                                              latitude increases as the Y-coordinate increases (the orientation
 *                                              longitude may or may not appear on a particular grid)
 * @param dxInMetres                  (`21-23`) Dx X-direction grid length (see Note (2))
 * @param dyInMetres                  (`24-26`) Dy Y-direction grid length (see Note (2))
 * @param projectionCenterFlag        (`27`)    Projection centre flag (see Note (5))
 * @param scanningMode                (`28`)    Scanning mode (flags see Flag/Code table 8)
 * @param latin1                      (`29-31`) Latin 1 first latitude from the pole at which the secant cone cuts the sphere
 * @param latin2                      (`32-34`) Latin 2 second latitude from the pole at which the secant cone cuts the sphere
 * @param latitudeOfSouthernPole      (`35-37`) Latitude of the southern pole in millidegrees (integer)
 * @param longitudeOfSouthernPole     (`38-40`) Longitude of the southern pole in millidegrees (integer)
 *
 * @throws java.io.IOException   if stream can not be opened etc.
 * @throws NoValidGribException  if stream contains no valid GRIB file
 * @author  Capt Richard D. Gonzalez
 * @version 2.0
 */
class Grib1GDSLambert(numberOfVerticalCoordinateValues: Int,
					  pvlLocation: Int,

					  val gridNx: Int,
					  val gridNy: Int,
					  override val latitudeOfFirstGridPoint: Double,
					  override val longitudeOfFirstGridPoint: Double,
					  val resolutionAndComponentFlags: Int,
					  val loV: Double,
					  val dxInMetres: Double,
					  val dyInMetres: Double,
					  val projectionCenterFlag: Int,
					  override val scanningMode: Int,
					  val latin1: Double,
					  val latin2: Double,
					  val latitudeOfSouthernPole: Double,
					  val longitudeOfSouthernPole: Double) :
		Grib1RecordGDS(numberOfVerticalCoordinateValues, pvlLocation) {

	companion object {
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
		private fun prepProjection(latitudeOfFirstGridPoint: Double,
								   longitudeOfFirstGridPoint: Double,
								   loV: Double,
								   latin1: Double,
								   latin2: Double): List<Double> {
			//double pi2; - peg - never used
			//pi2=Math.PI/2; - peg - never used
			val pi4 = Math.PI / 4
			val latin1r = Math.toRadians(latin1)
			val latin2r = Math.toRadians(latin2)

			// compute the common parameters
			val n = ln(cos(latin1r) / cos(latin2r)) / ln(tan(pi4 + latin2r / 2) / tan(pi4 + latin1r / 2))
			val f = cos(latin1r) * tan(pi4 + latin1r / 2).pow(n) / n
			val rho = EARTH_RADIUS * f * tan(pi4 + Math.toRadians(latitudeOfFirstGridPoint) / 2).pow(-n)
			val rhoRef = EARTH_RADIUS * f * tan(pi4 + Math.toRadians(latin1) / 2).pow(-n)

			// compute the starting x and starting y coordinates for this projection
			// the grid_lon2 here is the lov - the reference longitude
			val theta: Double = n * Math.toRadians(longitudeOfFirstGridPoint - loV)
			val startX = rho * sin(theta)
			val startY = rhoRef - rho * cos(theta)
			return listOf(startX, startY, n, f, rhoRef)
		}

		internal fun readFromStream(gribInputStream: GribInputStream,
									numberOfVerticalCoordinateValues: Int,
									pvlLocation: Int): Grib1GDSLambert {
			// [7-8] Nx number of points along x-axis
			val gridNx = gribInputStream.readUInt(2)

			// [9-10] Ny number of points along y-axis
			val gridNy = gribInputStream.readUInt(2)

			// [11-13] La1 latitude of first grid point
			val latitudeOfFirstGridPoint = gribInputStream.readUInt(3) / 1000.0

			// [14-16] Lo1 longitude of first grid point
			val longitudeOfFirstGridPoint = gribInputStream.readUInt(3) / 1000.0

			// [17] Resolution and component flags (see Code table 7)
			val resolutionAndComponentFlags = gribInputStream.readUInt(1)

			// [18-20] LoV orientation of the grid; i.e. the longitude value of the meridian which is parallel to the
			// y-axis (or columns of the grid) along which latitude increases as the Y-coordinate increases (the
			// orientation longitude may or may not appear on a particular grid)
			val loV = gribInputStream.readUInt(3) / 1000.0

			// [21-23] Dx X-direction grid length (see Note (2))
			var dxInMetres = gribInputStream.readUInt(3).toDouble()

			// [24-26] Dy Y-direction grid length (see Note (2))
			var dyInMetres = gribInputStream.readUInt(3).toDouble()

			// [27] Projection centre flag (see Note (5))
			val projectionCenterFlag = gribInputStream.readUInt(1)

			// [28] Scanning mode (flags see Flag/Code table 8)
			val scanningMode = gribInputStream.readUInt(1)
			if (scanningMode and 63 != 0) throw NoValidGribException("GribRecordGDS: This scanning mode" +
					" (${scanningMode}) is not supported.")
			if (scanningMode and 128 != 0) dxInMetres *= -1.0
			// rdg - changed to != 64 here because table 8 shows -j if bit NOT set
			if (scanningMode and 64 != 64) dyInMetres *= -1.0

			// [29-31] Latin 1 first latitude from the pole at which the secant cone cuts the sphere
			val latin1 = gribInputStream.readUInt(3) / 1000.0

			// [32-34] Latin 2 second latitude from the pole at which the secant cone cuts the sphere
			val latin2 = gribInputStream.readUInt(3) / 1000.0

			// [35-37] Latitude of the southern pole in millidegrees (integer)
			val latitudeOfSouthernPole = gribInputStream.readUInt(3) / 1000.0

			// [36-40] Longitude of the southern pole in millidegrees (integer)
			val longitudeOfSouthernPole = gribInputStream.readUInt(3) / 1000.0

			// [41-42]
			gribInputStream.skip(2)

			return Grib1GDSLambert(numberOfVerticalCoordinateValues, pvlLocation, gridNx, gridNy,
					latitudeOfFirstGridPoint, longitudeOfFirstGridPoint, resolutionAndComponentFlags, loV, dxInMetres,
					dyInMetres, projectionCenterFlag, scanningMode, latin1, latin2, latitudeOfSouthernPole,
					longitudeOfSouthernPole)
		}
	}

	override val length: Int = 42

	override val dataRepresentationType: Int = 3

	/**
	 * starting x value for this grid - THIS IS NOT A LONGITUDE, but an x value calculated for this specific
	 * projection, based on an origin of latin1, lov.
	 */
	private val startX: Double

	/**
	 * starting y value for this grid - THIS IS NOT A LONGITUDE, but an y value calculated for this specific
	 * projection, based on an origin of latin1, lov.
	 */
	private val startY: Double

	/** Variable used in calculating the projection - see prepProjection */
	private val f: Double

	/** Variable used in calculating the projection - see prepProjection */
	private val rhoRef: Double

	/** Variable used in calculating the projection - see prepProjection */
	private val n: Double

	override val gridCols: Int
		get() = gridNx

	override val gridRows: Int
		get() = gridNy

	override val gridDeltaX: Double
		get() = dxInMetres

	override val gridDeltaY: Double
		get() = dyInMetres

	/**
	 * Get all longitide coordinates
	 * @returns longtitude as double
	 */
	// doesn't work yet - need to create a projToLL method and convert each point
	//public double[] getXLons() {
	//	// alloc
	//	double[] coords = new double[gridNx];
	//	int k = 0;
	//	for (int x = 0; x < gridNx; x++) {
	//		double longi = gridLon1 +x * dxInMetres;
	//		// move x-coordinates to the range -180..180
	//		if (longi >= 180.0) longi = longi - 360.0;
	//		if (longi < -180.0) longi = longi + 360.0;
	//		coords[k++] = longi;
	//	}
	//	return coords;
	//}

	override val xCoords: DoubleArray
		get() {
			val xCoords = DoubleArray(gridNx)
			val startx = startX / 1000.0
			val dx = dxInMetres / 1000.0
			for (i in 0 until gridNx) {
				val x = startx + i * dx
				xCoords[i] = x
			}
			return xCoords
		}

	override val dataIndices: Sequence<Int>
		get() = TODO("Not yet implemented")

	override fun getDataIndex(sequence: Int): Int = TODO("Not yet implemented")

	override fun getDataIndex(latitude: Double, longitude: Double): Int = TODO("Not yet implemented")

	/**
	 * Get y Axis grid coordinates
	 * @returns latitude as double
	 */
	// doesn't work yet - need to create a projToLL method and convert each point
	//public double[] getYLats() {
	//	// alloc
	//	double[] coords = new double[gridNy];
	//	int k = 0;
	//	for (int y = 0; y < gridNy; y++) {
	//		double lati = latitudeOfFirstGridPoint + y * dyInMetres;
	//		// if (lati > 90.0 || lati < -90.0)
	//		// 	System.err.println("GribGDSLambert: latitude out of range (-90 to 90).");
	//		// coords[k++] = lati;
	//	}
	//	return coords;
	//}

	override val yCoords: DoubleArray
		get() {
			val yCoords = DoubleArray(gridNy)
			val starty = startY / 1000.0
			val dy = dyInMetres / 1000.0
			for (j in 0 until gridNy) {
				val y = starty + j * dy
				yCoords[j] = y
			}
			return yCoords
		}

	override val coords: Array<DoubleArray>
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
			val coords = Array(gridNy * gridNx) { DoubleArray(2) }

			// compute the lat and lon for each grid point
			// note - grid points are NOT the indices of the arrays, they are computed
			//        from the projection
			var k = 0
			for (j in 0 until gridNy) {
				y = startY + dyInMetres * j
				for (i in 0 until gridNx) {
					x = startX + dxInMetres * i
					theta = atan(x / (rhoRef - y))
					rho = sqrt(x.pow(2.0) + (rhoRef - y).pow(2.0))
					if (n < 0) {
						rho = -rho
					}
					lon = loV + Math.toDegrees(theta / n)
					lat = Math.toDegrees(2.0 *
							atan((EARTH_RADIUS * f / rho).pow(1 / n)) - pi2)

					// move x-coordinates to the range -180..180
					if (lon >= 180.0) lon -= 360.0
					if (lon < -180.0) lon += 360.0
					if (lat > 90.0 || lat < -90.0) Logger.error("GribGDSLambert: latitude out of range (-90 to 90).")
					//coords[gridNx * y + x] =
					coords[k][0] = lon
					coords[k][1] = lat
					k++
				}
			}
			return coords
		}

	override val isUVEastNorth: Boolean
		get() = resolutionAndComponentFlags and 0x08 == 0

	init {
		// calculate what you can about the projection from what we have
		val (startX, startY, n, f, rhoRef) = prepProjection(latitudeOfFirstGridPoint, longitudeOfFirstGridPoint, loV,
				latin1, latin2)
		this.startX = startX
		this.startY = startY
		this.n = n
		this.f = f
		this.rhoRef = rhoRef
	}

	override fun writeTo(outputStream: GribOutputStream) = TODO()

	/** @see Grib1RecordGDS.compareTo */
	override fun compareTo(other: Grib1RecordGDS): Int = if (other is Grib1GDSLambert) Comparator
			.comparingInt<Grib1GDSLambert>(Grib1RecordGDS::dataRepresentationType)
			.thenComparingInt(Grib1GDSLambert::resolutionAndComponentFlags)
			.thenComparingInt(Grib1GDSLambert::scanningMode)
			.thenComparingInt(Grib1GDSLambert::gridNx)
			.thenComparingInt(Grib1GDSLambert::gridNy)
			.thenComparingDouble(Grib1GDSLambert::dxInMetres)
			.thenComparingDouble(Grib1GDSLambert::dyInMetres)
			.thenComparingDouble(Grib1GDSLambert::latitudeOfFirstGridPoint)
			.thenComparingDouble(Grib1GDSLambert::latitudeOfSouthernPole)
			.thenComparingDouble(Grib1GDSLambert::longitudeOfFirstGridPoint)
			.thenComparingDouble(Grib1GDSLambert::loV)
			.thenComparingDouble(Grib1GDSLambert::longitudeOfSouthernPole)
			.compare(this, other)
	else super.compareTo(other)

	override fun equals(other: Any?) = this === other
			|| other is Grib1GDSLambert
			&& super.equals(other)
			&& gridNx == other.gridNx
			&& gridNy == other.gridNy
			&& latitudeOfFirstGridPoint == other.latitudeOfFirstGridPoint
			&& longitudeOfFirstGridPoint == other.longitudeOfFirstGridPoint
			&& resolutionAndComponentFlags == other.resolutionAndComponentFlags
			&& loV == other.loV
			&& dxInMetres == other.dxInMetres
			&& dyInMetres == other.dyInMetres
			&& projectionCenterFlag == other.projectionCenterFlag
			&& scanningMode == other.scanningMode
			&& latin1 == other.latin1
			&& latin2 == other.latin2
			&& latitudeOfSouthernPole == other.latitudeOfSouthernPole
			&& longitudeOfSouthernPole == other.longitudeOfSouthernPole

	override fun hashCode(): Int {
		var result = super.hashCode()
		result = 31 * result + gridNx
		result = 31 * result + gridNy
		result = 31 * result + latitudeOfFirstGridPoint.hashCode()
		result = 31 * result + longitudeOfFirstGridPoint.hashCode()
		result = 31 * result + resolutionAndComponentFlags
		result = 31 * result + loV.hashCode()
		result = 31 * result + dxInMetres.hashCode()
		result = 31 * result + dyInMetres.hashCode()
		result = 31 * result + projectionCenterFlag
		result = 31 * result + scanningMode
		result = 31 * result + latin1.hashCode()
		result = 31 * result + latin2.hashCode()
		result = 31 * result + latitudeOfSouthernPole.hashCode()
		result = 31 * result + longitudeOfSouthernPole.hashCode()
		return result
	}

	override fun toString(): String = listOf(
			"GRIB1 GDS section:",
			"\tLambert conformal grid (${gridNx}x${gridNy})",
			"\t1st point:  Lat: ${latitudeOfFirstGridPoint} Lng: ${longitudeOfFirstGridPoint}",
			"\tGrid length: X-Direction ${dxInMetres}m; Y-Direction: ${dyInMetres}m",
			//"\tOrientation - East longitude parallel to y-axis: ${gridLat2}",
			"\tResolution and Component Flags:",
			(if (resolutionAndComponentFlags and 128 == 128) "\t\tDirection increments given"
			else "\t\tDirection increments not given"),
			(if (resolutionAndComponentFlags and 64 == 64) "\t\tEarth assumed oblate spheroid 6378.16 km at equator, 6356.775 km at pole, f=1/297.0"
			else "\t\tEarth assumed spherical with radius = 6367.47 km"),
			(if (resolutionAndComponentFlags and 8 == 8) "\t\tu and v components are relative to the grid"
			else "\t\tu and v components are relative to easterly and northerly directions"),
			"\tScanning mode:",
			(if (scanningMode and 128 == 128) "\t\tPoints scan in the -i direction"
			else "\t\tPoints scan in the +i direction"),
			(if (scanningMode and 64 == 64) "\t\tPoints scan in the +j direction"
			else "\t\tPoints scan in the -j direction"),
			(if (scanningMode and 32 == 32) "\t\tAdjacent points in j direction are consecutive"
			else "\t\tAdjacent points in i direction are consecutive"),
			"\tThe first latitude from pole at which the secant cone cuts the spherical earth: ${latin1}",
			"\tThe second latitude from pole at which the secant cone cuts the spherical earth: ${latin2}",
			"\tLatitude of the southern pole: ${latitudeOfSouthernPole}",
			"\tLongitude of the southern pole: ${longitudeOfSouthernPole}")
			.joinToString("\n")
}
