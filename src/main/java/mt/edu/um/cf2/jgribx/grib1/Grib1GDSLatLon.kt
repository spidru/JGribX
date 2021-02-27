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

import mt.edu.um.cf2.jgribx.GribInputStream
import mt.edu.um.cf2.jgribx.GribOutputStream
import mt.edu.um.cf2.jgribx.Logger
import mt.edu.um.cf2.jgribx.NoValidGribException
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * ### [Grid 0: Latitude/longitude grid or equidistant cylindrical, or Plate Carrée](https://apps.ecmwf.int/codes/grib/format/grib1/grids/0/)
 *
 *     | Octets | # | Key                         | Type     | Content                                           |
 *     |--------|---|-----------------------------|----------|---------------------------------------------------|
 *     | 7-8    | 2 | Ni                          | unsigned | Ni number of points along a parallel              |
 *     | 9-10   | 2 | Nj                          | unsigned | Nj number of points along a meridian              |
 *     | 11-13  | 3 | latitudeOfFirstGridPoint    | signed   | La1 latitude of first grid point                  |
 *     | 14-16  | 3 | longitudeOfFirstGridPoint   | signed   | Lo1 longitude of first grid point                 |
 *     | 17     | 1 | resolutionAndComponentFlags | codeflag | Resolution and component flags (see Code table 7) |
 *     | 18-20  | 3 | latitudeOfLastGridPoint     | signed   | La2 latitude of last grid point                   |
 *     | 21-23  | 3 | longitudeOfLastGridPoint    | signed   | Lo2 longitude of last grid point                  |
 *     | 24-25  | 2 | iDirectionIncrement         | unsigned | Di i direction increment                          |
 *     | 26-27  | 2 | jDirectionIncrement         | unsigned | Dj j direction increment                          |
 *     | 28     | 1 | scanningMode                | codeflag | Scanning mode (flags see Flag/Code table 8)       |
 *     | 29-32  | 4 |                             |          | Set to zero (reserved)                            |
 *
 * A class that represents the grid definition section (GDS) of a GRIB record
 * with a Lat/Lon grid projection.
 *
 * Constructs a <tt>GribRecordGDS</tt> object from a bit input stream.
 *
 * See Table D of NCEP Office Note 388 for details
 *
 * @param numberOfVerticalCoordinateValues (`4`) NV number of vertical coordinate parameters
 * @param pvlLocation                      (`5`) PV location (octet number) of the list of vertical coordinate
 *                                               parameters, if present; or PL location (octet number) of the list of
 *                                               numbers of points in each row (if no vertical coordinate parameters
 *                                               are present), if present; or 255 (all bits set to 1) if neither are
 *                                               present
 * @param gridNi                           (`7-8`)   Ni number of points along a parallel
 * @param gridNj                           (`9-10`)  Nj number of points along a meridian
 * @param latitudeOfFirstGridPoint         (`11-13`) La1 latitude of first grid point
 * @param longitudeOfFirstGridPoint        (`14-16`) Lo1 longitude of first grid point
 * @param resolutionAndComponentFlags      (`17`)    Resolution and component flags (see Code table 7)
 * @param latitudeOfLastGridPoint          (`18-20`) La2 latitude of last grid point
 * @param longitudeOfLastGridPoint         (`21-23`) Lo2 longitude of last grid point
 * @param iDirectionIncrement              (`24-25`) Di i direction increment
 * @param jDirectionIncrement              (`26-27`) Dj j direction increment
 * @param scanningMode                     (`28`)    Scanning mode (flags see Flag/Code table 8)
 *
 * @throws java.io.IOException   if stream can not be opened etc.
 * @throws NoValidGribException  if stream contains no valid GRIB file
 * @author Richard Gonzalez
 * @author Jan Kubovy [jan@kubovy.eu]
 * based heavily on the original GribRecordGDS
 */
open class Grib1GDSLatLon(numberOfVerticalCoordinateValues: Int,
						  pvlLocation: Int,

						  val gridNi: Int, // Nx
						  val gridNj: Int, // Ny
						  override val latitudeOfFirstGridPoint: Double,
						  override val longitudeOfFirstGridPoint: Double,
						  val resolutionAndComponentFlags: Int,
						  val latitudeOfLastGridPoint: Double,
						  val longitudeOfLastGridPoint: Double,
						  val iDirectionIncrement: Double,
						  val jDirectionIncrement: Double,
						  override val scanningMode: Int) :
		Grib1RecordGDS(numberOfVerticalCoordinateValues, pvlLocation) {

	companion object {
		internal fun readFromStream(gribInputStream: GribInputStream,
									numberOfVerticalCoordinateValues: Int,
									pvlLocation: Int): Grib1GDSLatLon {
			// [7-8] Ni number of points along a parallel
			val gridNi = gribInputStream.readUInt(2)

			// [9-10] Nj number of points along a meridian
			val gridNj = gribInputStream.readUInt(2)

			// [11-13] La1 latitude of first grid point
			val latitudeOfFirstGridPoint = gribInputStream.readSMInt(3) / 1000.0

			// [14-16] Lo1 longitude of first grid point
			val longitudeOfFirstGridPoint = gribInputStream.readSMInt(3) / 1000.0

			// [17] Resolution and component flags (see Code table 7)
			// TABLE 7 - RESOLUTION AND COMPONENT FLAGS (GDS Octet 17)
			// Bit Value Meaning
			//  1    0   Direction increments not given
			//       1   Direction increments given
			//  2    0   Earth assumed spherical with radius = 6367.47 km
			//       1   Earth assumed oblate spheroid with size as determined by IAU in 1965:
			//           6378.160 km, 6356.775 km, f = 1/297.0
			// 3-4       reserved (set to 0)
			//  5    0   u- and v-components of vector quantities resolved relative to easterly and northerly directions
			//       1   u- and v-components of vector quantities resolved relative to the defined grid in the direction
			//           of increasing x and y (or i and j) coordinates respectively
			// 6-8       reserved (set to 0)
			val resolutionAndComponentFlags = gribInputStream.readUInt(1)
			val incrementsGiven = resolutionAndComponentFlags and 0x80 == 0x80
			val earthShapeSpheroid = resolutionAndComponentFlags and 0x40 == 0x40
			if (earthShapeSpheroid) {
				Logger.error("GRIB record assumes Earth is an oblate spheroid. This is not supported yet.")
			}
			val uvResolvedToGrid = resolutionAndComponentFlags and 0x08 == 0x08
			if (uvResolvedToGrid) {
				Logger.error("GRIB record resolves u- and v-components of vector quantities are relative to" +
						" the defined grid. This is not supported yet.")
			}

			// [18-20] La2 latitude of last grid point
			val latitudeOfLastGridPoint = gribInputStream.readSMInt(3) / 1000.0

			// [21-23] Lo2 longitude of last grid point
			val longitudeOfLastGridPoint = gribInputStream.readSMInt(3) / 1000.0

			var iDirectionIncrement: Double
			var jDirectionIncrement: Double
			if (incrementsGiven) { // MSB seems to indicate signedness, but this is taken care of by scanMode, so we use abs()
				iDirectionIncrement = abs(gribInputStream.readSMInt(2) / 1000.0)
				jDirectionIncrement = abs(gribInputStream.readSMInt(2) / 1000.0)
			} else { // calculate increments
				gribInputStream.skip(4)
				iDirectionIncrement = (longitudeOfLastGridPoint - longitudeOfFirstGridPoint) / gridNi
				jDirectionIncrement = (latitudeOfLastGridPoint - latitudeOfFirstGridPoint) / gridNj
			}

			// [28] Scan Mode
			val scanningMode = gribInputStream.readUInt(1)

			// [29-32] Set to zero (reserved)
			gribInputStream.skip(4)

			val iPositiveDirection = scanningMode and 0x80 != 0x80
			val jPositiveDirection = scanningMode and 0x40 == 0x40
			if (!iPositiveDirection) iDirectionIncrement *= -1.0
			if (!jPositiveDirection) jDirectionIncrement *= -1.0
			//val gridLatSP = -90.0
			//val gridLngSP = 0.0
			//val gridRotAngle = 0.0
			return Grib1GDSLatLon(numberOfVerticalCoordinateValues, pvlLocation, gridNi, gridNj,
					latitudeOfFirstGridPoint, longitudeOfFirstGridPoint, resolutionAndComponentFlags,
					latitudeOfLastGridPoint, longitudeOfLastGridPoint, iDirectionIncrement, jDirectionIncrement,
					scanningMode)
		}
	}

	override val length: Int = 32

	override val dataRepresentationType: Int = 0

	override val gridCols: Int
		get() = gridNi

	override val gridRows: Int
		get() = gridNj

	override val gridDeltaX: Double
		get() = iDirectionIncrement

	override val gridDeltaY: Double
		get() = jDirectionIncrement

	/** @see Grib1RecordGDS.isUVEastNorth */
	override val isUVEastNorth: Boolean
		get() = resolutionAndComponentFlags and 0x08 == 0

	override val xCoords: DoubleArray
		get() = getXCoords(true)

	/** Longitude coordinates */
	private fun getXCoords(convertTo180: Boolean): DoubleArray {
		val coords = DoubleArray(gridNi)
		for (x in 0 until gridNi) {
			var longi: Double = longitudeOfFirstGridPoint + x * iDirectionIncrement
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

	override val yCoords: DoubleArray
		get() {
			val coords = DoubleArray(gridNj)
			for (y in 0 until gridNj) {
				val lati: Double = latitudeOfFirstGridPoint + y * jDirectionIncrement
				if (lati > 90.0 || lati < -90.0) Logger.error("GribGDSLatLon.getYCoords: latitude out of range (-90 to 90).")
				coords[y] = lati
			}
			return coords
		}// move x-coordinates to the range -180..180

	override val coords: Array<DoubleArray>
		get() {
			val coords = Array(gridNj * gridNi) { DoubleArray(2) }
			var k = 0
			for (y in 0 until gridNj) {
				for (x in 0 until gridNi) {
					var longi: Double = longitudeOfFirstGridPoint + x * iDirectionIncrement
					val lati: Double = latitudeOfFirstGridPoint + y * jDirectionIncrement

					// move x-coordinates to the range -180..180
					if (longi >= 180.0) longi -= 360.0
					if (longi < -180.0) longi += 360.0
					if (lati > 90.0 || lati < -90.0) Logger.error(
							"GribGDSLatLon.getGridCoords: latitude out of range (-90 to 90).")
					coords[k][0] = longi
					coords[k][1] = lati
					k++
				}
			}
			return coords
		}

	override val dataIndices: Sequence<Int>
		get() = generateSequence(0 to 0) { (i, _) -> (i + 1) to getDataIndex(i + 1) }
				.take(gridNi * gridNj)
				.map { (_, index) -> index }

	override fun getDataIndex(sequence: Int) = getDataIndex(sequence % gridNi, sequence / gridNi)

	override fun getDataIndex(latitude: Double, longitude: Double): Int {
		val i = ((longitude - longitudeOfFirstGridPoint) / iDirectionIncrement).roundToInt()
		val j = ((latitude - latitudeOfFirstGridPoint) / jDirectionIncrement).roundToInt()
		return getDataIndex(i, j)
	}

	// Adjacent points in i direction are consecutive
	private fun getDataIndex(i: Int, j: Int): Int = if (scanningMode and 0x20 != 0x20)
		gridNi * j + i else gridNj * i + j

	// Attributes for Lat/Lon grid not included in GribRecordGDS
	// None!  The Lat/Lon grid is the most basic, and all attributes match
	//   the original GribRecordGDS

	override fun writeTo(outputStream: GribOutputStream) {
		super.writeTo(outputStream) // [1-6]
		outputStream.writeUInt(gridNi, bytes = 2) // [7-8]
		outputStream.writeUInt(gridNj, bytes = 2) // [9-10]
		outputStream.writeSMInt((latitudeOfFirstGridPoint * 1000.0).toInt(), bytes = 3) // [11-13]
		outputStream.writeSMInt((longitudeOfFirstGridPoint * 1000.0).toInt(), bytes = 3) // [14-16]
		outputStream.writeUInt(resolutionAndComponentFlags, bytes = 1) // [17]
		outputStream.writeSMInt((latitudeOfLastGridPoint * 1000.0).toInt(), bytes = 3) // [18-20]
		outputStream.writeSMInt((longitudeOfLastGridPoint * 1000.0).toInt(), bytes = 3) // [21-23]
		outputStream.writeSMInt(abs(iDirectionIncrement * 1000.0).toInt(), bytes = 2) // [24-25]
		outputStream.writeSMInt(abs(jDirectionIncrement * 1000.0).toInt(), bytes = 2) // [26-27]
		outputStream.writeUInt(scanningMode, bytes = 1) // [28]
		outputStream.write(ByteArray(4)) // [29-32]
	}

	/** @see Grib1RecordGDS.compare     */
	override fun compare(gds: Grib1RecordGDS): Int {
		if (this == gds) return 0
		// not equal, so either less than or greater than.
		// check if gds is less, if not, then gds is greater

		if (dataRepresentationType > gds.dataRepresentationType) return -1
		if (gds !is Grib1GDSLatLon) return dataRepresentationType.compareTo(gds.dataRepresentationType)
		if (resolutionAndComponentFlags > gds.resolutionAndComponentFlags) return -1
		if (scanningMode > gds.scanningMode) return -1
		if (gridNi > gds.gridNi) return -1
		if (gridNj > gds.gridNj) return -1
		if (iDirectionIncrement > gds.iDirectionIncrement) return -1
		if (jDirectionIncrement > gds.jDirectionIncrement) return -1
		if (latitudeOfFirstGridPoint > gds.latitudeOfFirstGridPoint) return -1
		if (latitudeOfLastGridPoint > gds.latitudeOfLastGridPoint) return -1
		//if (gridLatSP > gds.gridLatSP) return -1
		if (longitudeOfFirstGridPoint > gds.longitudeOfFirstGridPoint) return -1
		return if (longitudeOfLastGridPoint > gds.longitudeOfLastGridPoint) return -1 else 1
		//if (gridLngSP > gds.gridLngSP) return -1
		//return if (gridRotAngle > gds.gridRotAngle) -1 else 1
		// if here, then something must be greater than something else - doesn't matter what
	}

	override fun equals(other: Any?) = this === other
			|| other is Grib1GDSLatLon
			&& super.equals(other)
			&& gridNi == other.gridNi
			&& gridNj == other.gridNj
			&& latitudeOfFirstGridPoint == other.latitudeOfFirstGridPoint
			&& longitudeOfFirstGridPoint == other.longitudeOfFirstGridPoint
			&& resolutionAndComponentFlags == other.resolutionAndComponentFlags
			&& latitudeOfLastGridPoint == other.latitudeOfLastGridPoint
			&& longitudeOfLastGridPoint == other.longitudeOfLastGridPoint
			&& iDirectionIncrement == other.iDirectionIncrement
			&& jDirectionIncrement == other.jDirectionIncrement
			&& scanningMode == other.scanningMode

	override fun hashCode(): Int {
		var result = super.hashCode()
		result = 31 * result + gridNi
		result = 31 * result + gridNj
		result = 31 * result + latitudeOfFirstGridPoint.hashCode()
		result = 31 * result + longitudeOfFirstGridPoint.hashCode()
		result = 31 * result + resolutionAndComponentFlags
		result = 31 * result + latitudeOfLastGridPoint.hashCode()
		result = 31 * result + longitudeOfLastGridPoint.hashCode()
		result = 31 * result + iDirectionIncrement.hashCode()
		result = 31 * result + jDirectionIncrement.hashCode()
		result = 31 * result + scanningMode
		return result
	}

	// TODO include more information about this projection
	override fun toString(): String = listOfNotNull(
			"GRIB1 GDS section (${this.gridNi}x${this.gridNj}):",
			"\tLatLng Grid",
			"\t\tLat: ${latitudeOfFirstGridPoint} to ${latitudeOfLastGridPoint} (dy ${jDirectionIncrement}))",
			"\t\tLng: ${longitudeOfFirstGridPoint} to ${longitudeOfLastGridPoint} (dx ${iDirectionIncrement})")
			.joinToString("\n")
}
