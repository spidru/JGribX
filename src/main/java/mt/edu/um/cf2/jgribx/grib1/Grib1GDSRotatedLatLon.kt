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

/**
 * ### [Grid 10: Rotated latitude/longitude](https://apps.ecmwf.int/codes/grib/format/grib1/grids/10/)
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
 *     | 33-35  | 3 | latitudeOfSouthernPole      | signed   | Latitude of the southern pole in millidegrees     |
 *     |        |   |                             |          | (integer)                                         |
 *     | 36-38  | 3 | longitudeOfSouthernPole     | signed   | Longitude of the southern pole in millidegrees    |
 *     |        |   |                             |          | (integer)                                         |
 *     | 39-42  | 4 | angleOfRotationInDegrees    | ibmfloat | Angle of rotation (represented in the same way as |
 *     |        |   |                             |          | the reference value)                              |
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
 * @param latitudeOfSouthernPole           (`33-35`)    Scanning mode (flags see Flag/Code table 8)
 * @param latitudeOfSouthernPole           (`36-38`)    Scanning mode (flags see Flag/Code table 8)
 * @param angleOfRotationInDegrees         (`39-42`)    Scanning mode (flags see Flag/Code table 8)
 *
 * @throws java.io.IOException   if stream can not be opened etc.
 * @throws NoValidGribException  if stream contains no valid GRIB file
 * @author Richard Gonzalez
 * @author Jan Kubovy [jan@kubovy.eu]
 * based heavily on the original GribRecordGDS
 */
class Grib1GDSRotatedLatLon(numberOfVerticalCoordinateValues: Int,
							pvlLocation: Int,

							gridNi: Int,
							gridNj: Int,
							latitudeOfFirstGridPoint: Double,
							longitudeOfFirstGridPoint: Double,
							resolutionAndComponentFlags: Int,
							latitudeOfLastGridPoint: Double,
							longitudeOfLastGridPoint: Double,
							iDirectionIncrement: Double,
							jDirectionIncrement: Double,
							scanningMode: Int,

							val latitudeOfSouthernPole: Double,
							val longitudeOfSouthernPole: Double,
							val angleOfRotationInDegrees: Float) :
		Grib1GDSLatLon(
				numberOfVerticalCoordinateValues,
				pvlLocation,

				gridNi,
				gridNj,
				latitudeOfFirstGridPoint,
				longitudeOfFirstGridPoint,
				resolutionAndComponentFlags,
				latitudeOfLastGridPoint,
				longitudeOfLastGridPoint,
				iDirectionIncrement,
				jDirectionIncrement,
				scanningMode) {

	constructor(gds: Grib1GDSLatLon,
				latitudeOfSouthernPole: Double,
				longitudeOfSouthernPole: Double,
				angleOfRotationInDegrees: Float) : this(
			gds.numberOfVerticalCoordinateValues,
			gds.pvlLocation,

			gds.gridNi,
			gds.gridNj,
			gds.latitudeOfFirstGridPoint,
			gds.longitudeOfFirstGridPoint,
			gds.resolutionAndComponentFlags,
			gds.latitudeOfLastGridPoint,
			gds.longitudeOfLastGridPoint,
			gds.iDirectionIncrement,
			gds.jDirectionIncrement,
			gds.scanningMode,

			latitudeOfSouthernPole,
			longitudeOfSouthernPole,
			angleOfRotationInDegrees)

	companion object {
		internal fun readFromStream(gribInputStream: GribInputStream,
									numberOfVerticalCoordinateValues: Int,
									pvlLocation: Int): Grib1GDSRotatedLatLon {
			val gdsLatLon = Grib1GDSLatLon.readFromStream(gribInputStream, numberOfVerticalCoordinateValues, pvlLocation)
			Logger.warning("GRIB Record uses rotated LatLon grid. This is untested.")
			val latitudeOfSouthernPole = gribInputStream.readSMInt(3) / 1000.0
			val longitudeOfSouthernPole = gribInputStream.readSMInt(3) / 1000.0
			val angleOfRotationInDegrees = gribInputStream.readFloatIBM()

			return Grib1GDSRotatedLatLon(gdsLatLon, latitudeOfSouthernPole, longitudeOfSouthernPole,
					angleOfRotationInDegrees)
		}
	}

	override val length: Int = 42

	override val dataRepresentationType: Int = 10

	override fun writeTo(outputStream: GribOutputStream) {
		super.writeTo(outputStream) // [1-32]
		outputStream.writeSMInt((latitudeOfSouthernPole * 1000.0).toInt(), bytes = 3) // [33-35]
		outputStream.writeSMInt((longitudeOfSouthernPole * 1000.0).toInt(), bytes = 3) // [36-38]
		outputStream.writeFloatIBM(angleOfRotationInDegrees) // [39-42]
	}

	override fun equals(other: Any?) = this === other
			|| other is Grib1GDSRotatedLatLon
			&& super.equals(other)
			&& latitudeOfSouthernPole == other.latitudeOfSouthernPole
			&& longitudeOfSouthernPole == other.longitudeOfSouthernPole
			&& angleOfRotationInDegrees == other.angleOfRotationInDegrees

	override fun hashCode(): Int {
		var result = super.hashCode()
		result = 31 * result + latitudeOfSouthernPole.hashCode()
		result = 31 * result + longitudeOfSouthernPole.hashCode()
		result = 31 * result + angleOfRotationInDegrees.hashCode()
		return result
	}

	// TODO include more information about this projection
	override fun toString(): String = listOfNotNull(
			"GRIB1 GDS section (${this.gridNi}x${this.gridNj}):",
			"\tRotated LatLng Grid",
			"\t\tLat: ${latitudeOfFirstGridPoint} to ${latitudeOfLastGridPoint} (dy ${jDirectionIncrement}))",
			"\t\tLng: ${longitudeOfFirstGridPoint} to ${longitudeOfLastGridPoint} (dx ${iDirectionIncrement})",
			"\t\tSouth pole: lat=${latitudeOfSouthernPole} lng=${longitudeOfSouthernPole}",
			"\t\tRotation angle: ${angleOfRotationInDegrees}")
			.joinToString("\n")
}
