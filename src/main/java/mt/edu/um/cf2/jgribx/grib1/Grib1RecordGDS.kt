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
import mt.edu.um.cf2.jgribx.NotSupportedException

/**
 * A class that represents the grid definition section (GDS) of a GRIB record.
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
 * @author  Benjamin Stark
 * @author  Capt Richard D. Gonzalez, USAF (Modified original code)
 * @author  Peter Gylling <peg at frv.dk> (Made class abstract)</peg>
 * @version 3.0
 */
abstract class Grib1RecordGDS {
	companion object {
		/** Radius of earth used in calculating projections per table 7 - assumes spheroid */
		internal const val EARTH_RADIUS = 6367470.0

		fun readFromStream(inputStream: GribInputStream): Grib1RecordGDS {
			inputStream.mark(6)
			inputStream.skip(5)
			val type = inputStream.readUINT(1)
			inputStream.reset()
			return when (type) {
				0 -> Grib1GDSLatLon(inputStream)
				1 -> throw NotSupportedException("Mercator projection is not yet supported")
				else -> throw NotSupportedException("Unknown GDS type: ${type}")
			}
		}
	}

	protected var latitudeFirst = 0.0
	protected var latitudeLast = 0.0
	protected var longitudeFirst = 0.0
	protected var longitudeLast = 0.0
	protected var nDataPoints = 0

	/** Length in bytes of this section. */
	var length: Int
		protected set

	/** Type of grid (See table 6) */
	var gridType: Int = 0
		private set

	/** Number of grid columns. (Also Ni) */
	var gridNX = 0
		protected set

	/** Number of grid rows. (Also Nj) */
	var gridNY = 0
		protected set

	/** Latitude of grid start point. */
	var gridLat1 = 0.0
		protected set

	/** Longitude of grid start point. */
	var gridLng1 = 0.0
		protected set

	/** Mode of grid (See table 7) only 128 supported == increments given) */
	var gridMode = 0
		protected set

	/** Latitude of grid end point. Get y-coordinate/latitude of grid end point. */
	var gridLat2 = 0.0
		protected set

	/** Longitude of grid end point. Get x-coordinate/longitude of grid end point. */
	var gridLng2 = 0.0
		protected set

	/** x-distance between two grid points can be delta-Lon or delta x. */
	var gridDX = 0.0
		protected set

	/** y-distance of two grid points can be delta-Lat or delta y. */
	var gridDY = 0.0
		protected set

	/** Scanning mode (See table 8). Get scan mode (sign of increments). *Only 64, 128 and 192 supported so far. */
	var gridScanmode = 0
		protected set

	// rdg - the remaining coordinates are not common to all types, and as such
	//    should be removed.  They are left here (temporarily) for continuity.
	//    These should be implemented in a GribGDSxxxx child class.

	/** y-coordinate/latitude of south pole of a rotated lat/lon grid. */
	var gridLatSP = 0.0
		protected set

	/** x-coordinate/longitude of south pole of a rotated lat/lon grid. */
	var gridLngSP = 0.0
		protected set

	/** Rotation angle of rotated lat/lon grid. */
	var gridRotAngle = 0.0
		protected set

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

	constructor(inputStream: GribInputStream) {
		// [1-3] Length of section in octets
		length = inputStream.readUINT(4)

		// [4] NV -- number of vertical coordinate parameters */
		inputStream.skip(1)

		// [5] PV -- location (octet number) of the list of vertical coordinate parameters, if present; or
		//     PL -- location (octet number) of the list of numbers of points in each row (if no vertical coordinate parameters are present), if present; or
		//     255 (all bits set to 1) if neither are present
		inputStream.skip(1)

		// [6] Data representation type
		gridType = inputStream.readUINT(1)

		//[7-xx]
	}

	/**
	 * New constructor created for child classes, which has to be public!
	 *
	 * @param header integer array of header data (octets 1-6) read in GribGDSFactory exceptions are thrown in
	 *               children and passed up
	 */
	constructor(header: ByteArray) {

		// octets 1-3 (GDS section length)
		// this.length = Bytes2Number.uint3(header[0], header[1], header[2]);
		length = Bytes2Number.bytesToUint(header.copyOfRange(0, 3))

		// octet 4 (number of vertical coordinate parameters) and
		// octet 5 (octet location of vertical coordinate parameters
		// not implemented yet

		// octet 6 (grid type)
		gridType = Bytes2Number.bytesToUint(header[5])
	}

	// *** public methods **************************************************************
	// rdg - the basic getters can remain here, but other functionality should
	//    be moved to the child GribGDSxxxx classes.  For now, overriding these
	//    methods will work just fine.

	// peg - turned all common methods into abstract methods, so it will become
	//       easier to subclass with a new GDS type class, this way it's much
	//       more clear which methods is standard for all GDS types

	/**
	 * rdg - added this method to be used in a comparator for sorting while extracting records.
	 * Not currently used in the JGrib library, but is used in a library I'm using that uses JGrib.
	 * @param gds - GribRecordGDS
	 * @return - -1 if gds is "less than" this, 0 if equal, 1 if gds is "greater than" this.
	 *
	 * @see java.util.Comparator.compare
	 */
	abstract fun compare(gds: Grib1RecordGDS): Int
}