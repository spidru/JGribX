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

import mt.edu.um.cf2.jgribx.GribInputStream
import mt.edu.um.cf2.jgribx.GribOutputStream
import mt.edu.um.cf2.jgribx.Logger
import mt.edu.um.cf2.jgribx.NoValidGribException
import mt.edu.um.cf2.jgribx.api.GribRecord
import kotlin.math.roundToInt

/**
 * ### [Section 7: Data Section](https://www.nco.ncep.noaa.gov/pmb/docs/grib2/grib2_doc/grib2_sect7.shtml)
 *
 *    | Octet | # | Value                                                                                         |
 *    |-------|---|-----------------------------------------------------------------------------------------------|
 *    | 1-4   | 4 | Length of section in octets: nn                                                               |
 *    | 5     | 1 | Number of Section: 7                                                                          |
 *    | 6-nn  |   | Data in a format described by data Template 7.X, where X is the data  representation template |
 *    |       |   | number given in octets 10-11 of Section 5.                                                    |
 *
 * See [Table 7.0 Data Template Definition used in section 7](https://www.nco.ncep.noaa.gov/pmb/docs/grib2/grib2_doc/grib2_table7-0.shtml)
 *
 * @param gds Reference to [Section 3: Grid Definition Section](https://www.nco.ncep.noaa.gov/pmb/docs/grib2/grib2_doc/grib2_sect3.shtml)
 * @param drs Reference to [Section 5: Data Representation Section](https://www.nco.ncep.noaa.gov/pmb/docs/grib2/grib2_doc/grib2_sect5.shtml)
 * @param bms Reference to [Section 6: Bit Map Section](https://www.nco.ncep.noaa.gov/pmb/docs/grib2/grib2_doc/grib2_sect6.shtml)
 * @param data Decoded data
 *
 * @author Jan Kubovy [jan@kubovy.eu]
 */
abstract class Grib2RecordDS<DRS : Grib2RecordDRS> internal constructor(internal val gds: Grib2RecordGDS,
																		internal val drs: DRS,
																		bms: Grib2RecordBMS,
																		data: FloatArray) : Grib2Section {
	companion object {
		internal fun <DRS : Grib2RecordDRS> readFromStream(gribInputStream: GribInputStream,
														   gds: Grib2RecordGDS,
														   drs: DRS,
														   bms: Grib2RecordBMS): Grib2RecordDS<*> {
			/* [1-5] Length, section number */
			val length = Grib2Section.readFromStream(gribInputStream, 7)

			return when (drs) {
				// Order is relevant since, e.g., DRS3 inherits DRS2 and DRS2 inherits DRS0!
				is Grib2RecordDRS3 -> Grib2RecordDS3.readFromStream(gribInputStream, gds, drs, bms)
						.also { it.length = length } // TODO Calculate length
				is Grib2RecordDRS2 -> Grib2RecordDS2.readFromStream(gribInputStream, gds, drs, bms)
						.also { it.length = length } // TODO Calculate length
				is Grib2RecordDRS0 -> Grib2RecordDS0.readFromStream(gribInputStream, gds, drs, bms)
				else -> TODO("Grib2RecordDS${drs::class.simpleName?.get(0)} not implemented!")
			}.takeIf { it.length == length } ?: throw NoValidGribException("DS length mismatch")
		}
	}

	override val number: Int = 7

	internal var bms: Grib2RecordBMS = bms
		private set

	internal var data: FloatArray
		private set

	/**
	 * Data values ordered with the defined [grid][Grib2RecordDS.gds].
	 *
	 * Since this iterates over data and instantiates an new array on every call it should only be use when one
	 * needs the while data for sequential access. For random access use one of the [Grib2RecordDS.getValue] methods.
	 */
	internal val values: FloatArray
		get() = gds.dataIndices.map { data[it] }.toList().toFloatArray()

	init {
		this.data = applyBitmapTo(data)
	}

	internal fun getValue(sequence: Int) = gds.getDataIndex(sequence).let { data[it] }

	internal fun getValue(latitude: Double, longitude: Double): Float = gds.getDataIndex(latitude, longitude).let { data[it] }

	/** @see GribRecord.cutOut */
	internal fun cutOut(north: Double, east: Double, south: Double, west: Double) {
		val gds = gds
		if (gds !is Grib2RecordGDSLatLon) throw TODO("${gds::class.simpleName} not implemented")

		val closestSouth = gds.closestLatSmallerThan(south)
		val closestWest = gds.closestLngSmallerThan(west)
		val closestNorth = gds.closestLatLargerThan(north)
		val closestEast = gds.closestLngLargerThan(east)

		val jMin = ((closestSouth - gds.latStart) / gds.deltaY).roundToInt() // j = index_closest_latitude
		val iMin = ((closestWest - gds.lngStart) / gds.deltaX).roundToInt() // i = index_closest_longitude
		val jMax = jMin + gds.jPointsCount(closestSouth, closestNorth)
		val iMax = iMin + gds.iPointsCount(closestWest, closestEast)

		this.data = (jMin until jMax).flatMap { j -> (iMin until iMax).map { j to it } }
				.map { (j, i) -> if (gds.scanMode.iDirectionConsecutive) gds.gridNi * j + i else gds.gridNj * i + j }
				.map { data[it] }
				.toFloatArray()
		gds.cutOut(north, east, south, west)
	}

	override fun writeTo(outputStream: GribOutputStream) {
		Logger.debug("Writing GRIB2 Data Section (DS), template: ${drs.templateNumber} - ${length} bytes")
		super.writeTo(outputStream)
	}

	override fun equals(other: Any?) = this === other
			|| other is Grib2RecordDS<*>
			&& length == other.length
			&& number == other.number
			&& data.contentEquals(other.data)

	override fun hashCode(): Int {
		var result = length
		result = 31 * result + number
		result = 31 * result + data.contentHashCode()
		return result
	}

	/** Applies bit-map to raw read data and sets missing data as [Float.NaN]. */
	private fun applyBitmapTo(data: FloatArray): FloatArray = when (bms.indicator) {
		Grib2RecordBMS.Indicator.BITMAP_SPECIFIED,
		Grib2RecordBMS.Indicator.BITMAP_PREDEFINED -> { // Predefined BM gets bitmap from previously loaded one
			var d = 0
			bms.bitmap.asSequence().map { bit -> if (bit) data[d++] else Float.NaN }.toList().toFloatArray()
		}
		else -> data
	}

	/** Filters data based on bit-map for writing */
	protected fun filterBitmapFrom(data: FloatArray): FloatArray = when (bms.indicator) {
		Grib2RecordBMS.Indicator.BITMAP_SPECIFIED,
		Grib2RecordBMS.Indicator.BITMAP_PREDEFINED -> {
			bms.bitmap.mapIndexed { i, b -> if (b) data[i] else null }.filterNotNull().toFloatArray()
		}
		else -> data
	}

	/** Recalculates bitmap */
	fun calculateBitmap() {
		bms = Grib2RecordBMS(0, data.map { it.isFinite() }.toBooleanArray())
	}
}
