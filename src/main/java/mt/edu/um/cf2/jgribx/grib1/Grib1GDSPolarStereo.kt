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

import mt.edu.um.cf2.jgribx.GribInputStream
import mt.edu.um.cf2.jgribx.GribOutputStream
import mt.edu.um.cf2.jgribx.Logger
import mt.edu.um.cf2.jgribx.NotSupportedException
import kotlin.math.*

/**
 * ### [Grid 5: Polar stereographic](https://apps.ecmwf.int/codes/grib/format/grib1/grids/5/)
 *
 *     | Octets | # | Key                         | Type     | Content                                               |
 *     |--------|---|-----------------------------|----------|-------------------------------------------------------|
 *     | 7-8    | 2 | Nx                          | unsigned | Nx number of points along x-axis                      |
 *     | 9-10   | 2 | Ny                          | unsigned | Ny number of points along y-axis                      |
 *     | 11-13  | 3 | latitudeOfFirstGridPoint    | signed   | La1 latitude of first grid point                      |
 *     | 14-16  | 3 | longitudeOfFirstGridPoint   | signed   | Lo1 longitude of first grid point                     |
 *     | 17     | 1 | resolutionAndComponentFlags | codeflag | Resolution and component flags (see Code table 7)     |
 *     | 18-20  | 3 | orientationOfTheGrid        | signed   | LoV orientation of the grid; i.e. the longitude value |
 *     |        |   |                             |          | of the meridian which is parallel to the y-axis (or   |
 *     |        |   |                             |          | columns of the grid) along which latitude increases   |
 *     |        |   |                             |          | as the Y-coordinate increases (the orientation        |
 *     |        |   |                             |          | longitude may or may not appear on a particular grid) |
 *     | 21-23  | 3 | DxInMetres                  | unsigned | Dx X-direction grid length (see Note (2))             |
 *     | 24-26  | 3 | DyInMetres                  | unsigned | Dy Y-direction grid length (see Note (2))             |
 *     | 27     | 1 | projectionCenterFlag        | unsigned | Projection centre flag (see Note (5))                 |
 *     | 28     | 1 | scanningMode                | codeflag | Scanning mode (flags see Flag/Code table 8)           |
 *     | 29-32  | 4 |                             |          | Set to zero (reserved)                                |
 *
 * A class that represents the grid definition section (GDS) of a GRIB record.
 *
 * Modified 4 Sep 02 to be constructed by GribGDSFactory - Richard D. Gonzalez
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
 * @param orientationOfTheGrid        (`18-20`) LoV orientation of the grid; i.e. the longitude value of the meridian which is parallel to the y-axis (or columns of the grid) along which latitude increases as the Y-coordinate increases (the orientation longitude may or may not appear on a particular grid)
 * @param dxInMetres                  (`21-23`) Dx X-direction grid length (see Note (2))
 * @param dyInMetres                  (`24-26`) Dy Y-direction grid length (see Note (2))
 * @param projectionCenterFlag        (`27`)    Projection centre flag (see Note (5))
 * @param scanningMode                (`28`)    Scanning mode (flags see Flag/Code table 8)
 *
 * @param gridStartX Starting x value using this projection. This is not a Longitude, but an x value based on the projection
 * @param gridStartY Starting y value using this projection. This is not a Latitude, but a y value based on the projection
 *
 * @throws java.io.IOException           if stream can not be opened etc.
 * @throws mt.edu.um.cf2.jgribx.NoValidGribException  if stream contains no valid GRIB file
 * @throws mt.edu.um.cf2.jgribx.NotSupportedException
 * @author Benjamin Stark
 * @author Capt Richard D. Gonzalez
 * @author Jan Kubovy [jan@kubovy.eu]
 */
class Grib1GDSPolarStereo(numberOfVerticalCoordinateValues: Int,
						  pvlLocation: Int,

						  val gridNx: Int,
						  val gridNy: Int,
						  override val latitudeOfFirstGridPoint: Double,
						  override val longitudeOfFirstGridPoint: Double,
						  val resolutionAndComponentFlags: Int,
						  val orientationOfTheGrid: Double,
						  val dxInMetres: Double,
						  val dyInMetres: Double,
						  val projectionCenterFlag: Int,
						  override val scanningMode: Int,

						  val gridStartX: Double,
						  val gridStartY: Double) :
		Grib1RecordGDS(numberOfVerticalCoordinateValues, pvlLocation) {
	companion object {
		/** Central Scale Factor.  Assumed 1.0 */
		private const val SCALE_FACTOR = 1.0

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
		private fun prepProjection(latitudeOfFirstGridPoint: Double,
								   longitudeOfFirstGridPoint: Double,
								   orientationOfTheGrid: Double,
								   latitudeTrueScale: Double): Pair<Double, Double> {
			val k: Double
			// peg - variables pi2 and pi4 never used
			//double pi2;
			//double pi4;
			val cosLat1 = cos(Math.toRadians(latitudeOfFirstGridPoint))
			val sinLat1 = sin(Math.toRadians(latitudeOfFirstGridPoint))
			val cos60 = cos(Math.toRadians(latitudeTrueScale))
			val sin60 = sin(Math.toRadians(latitudeTrueScale))
			val dLonRad = Math.toRadians(longitudeOfFirstGridPoint - orientationOfTheGrid) //lon2 is lov
			k = 2.0 * SCALE_FACTOR /
					(1 + sin60 * sinLat1 + cos60 * cosLat1 * cos(dLonRad))
			val gridStartX = EARTH_RADIUS * k * cosLat1 * sin(dLonRad)
			val gridStartY = EARTH_RADIUS * k *
					(cos60 * sinLat1 - sin60 * cosLat1 * cos(dLonRad))
			return gridStartX to gridStartY
		}

		internal fun readFromStream(gribInputStream: GribInputStream,
									numberOfVerticalCoordinateValues: Int,
									pvlLocation: Int): Grib1GDSPolarStereo {
			Logger.debug("Discovered GDS type: PolarStereo")

			// [7-8] (Nx - number of points along x-axis)
			val gridNx = gribInputStream.readUINT(2)

			// [9-10] (Ny - number of points along y-axis)
			val gridNy = gribInputStream.readUINT(2)

			// [11-13] (La1 - latitude of first grid point)
			val latitudeOfFirstGridPoint = gribInputStream.readUINT(3) / 1000.0

			// [14-16] (Lo1 - longitude of first grid point)
			val longitudeOfFirstGridPoint = gribInputStream.readUINT(3) / 1000.0

			// [17] (resolution and component flags).  See Table 7
			val resolutionAndComponentFlags = gribInputStream.readUINT(1)

			// [18-20] (Lov - Orientation of the grid - east lon parallel to y axis)
			val orientationOfTheGrid = gribInputStream.readUINT(3) / 1000.0

			// [21-23] (Dx - the X-direction grid length) See Note 2 of Table D
			var dxInMetres = gribInputStream.readUINT(3).toDouble()

			// [24-26] (Dy - the Y-direction grid length) See Note 2 of Table D
			var dyInMetres = gribInputStream.readUINT(3).toDouble()

			// [27] (Projection Center flag) See Note 5 of Table D
			val projectionCenterFlag = gribInputStream.readUINT(1)
			// if bit 1 set to 1, SP is on proj plane
			val latitudeTrueScale = if (projectionCenterFlag and 128 == 128) -60.0 else 60.0

			// [28] (Scanning mode)  See Table 8
			val scanningMode = gribInputStream.readUINT(1)
			if (scanningMode and 63 != 0) {
				throw NotSupportedException("GribRecordGDS: This scanning mode (" +
						scanningMode + ") is not supported.")
			}
			// rdg = table 8 shows -i if bit set
			if (scanningMode and 128 != 0) dxInMetres *= -1.0
			// rdg - changed to != 64 here because table 8 shows -j if bit NOT set
			if (scanningMode and 64 != 64) dyInMetres *= -1.0

			// [29-32] are reserved
			gribInputStream.skip(4)

			val (gridStartX, gridStartY) = prepProjection(latitudeOfFirstGridPoint, longitudeOfFirstGridPoint,
					orientationOfTheGrid, latitudeTrueScale)

			return Grib1GDSPolarStereo(numberOfVerticalCoordinateValues, pvlLocation, gridNx, gridNy,
					latitudeOfFirstGridPoint, longitudeOfFirstGridPoint, resolutionAndComponentFlags,
					orientationOfTheGrid, dxInMetres, dyInMetres, projectionCenterFlag, scanningMode,
					gridStartX, gridStartY)
		}
	}

	override val length: Int = 32

	override val dataRepresentationType: Int = 5

	override val gridCols: Int
		get() = gridNx

	override val gridRows: Int
		get() = gridNy

	override val gridDeltaX: Double
		get() = dxInMetres

	override val gridDeltaY: Double
		get() = dyInMetres

	/** Latitude of Center - assumed 60 N or 60 S based on note 2 of table D */
	private val latitudeTrueScale: Double //true scale
		get() = if (projectionCenterFlag and 128 == 128) -60.0 else 60.0

	override val isUVEastNorth: Boolean
		get() = resolutionAndComponentFlags and 0x08 == 0

	/** Latitude of the circle where grid lengths are defined */
	private val gridCenterLat: Double
		get() = if (latitudeTrueScale > 0) 90.0 else -90.0

	override val xCoords: DoubleArray
		get() {
			val xCoords = DoubleArray(gridNx)
			val startX = gridStartX / 1000.0
			val dx = dxInMetres / 1000.0
			for (i in 0 until gridNx) {
				val x = startX + i * dx
				xCoords[i] = x
			}
			return xCoords
		}

	override val yCoords: DoubleArray
		get() {
			val yCoords = DoubleArray(gridNy)
			val startY = gridStartY / 1000.0
			val dy = dyInMetres / 1000.0
			for (j in 0 until gridNy) {
				val y = startY + j * dy
				yCoords[j] = y
			}
			return yCoords
		}

	override val coords: Array<DoubleArray>
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
			val coords = Array(gridNx * gridNy) { DoubleArray(2) }
			val cos60 = cos(Math.toRadians(latitudeTrueScale))
			val sin60 = sin(Math.toRadians(latitudeTrueScale))
			for (j in 0 until gridNy) {
				y = gridStartY + dyInMetres * j
				for (i in 0 until gridNx) {
					x = gridStartX + dxInMetres * i
					rho = sqrt(x * x + y * y)
					c = 2.0 * atan(rho / (2.0 * EARTH_RADIUS * SCALE_FACTOR))
					cosC = cos(Math.toRadians(c))
					sinC = sin(Math.toRadians(c))
					lon = asin(cosC * sin60 + y * sinC * cos60 / rho)
					lat = orientationOfTheGrid + atan(x * sinC / (rho * cos60 * cosC - y * cos60 * sinC))

					// move x-coordinates to the range -180..180
					if (lon >= 180.0) lon -= 360.0
					if (lon < -180.0) lon += 360.0
					if (lat > 90.0 || lat < -90.0) {
						Logger.error("GribGDSPolarStereo: latitude out of range (-90 to 90).")
					}
					coords[count][0] = lon
					coords[count][1] = lat
					count++
				}
			}
			return coords
		}

	override val dataIndices: Sequence<Int>
		get() = TODO("Not yet implemented")

	override fun getDataIndex(sequence: Int): Int = TODO("Not yet implemented")

	override fun getDataIndex(latitude: Double, longitude: Double): Int = TODO("Not yet implemented")

	override fun writeTo(outputStream: GribOutputStream) = TODO()

	/** @see Grib1RecordGDS.compare */
	override fun compare(gds: Grib1RecordGDS): Int {
		if (this == gds) return 0
		if (gds !is Grib1GDSPolarStereo) return -1
		// not equal, so either less than or greater than.
		// check if gds is less, if not, then gds is greater
		if (dataRepresentationType > gds.dataRepresentationType) return -1
		if (resolutionAndComponentFlags > gds.resolutionAndComponentFlags) return -1
		if (scanningMode > gds.scanningMode) return -1
		if (gridNx > gds.gridNx) return -1
		if (gridNy > gds.gridNy) return -1
		if (dxInMetres > gds.dxInMetres) return -1
		if (dyInMetres > gds.dyInMetres) return -1
		if (latitudeOfFirstGridPoint > gds.latitudeOfFirstGridPoint) return -1
		//if (gridLat2 > gds.gridLat2) return -1
		//if (gridLatSP > gds.gridLatSP) return -1
		return if (longitudeOfFirstGridPoint > gds.longitudeOfFirstGridPoint) return -1 else 1
		//if (gridLng2 > gds.gridLng2) return -1
		//if (gridLngSP > gds.gridLngSP) return -1
		//return if (gridRotAngle > gds.gridRotAngle) -1 else 1
		// if here, then something must be greater than something else - doesn't matter what
	}

	override fun equals(other: Any?) = this === other
			|| other is Grib1GDSPolarStereo
			&& super.equals(other)
			&& gridNx == other.gridNx
			&& gridNy == other.gridNy
			&& latitudeOfFirstGridPoint == other.latitudeOfFirstGridPoint
			&& longitudeOfFirstGridPoint == other.longitudeOfFirstGridPoint
			&& resolutionAndComponentFlags == other.resolutionAndComponentFlags
			&& orientationOfTheGrid == other.orientationOfTheGrid
			&& dxInMetres == other.dxInMetres
			&& dyInMetres == other.dyInMetres
			&& projectionCenterFlag == other.projectionCenterFlag
			&& scanningMode == other.scanningMode

	override fun hashCode(): Int {
		var result = super.hashCode()
		result = 31 * result + gridNx
		result = 31 * result + gridNy
		result = 31 * result + latitudeOfFirstGridPoint.hashCode()
		result = 31 * result + longitudeOfFirstGridPoint.hashCode()
		result = 31 * result + resolutionAndComponentFlags
		result = 31 * result + orientationOfTheGrid.hashCode()
		result = 31 * result + dxInMetres.hashCode()
		result = 31 * result + dyInMetres.hashCode()
		result = 31 * result + projectionCenterFlag
		result = 31 * result + scanningMode
		return result
	}

	override fun toString(): String = listOfNotNull(
			"GRIB1 GDS section:",
			"\tPolar stereo grid (${gridNx}x${gridNy})",
			"\t1st point:  lat: ${latitudeOfFirstGridPoint} lng: ${longitudeOfFirstGridPoint}",
			"\tGrid start X: ${gridStartX}m; Y: ${gridStartY}m;",
			"\tGrid length: X-Direction ${dxInMetres}m;  Y-Direction: ${dyInMetres}m",
			"\tOrientation - East longitude parallel to y-axis: ${orientationOfTheGrid}",
			"\tResolution and Component Flags:",
			(if (resolutionAndComponentFlags and 128 == 128) "\t\tDirection increments given"
			else "\t\tDirection increments not given"),
			(if (resolutionAndComponentFlags and 64 == 64) "\t\tEarth assumed oblate spheroid 6378.16 km at equator,  6356.775 km at pole, f=1/297.0"
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
			"\tProjection center flag: ${projectionCenterFlag}",
			"\tLatitude true scale: ${latitudeTrueScale}",
			"\tCenter lat: ${gridCenterLat}",
			"\tCenter lng: ${orientationOfTheGrid}")
			.joinToString("\n`")
}
