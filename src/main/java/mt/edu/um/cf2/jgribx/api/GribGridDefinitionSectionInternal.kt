package mt.edu.um.cf2.jgribx.api

/**
 * This interface extends [GribGridDefinitionSection] and ads API for internal use only.
 *
 * @author Jan Kubovy [jan@kubovy.eu]
 */
internal interface GribGridDefinitionSectionInternal : GribGridDefinitionSection {
	/** Counterpart to [GribGridDefinitionSection.coords] */
	val dataIndices: Sequence<Int>

	/**
	 * Given a sequence number of a [coordinates][GribGridDefinitionSection.coords] from the grid (or its index) this will return
	 * the internal index of data from the data section.
	 *
	 * @param sequence sequence number of a [coordinates][GribGridDefinitionSection.coords] from the grid (or its index)
	 * @return The internal index of data from the data section
	 */
	fun getDataIndex(sequence: Int): Int

	/**
	 * Given a location this will return the internal index of data from data section.
	 *
	 * @param latitude Latitude
	 * @param longitude Longitude
	 * @return The internal index of data from the data section
	 */
	fun getDataIndex(latitude: Double, longitude: Double): Int
}
