package mt.edu.um.cf2.jgribx.api

/**
 * GRIB Grid unified public API
 *
 * @author Jan Kubovy [jan@kubovy.eu]
 */
interface GribGridDefinitionSection {
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
}
