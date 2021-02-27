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

import mt.edu.um.cf2.jgribx.*
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.roundToInt

/**
 * ### [Grid Definition Template 3.0: Latitude/Longitude (or equidistant cylindrical, or Plate Carree)](https://www.nco.ncep.noaa.gov/pmb/docs/grib2/grib2_doc/grib2_temp3-0.shtml)
 *
 *    | Octet | # | Value                                                                                       |
 *    |-------|---|---------------------------------------------------------------------------------------------|
 *    | 15    | 1 | Shape of the Earth (See Code Table 3.2)                                                     |
 *    | 16    | 1 | Scale Factor of radius of spherical Earth                                                   |
 *    | 17-20 | 4 | Scale value of radius of spherical Earth                                                    |
 *    | 21    | 1 | Scale factor of major axis of oblate spheroid Earth                                         |
 *    | 22-25 | 4 | Scaled value of major axis of oblate spheroid Earth                                         |
 *    | 26    | 1 | Scale factor of minor axis of oblate spheroid Earth                                         |
 *    | 27-30 | 4 | Scaled value of minor axis of oblate spheroid Earth                                         |
 *    | 31-34 | 4 | Ni — number of points along a parallel                                                      |
 *    | 35-38 | 4 | Nj — number of points along a meridian                                                      |
 *    | 39-42 | 4 | Basic angle of the initial production domain (see Note 1)                                   |
 *    | 43-46 | 4 | Subdivisions of basic angle used to define extreme longitudes and latitudes, and direction  |
 *    |       |   | increments (see Note 1)                                                                     |
 *    | 47-50 | 4 | La1 — latitude of first grid point (see Note 1)                                             |
 *    | 51-54 | 4 | Lo1 — longitude of first grid point (see Note 1)                                            |
 *    | 55    | 1 | Resolution and component flags (see Flag Table 3.3)                                         |
 *    | 56-59 | 4 | La2 — latitude of last grid point (see Note 1)                                              |
 *    | 60-63 | 4 | Lo2 — longitude of last grid point (see Note 1)                                             |
 *    | 64-67 | 4 | Di — i direction increment (see Notes 1 and 5)                                              |
 *    | 68-71 | 4 | Dj — j direction increment (see Note 1 and 5)                                               |
 *    | 72    | 1 | Scanning mode (flags — see Flag Table 3.4 and Note 6)                                       |
 *    | 73-nn |   | List of number of points along each meridian or parallel (These octets are only present for |
 *    |       |   | quasi-regular grids as described in notes 2 and 3)                                          |
 *
 * @param gridDefinitionSource (`6`)     Source of grid definition (see
 *                                       [Table 3.0](https://www.nco.ncep.noaa.gov/pmb/docs/grib2/grib2_doc/grib2_table3-0.shtml))
 *                                       If octet `6` is not zero, octets `15-xx` (`15-nn` if octet `11` is zero) may not
 *                                       be supplied. This should be documented with all bits set to `1` in the grid
 *                                       definition template number.
 * @param numberOfDataPoints   (`7-10`)  Number of data points
 * @param nBytes               (`11`)    Number of octets for optional list of numbers defining number of points (see note 2)
 * @param interpretation       (`12`)    Interpetation of list of numbers defining number of points
 *                                       (see [Table 3.11](https://www.nco.ncep.noaa.gov/pmb/docs/grib2/grib2_doc/grib2_table3-11.shtml))
 * @param earthShape           (`15`)    Shape of the Earth (see
 *                                       [Code Table 3.2](https://www.nco.ncep.noaa.gov/pmb/docs/grib2/grib2_doc/grib2_table3-2.shtml))
 * @param radiusScaleFactor    (`16`)    Scale Factor of radius of spherical Earth
 * @param radiusScaledValue    (`17-20`) Scale value of radius of spherical Earth
 * @param majorScaleFactor     (`21`)    Scale factor of major axis of oblate spheroid Earth
 * @param majorScaledValue     (`22-25`) Scaled value of major axis of oblate spheroid Earth
 * @param minorScaleFactor     (`26`)    Scale factor of minor axis of oblate spheroid Earth
 * @param minorScaledValue     (`27-30`) Scaled value of minor axis of oblate spheroid Earth
 * @param gridNi               (`31-34`) `Ni` — number of points along a parallel
 * @param gridNj               (`35-38`) `Nj` — number of points along a meridian
 * @param basicAngle           (`39-42`) Basic angle of the initial production domain (see Note 1)
 * @param basicAngleSubdiv     (`43-46`) Subdivisions of basic angle used to define extreme longitudes and latitudes,
 *                                       and direction increments (see Note 1)
 * @param lat1                 (`47-50`) `La1` — latitude of first grid point (see Note 1)
 * @param lon1                 (`51-54`) `Lo1` — longitude of first grid point (see Note 1)
 * @param flags                (`55`)    Resolution and component flags (see
 *                                       [Flag Table 3.3](https://www.nco.ncep.noaa.gov/pmb/docs/grib2/grib2_doc/grib2_table3-3.shtml))
 * @param lat2                 (`56-59`) `La2` — latitude of last grid point (see Note 1)
 * @param lon2                 (`60-63`) `Lo2` — longitude of last grid point (see Note 1)
 * @param gridDi               (`64-67`) `Di` — i direction increment (see Notes 1 and 5)
 * @param gridDj               (`68-71`) `Dj` — j direction increment (see Note 1 and 5)
 * @param scanMode             (`72`)    Scanning mode (flags — see
 *                                       [Flag Table 3.4](https://www.nco.ncep.noaa.gov/pmb/docs/grib2/grib2_doc/grib2_table3-4.shtml)
 *                                       and Note 6)
 * @param additionalPoints     (`73-nn`) List of number of points along each meridian or parallel (These octets are
 *                                       only present for quasi-regular grids as described in notes 2 and 3)
 *
 * @author spidru
 * @author Jan Kubovy [jan@kubovy.eu]
 */
class Grib2RecordGDSLatLon internal constructor(gridDefinitionSource: Int,
												numberOfDataPoints: Int,
												nBytes: Int,
												interpretation: Int,

												private val earthShape: Int,
												private val radiusScaleFactor: Int,
												private val radiusScaledValue: Int,
												private val majorScaleFactor: Int,
												private val majorScaledValue: Int,
												private val minorScaleFactor: Int,
												private val minorScaledValue: Int,
												gridNi: Int,
												gridNj: Int,
												private val basicAngle: Int,
												private val basicAngleSubdiv: Int,
												private var lat1: Double,
												private var lon1: Double,
												private val flags: Int,
												private var lat2: Double,
												private var lon2: Double,
												private val gridDi: Double,
												private val gridDj: Double,
												internal val scanMode: ScanMode,
												internal val additionalPoints: ByteArray) :
		Grib2RecordGDS(gridDefinitionSource, numberOfDataPoints, nBytes, interpretation) {

	companion object {
		internal fun readFromStream(gribInputStream: GribInputStream,
									length: Int,
									gridDefinitionSource: Int,
									numberOfDataPoints: Int,
									nBytes: Int,
									interpolation: Int): Grib2RecordGDSLatLon {
			/* [15] Grid Definition Template Number */
			val earthShape = gribInputStream.readUInt(1)
			/* [16] Scale Factor of radius of spherical Earth */
			val radiusScaleFactor = gribInputStream.readUInt(1)
			/* [17-20] Scale value of radius of spherical Earth */
			val radiusScaledValue = gribInputStream.readUInt(4)
			/* [21] Scale factor of major axis of oblate spheroid Earth */
			val majorScaleFactor = gribInputStream.readUInt(1)
			/* [22-25] Scaled value of major axis of oblate spheroid Earth */
			val majorScaledValue = gribInputStream.readUInt(4)
			/* [26] Scale factor of minor axis of oblate spheroid Earth */
			val minorScaleFactor = gribInputStream.readUInt(1)
			/* [27-30] Scaled value of minor axis of oblate spheroid Earth */
			val minorScaledValue = gribInputStream.readUInt(4)
			/* [31-34] Ni — number of points along a parallel */
			val gridNi = gribInputStream.readUInt(4)
			/* [35-38] Nj — number of points along a meridian */
			val gridNj = gribInputStream.readUInt(4)
			/* [39-42] Basic angle of the initial production domain (see Note 1) */
			val basicAngle = gribInputStream.readUInt(4)
			/* [43-46] Subdivisions of basic angle used to define extreme longitudes and latitudes, and direction increments */
			val basicAngleSubdiv = gribInputStream.readUInt(4)

			/* [47-50] La1 — latitude of first grid point (see Note 1) */
			val lat1: Double
			/* [51-54] Lo1 — longitude of first grid point (see Note 1) */
			val lon1: Double
			if (basicAngle == 0) {
				lat1 = gribInputStream.readSMInt(4) / 1.0e6
				lon1 = gribInputStream.readSMInt(4) / 1.0e6
			} else TODO("Not implemented")

			/* [55] Resolution and component flags */
			val flags = gribInputStream.readUInt(1)
			val iDirectionIncrementsGiven = flags and 0x20 == 0x20
			val jDirectionIncrementsGiven = flags and 0x10 == 0x10

			/* [56-59] La2 — latitude of last grid point (see Note 1) */
			val lat2: Double
			/* [60-63] Lo2 — longitude of last grid point (see Note 1) */
			val lon2: Double
			if (basicAngle == 0) {
				lat2 = gribInputStream.readSMInt(4) / 1.0e6
				lon2 = gribInputStream.readSMInt(4) / 1.0e6
			} else TODO("Not implemented")

			/* [64-67] i-Direction Increment Di */
			var gridDi = 0.0
			if (iDirectionIncrementsGiven) {
				if (basicAngle == 0) {
					gridDi = gribInputStream.readUInt(4) / 1.0e6
				} else TODO("Not implemented")
			}
			/* [68-71] j-Direction Increment Dj */
			var gridDj = 0.0
			if (jDirectionIncrementsGiven) {
				if (basicAngle == 0) {
					gridDj = gribInputStream.readUInt(4) / 1.0e6
				} else TODO("Not implemented")
			}
			/* [72] Scanning Mode */
			val scanMode = ScanMode(gribInputStream.readUInt(1).toByte()).also { scanMode ->
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

			/* [73-nn] List of number of points along each meridian or parallel */
			val additionalPoints = ByteArray(length - 72)
			gribInputStream.read(additionalPoints)

			return Grib2RecordGDSLatLon(gridDefinitionSource, numberOfDataPoints, nBytes, interpolation,
					earthShape, radiusScaleFactor, radiusScaledValue, majorScaleFactor, majorScaledValue,
					minorScaleFactor, minorScaledValue, gridNi, gridNj, basicAngle, basicAngleSubdiv, lat1, lon1, flags,
					lat2, lon2, gridDi, gridDj, scanMode, additionalPoints)
		}
	}

	override val length: Int
		get() = 72 + additionalPoints.size

	override val gridType: Int = 0

	internal var gridNi: Int = gridNi
		private set

	internal var gridNj: Int = gridNj
		private set

	override val coords: Array<DoubleArray>
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

	override val xCoords: DoubleArray
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

	override val yCoords: DoubleArray
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

	override val dataIndices: Sequence<Int>
		get() = generateSequence(0 to 0) { (i, _) -> (i + 1) to getDataIndex(i + 1) }
				.take(gridNi * gridNj)
				.map { (_, index) -> index }

	internal fun closestLat1(latitude: Double): Double = yCoords
			.filter { if (gridDj >= 0) latitude > it else latitude < it }
			.minByOrNull { abs(latitude - it) }
			?: throw TransformationException("Could not find closest first latitude")

	internal fun closestLon1(longitude: Double): Double = xCoords
			.filter { if (gridDj >= 0) longitude > it else longitude < it }
			.minByOrNull { abs(longitude - it) }
			?: throw TransformationException("Could not find closest first longitude")

	internal fun iPointsCount(lng1: Double, lng2: Double): Int = ceil((lng2 - lng1) / gridDi).toInt() + 1

	internal fun jPointsCount(lat1: Double, lat2: Double): Int = ceil((lat2 - lat1) / gridDj).toInt() + 1

	override fun getDataIndex(sequence: Int): Int = getDataIndex(sequence % gridNi, sequence / gridNi)

	override fun getDataIndex(latitude: Double, longitude: Double): Int {
		// double[] xcoords = gds.getGridXCoords();
		// double[] ycoords = gds.getGridYCoords();
		val j = ((latitude - gridLatStart) / gridDeltaY).roundToInt() // j = index_closest_latitude
		val i = ((longitude - gridLonStart) / gridDeltaX).roundToInt() // i = index_closest_longitude

		// double closest_latitude = ycoords[index_closest_latitude];
		// double closest_longitude = xcoords[index_closest_longitude];
		return getDataIndex(i, j)
	}

	private fun getDataIndex(i: Int, j: Int): Int {
		if (scanMode.iDirectionEvenRowsOffset
				|| scanMode.iDirectionOddRowsOffset
				|| scanMode.jDirectionOffset
				|| scanMode.rowsNiNjPoints
				|| scanMode.rowsZigzag) {
			Logger.error("Unsupported scan mode ${scanMode} found")
		}
		return if (scanMode.iDirectionConsecutive) gridNi * j + i else gridNj * i + j
	}

	override fun cutOut(north: Double, east: Double, south: Double, west: Double) {
		val inputLat1 = if (gridDj >= 0) south else north
		val inputLng1 = if (gridDi >= 0) west else east
		val inputLat2 = if (gridDj >= 0) north else south
		val inputLng2 = if (gridDi >= 0) east else west

		val closestLat1 = closestLat1(inputLat1)
		val closestLng1 = closestLon1(inputLng1)

		val newNj = jPointsCount(closestLat1, inputLat2)
		val newNi = iPointsCount(closestLng1, inputLng2)

		val closestLat2 = closestLat1 + newNj * gridDj
		val closestLng2 = closestLng1 + newNi * gridDi
		//if (closestLat2 > south) throw TransformationException("South exceeded")
		//if (lon1 <= lon2 && closestLat1 > closestLat2
		//		|| lon1 > lon2 && closestLat1 <= closestLat2) throw TransformationException("West exceeded")

		// Update
		lat1 = closestLat1
		lon1 = closestLng1
		gridNj = newNj
		gridNi = newNi
		lat2 = closestLat2
		lon2 = closestLng2
	}

	override fun writeTo(outputStream: GribOutputStream) {
		super.writeTo(outputStream) // [1-5] length, section number + [6-14] GDS
		outputStream.writeUInt(earthShape, bytes = 1) // [15]
		outputStream.writeUInt(radiusScaleFactor, bytes = 1) // [16]
		outputStream.writeUInt(radiusScaledValue, bytes = 4) // [17-20]
		outputStream.writeUInt(majorScaleFactor, bytes = 1) // [21]
		outputStream.writeUInt(majorScaledValue, bytes = 4) // [22-25]
		outputStream.writeUInt(minorScaleFactor, bytes = 1) // [26]
		outputStream.writeUInt(minorScaledValue, bytes = 4) // [27-30]
		outputStream.writeUInt(gridNi, bytes = 4) // [31-34]
		outputStream.writeUInt(gridNj, bytes = 4) // [35-38]
		outputStream.writeUInt(basicAngle, bytes = 4) // [39-42]
		outputStream.writeUInt(basicAngleSubdiv, bytes = 4) // [43-46]
		if (basicAngle == 0) {
			outputStream.writeSMInt((lat1 * 1.0e6).toInt(), bytes = 4) // [47-50]
			outputStream.writeSMInt((lon1 * 1.0e6).toInt(), bytes = 4) // [51-54]
		} else {
			TODO("Not implemented")
		}
		outputStream.writeUInt(flags, bytes = 1) // [55]
		if (basicAngle == 0) {
			outputStream.writeUInt((lat2 * 1.0e6).toInt(), bytes = 4) // [56-59]
			outputStream.writeUInt((lon2 * 1.0e6).toInt(), bytes = 4) // [60-63]
		}
		val iDirectionIncrementsGiven = flags and 0x20 == 0x20
		val jDirectionIncrementsGiven = flags and 0x10 == 0x10
		if (iDirectionIncrementsGiven) {
			if (basicAngle == 0) {
				outputStream.writeUInt((gridDi * 1.0e6 * (if (scanMode.iDirectionPositive) 1 else -1)).toInt(), bytes = 4) // [64-67]
			} else {
				TODO("Not implemented")
			}
		}
		if (jDirectionIncrementsGiven) {
			if (basicAngle == 0) {
				outputStream.writeUInt((gridDj * 1.0e6 * (if (scanMode.jDirectionPositive) 1 else -1)).toInt(), bytes = 4) // [68-71]
			} else {
				TODO("Not implemented")
			}
		}
		outputStream.writeByte(scanMode.flags) // [72]
		outputStream.write(additionalPoints) // [73-nn]
	}

	override fun equals(other: Any?) = this === other
			|| super.equals(other)
			&& other is Grib2RecordGDSLatLon
			&& earthShape == other.earthShape
			&& radiusScaleFactor == other.radiusScaleFactor
			&& radiusScaledValue == other.radiusScaledValue
			&& majorScaleFactor == other.majorScaleFactor
			&& majorScaledValue == other.majorScaledValue
			&& minorScaleFactor == other.minorScaleFactor
			&& minorScaledValue == other.minorScaledValue
			&& gridNi == other.gridNi
			&& gridNj == other.gridNj
			&& basicAngle == other.basicAngle
			&& basicAngleSubdiv == other.basicAngleSubdiv
			&& abs(lat1 - other.lat1) < FLOAT_PRECISION
			&& abs(lon1 - other.lon1) < FLOAT_PRECISION
			&& flags == other.flags
			&& abs(lat2 - other.lat2) < FLOAT_PRECISION
			&& abs(lon2 - other.lon2) < FLOAT_PRECISION
			&& abs(gridDi - other.gridDi) < FLOAT_PRECISION
			&& abs(gridDj - other.gridDj) < FLOAT_PRECISION
			&& scanMode == other.scanMode
			&& additionalPoints.contentEquals(other.additionalPoints)

	override fun hashCode(): Int {
		var result = super.hashCode()
		result = 31 * result + earthShape
		result = 31 * result + radiusScaleFactor
		result = 31 * result + radiusScaledValue
		result = 31 * result + majorScaleFactor
		result = 31 * result + majorScaledValue
		result = 31 * result + minorScaleFactor
		result = 31 * result + minorScaledValue
		result = 31 * result + gridNi
		result = 31 * result + gridNj
		result = 31 * result + basicAngle
		result = 31 * result + basicAngleSubdiv
		result = 31 * result + lat1.hashCode()
		result = 31 * result + lon1.hashCode()
		result = 31 * result + flags
		result = 31 * result + lat2.hashCode()
		result = 31 * result + lon2.hashCode()
		result = 31 * result + gridDi.hashCode()
		result = 31 * result + gridDj.hashCode()
		result = 31 * result + scanMode.hashCode()
		result = 31 * result + additionalPoints.contentHashCode()
		return result
	}

	override fun toString() = "Grib2RecordGDSLatLon(earthShape=${earthShape}, radiusScaleFactor=${radiusScaleFactor}," +
			" radiusScaledValue=${radiusScaledValue}, majorScaleFactor=${majorScaleFactor}," +
			" majorScaledValue=${majorScaledValue}, minorScaleFactor=${minorScaleFactor}," +
			" minorScaledValue=${minorScaledValue}, gridNi=${gridNi}, gridNj=${gridNj}, basicAngle=${basicAngle}," +
			" basicAngleSubdiv=${basicAngleSubdiv}, lat1=${lat1}, lon1=${lon1}, flags=${flags}, lat2=${lat2}," +
			" lon2=${lon2}, gridDi=${gridDi}, gridDj=${gridDj}, scanMode=${scanMode}," +
			" additionalPoints=${additionalPoints.contentToString()})"
}
