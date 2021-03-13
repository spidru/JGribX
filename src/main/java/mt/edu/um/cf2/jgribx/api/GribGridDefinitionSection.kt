package mt.edu.um.cf2.jgribx.api

/**
 * GRIB Grid unified public API
 *
 * @author Jan Kubovy [jan@kubovy.eu]
 */
interface GribGridDefinitionSection : GribSection {
	/**
	 * Get grid coordinates in longitude/latitude pairs Longitude is returned in the range +/- 180 degrees
	 *
	 * @see mt.edu.um.cf2.jgribx.grib1.Grib1RecordGDS.coords
	 * @see mt.edu.um.cf2.jgribx.grib2.Grib2RecordGDS.coords
	 * @return Longitide/latituide as 2-dimensional array
	 */
	val coords: Array<DoubleArray>

	/** All longitude coordinates */
	val xCoords: DoubleArray

	/** All latitude coordinates */
	val yCoords: DoubleArray

	/** Number of grid columns. (Also Ni) */
	val cols: Int

	/** Number of grid rows. (Also Nj) */
	val rows: Int

	/** x-distance between two grid points can be delta-Lon or delta x. */
	val deltaX: Double

	/** y-distance of two grid points can be delta-Lat or delta y. */
	val deltaY: Double
}
