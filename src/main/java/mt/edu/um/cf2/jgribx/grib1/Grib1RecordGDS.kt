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
import mt.edu.um.cf2.jgribx.NoValidGribException
import mt.edu.um.cf2.jgribx.NotSupportedException

/**
 * ### [SSection 2: Grid description section](https://apps.ecmwf.int/codes/grib/format/grib1/sections/2/)
 *
 *     | Octets | # | Key                              | Type      | Content                                         |
 *     |--------|---|----------------------------------|-----------|-------------------------------------------------|
 *     | 1-3    | 3 | section4Length                   | unsigned  | Length of section                               |
 *     | 4      | 1 | numberOfVerticalCoordinateValues | unsigned  | NV number of vertical coordinate parameters     |
 *     | 5      | 1 | pvlLocation                      | unsigned  | PV location (octet number) of the list of       |
 *     |        |   |                                  |           | vertical coordinate parameters, if present; or  |
 *     |        |   |                                  |           | PL location (octet number) of the list of       |
 *     |        |   |                                  |           | numbers of points in each row (if no vertical   |
 *     |        |   |                                  |           | coordinate parameters are present), if present; |
 *     |        |   |                                  |           | or 255 (all bits set to 1) if neither are       |
 *     |        |   |                                  |           | present                                         |
 *     | 6      | 1 | dataRepresentationType           | codetable | Data representation type (see Code table 6)     |
 *     | 7-32   |   |                                  |           | Grid definition (according to data              |
 *     |        |   |                                  |           | representation type octet 6 above)              |
 *     | 33-42  |   |                                  |           | Extensions of grid definition for rotation or   |
 *     |        |   |                                  |           | stretching of the coordinate system or Lambert  |
 *     |        |   |                                  |           | conformal projection or Mercator projection     |
 *     | 33-44  |   |                                  |           | Extensions of grid definition for space view    |
 *     |        |   |                                  |           | perspective projection                          |
 *     | 33-52  |   |                                  |           | Extensions of grid definition for stretched and |
 *     |        |   |                                  |           | rotated coordinate system                       |
 *
 * - `PV`: List of vertical coordinate parameters (`length = NV × 4 octets`); if present, then `PL = 4NV + PV`
 * - `PL`: List of numbers of points in each row (`length = NROWS x 2` octets, where `NROWS` is the total number of rows
 *       defined within the grid description)
 *
 * See [Code Table 6: Data representation type ](https://apps.ecmwf.int/codes/grib/format/grib1/ctable/6/)
 *
 * 5 Okt 05 - Changed class to become abstract as intended by RDG all common methods between this class and all known
 * subclasses is changed to abstract methods, so it becomes more clear, which methods one should actually implement,
 * when adding support for a new type of GRIB files.
 *
 * 4 Sep 02 - Modified to be implemented using GribGDSFactory class.
 *
 * This class is used to store the first 32 octets of the GDS, which are common, or similar, in all GDS types.
 * Sometimes names vary slightly in Table D, but functionality is similar, e.g.
 *
 *     Grid type     Octet    Id
 *     Lat/Lon       7-8      Ni - Number of points along a latitude circle
 *     Lambert       7-8      Nx - Number of points along x-axis
 *
 * Other times, functionality is different, e.g.
 *
 *     Lat/Lon      18-20     La2 - latitude of grid point
 *     Lambert      18-20     Lov - the orientation of the grid
 *
 * However, all sets have at least 32 octets.  Those 32 are stored here, and the differences are resolved in the child
 * classes, and therefore, all attributes are set from the Child classes.
 *
 * The names of the attributes are the same JGrib originally used , for simplicity and continuity. The fact that some
 * grids use a different number of octets for doubles is irrelevant, as the conversion is stored, not the octets.
 *
 * The child classes should call the proper setters and getters.
 *
 * The class retains every bit of the original functionality, so it can continue to be used in legacy programs (still
 * limited to grid_type 0 and 10).
 *
 * New users should not create instances of this class directly (in fact, it should be changed to an abstract
 * class - it's on the to do list), but use the GribGDS factory instead, and add new child classes (e.g. GribGDSXxxx)
 * as needed for additional grid_types.
 *
 * @param numberOfVerticalCoordinateValues (`4`) NV number of vertical coordinate parameters
 * @param pvlLocation                      (`5`) PV location (octet number) of the list of vertical coordinate
 *                                               parameters, if present; or PL location (octet number) of the list of
 *                                               numbers of points in each row (if no vertical coordinate parameters
 *                                               are present), if present; or 255 (all bits set to 1) if neither are
 *                                               present
 * @author Benjamin Stark
 * @author Capt Richard D. Gonzalez, USAF (Modified original code)
 * @author Peter Gylling <peg at frv.dk> (Made class abstract)</peg>
 * @author Jan Kubovy [jan@kubovy.eu]
 */
abstract class Grib1RecordGDS(
		internal val numberOfVerticalCoordinateValues: Int,
		internal val pvlLocation: Int) : Grib1Section {

	companion object {
		/** Radius of earth used in calculating projections per table 7 - assumes spheroid */
		internal const val EARTH_RADIUS = 6367470.0

		fun readFromStream(gribInputStream: GribInputStream): Grib1RecordGDS {
			// octets 1-3 (GDS section length)
			val length = Grib1Section.readFromStream(gribInputStream)

			// [4] NV -- number of vertical coordinate parameters */
			val numberOfVerticalCoordinateValues = gribInputStream.readUINT(1)

			// [5] PV -- location (octet number) of the list of vertical coordinate parameters, if present; or
			//     PL -- location (octet number) of the list of numbers of points in each row (if no vertical coordinate parameters are present), if present; or
			//     255 (all bits set to 1) if neither are present
			val pvlLocation = gribInputStream.readUINT(1)

			// [6] Data representation type
			val dataRepresentationType = gribInputStream.readUINT(1)
			return when (dataRepresentationType) {
				0 -> Grib1GDSLatLon.readFromStream(gribInputStream, numberOfVerticalCoordinateValues, pvlLocation)
				1 -> throw NotSupportedException("Mercator projection is not yet supported")
				//3 -> Grib1GDSLambert.readFromStream(gribInputStream, numberOfVerticalCoordinateValues, pvlLocation)
				//5 -> Grib1GDSPolarStereo.readFromStream(gribInputStream, numberOfVerticalCoordinateValues, pvlLocation)
				10 -> Grib1GDSRotatedLatLon.readFromStream(gribInputStream, numberOfVerticalCoordinateValues, pvlLocation)
				else -> throw NotSupportedException("Unknown GDS type: ${dataRepresentationType}")
			}.takeIf { it.length == length } ?: throw NoValidGribException("GRS length mismatch")
		}
	}

	/** (`6`) Type of grid (See table 6) */
	abstract val dataRepresentationType: Int

	/** Number of grid columns. (Also Ni) */
	abstract val gridCols: Int

	/** Number of grid rows. (Also Nj) */
	abstract val gridRows: Int

	/** Latitude of grid start point. */
	abstract val latitudeOfFirstGridPoint: Double

	/** Longitude of grid start point. */
	abstract val longitudeOfFirstGridPoint: Double

	/** x-distance between two grid points can be delta-Lon or delta x. */
	abstract val gridDeltaX: Double

	/** y-distance of two grid points can be delta-Lat or delta y. */
	abstract val gridDeltaY: Double

	/** Scanning mode (See table 8). Get scan mode (sign of increments). *Only 64, 128 and 192 supported so far. */
	abstract val scanningMode: Int

	// rdg - the remaining coordinates are not common to all types, and as such
	//    should be removed.  They are left here (temporarily) for continuity.
	//    These should be implemented in a GribGDSxxxx child class.

	/** Get all longitide coordinates */
	abstract val xCoords: DoubleArray

	/** Get all latitude coordinates */
	abstract val yCoords: DoubleArray

	/** Get grid coordinates in longitude/latitude */
	abstract val gridCoords: DoubleArray

	/**
	 * Table J.Resolution and Component Flags,
	 *
	 *     bit 5 (from left) = 2^(8-5) = 8 = 0x08:
	 *       false = u and v components are relative to east, north
	 *       true = u and v components are relative to  grid x,y direction (i,j)
	 *
	 * @return true/false
	 */
	abstract val isUVEastNorth: Boolean

	// *** public methods **************************************************************
	// rdg - the basic getters can remain here, but other functionality should
	//    be moved to the child GribGDSxxxx classes.  For now, overriding these
	//    methods will work just fine.

	// peg - turned all common methods into abstract methods, so it will become
	//       easier to subclass with a new GDS type class, this way it's much
	//       more clear which methods is standard for all GDS types

	override fun writeTo(outputStream: GribOutputStream) {
		super.writeTo(outputStream)  // [1-3] length
		outputStream.writeUInt(numberOfVerticalCoordinateValues, bytes = 1) // [4]
		outputStream.writeUInt(pvlLocation, bytes = 1) // [5]
		outputStream.writeUInt(dataRepresentationType, bytes = 1) // [6]
	}

	/**
	 * rdg - added this method to be used in a comparator for sorting while extracting records.
	 * Not currently used in the JGrib library, but is used in a library I'm using that uses JGrib.
	 * @param gds - GribRecordGDS
	 * @return - -1 if gds is "less than" this, 0 if equal, 1 if gds is "greater than" this.
	 *
	 * @see java.util.Comparator.compare
	 */
	abstract fun compare(gds: Grib1RecordGDS): Int

	override fun equals(other: Any?) = this === other
			|| other is Grib1RecordGDS
			&& length == other.length
			&& numberOfVerticalCoordinateValues == other.numberOfVerticalCoordinateValues
			&& pvlLocation == other.pvlLocation
			&& dataRepresentationType == other.dataRepresentationType

	override fun hashCode(): Int {
		var result = length
		result = 31 * result + numberOfVerticalCoordinateValues
		result = 31 * result + pvlLocation
		result = 31 * result + dataRepresentationType
		return result
	}
}
