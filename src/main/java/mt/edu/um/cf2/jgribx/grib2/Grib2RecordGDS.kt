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
import mt.edu.um.cf2.jgribx.api.GribGridDefinitionSectionInternal

/**
 * ### [Section 3: Grid Definition Section](https://www.nco.ncep.noaa.gov/pmb/docs/grib2/grib2_doc/grib2_sect3.shtml)
 *
 *    | Octet     | # | Value                                                                                   |
 *    |-----------|---|-----------------------------------------------------------------------------------------|
 *    | 1-4       | 4 | Length of the section in octets (nn)                                                    |
 *    | 5         | 1 | Number of the section (3)                                                               |
 *    | 6         | 1 | Source of grid definition (see Table 3.0) (See note 1 below)                            |
 *    | 7-10      | 4 | Number of data points                                                                   |
 *    | 11        | 1 | Number of octets for optional list of numbers defining number of points (see note 2)    |
 *    | 12        | 1 | Interpetation of list of numbers defining number of points (see Table 3.11)             |
 *    | 13-14     | 2 | Grid definition template number (=N) (see Table 3.1)                                    |
 *    | 15-xx     |   | Grid definition template (See Template 3.N, where N is the grid definition template     |
 *    |           |   | number given in octets 13-14)                                                           |
 *    | [xx+1]-nn |   | Optional list of numbers defining number of points (see notes 2, 3, and 4 below)        |
 *
 * @param gridDefinitionSource (`6`) Source of grid definition (see
 *                             [Table 3.0](https://www.nco.ncep.noaa.gov/pmb/docs/grib2/grib2_doc/grib2_table3-0.shtml))
 *                             If octet `6` is not zero, octets `15-xx` (`15-nn` if octet `11` is zero) may not be
 *                             supplied. This should be documented with all bits set to `1` in the grid definition
 *                             template number.
 * @param numberOfDataPoints   (`7-10`) Number of data points
 * @param nBytes               (`11`) Number of octets for optional list of numbers defining number of points (see note 2)
 * @param interpretation       (`12`) Interpetation of list of numbers defining number of points
 *                             (see [Table 3.11](https://www.nco.ncep.noaa.gov/pmb/docs/grib2/grib2_doc/grib2_table3-11.shtml))
 * @author Jan Kubovy [jan@kubovy.eu]
 */
abstract class Grib2RecordGDS protected constructor(protected var gridDefinitionSource: Int,
													var numberOfDataPoints: Int,
													protected var nBytes: Int,
													protected var interpretation: Int) : Grib2Section, GribGridDefinitionSectionInternal {

	data class ScanMode internal constructor(internal val flags: Byte) {
		internal var iDirectionPositive: Boolean = flags.toInt() and 0x80 != 0x80
		internal var jDirectionPositive: Boolean = flags.toInt() and 0x40 == 0x40
		var iDirectionConsecutive: Boolean = flags.toInt() and 0x20 != 0x20
		var rowsZigzag: Boolean = flags.toInt() and 0x10 == 0x10
		var iDirectionEvenRowsOffset: Boolean = flags.toInt() and 0x08 == 0x08
		var iDirectionOddRowsOffset: Boolean = flags.toInt() and 0x04 == 0x04
		var jDirectionOffset: Boolean = flags.toInt() and 0x02 == 0x02
		var rowsNiNjPoints: Boolean = flags.toInt() and 0x01 != 0x01
	}

	companion object {
		internal fun readFromStream(gribInputStream: GribInputStream): Grib2RecordGDS {
			/* [1-5] Length, section number */
			val length = Grib2Section.readFromStream(gribInputStream, 3)

			/* [6] Grid Definition Source */
			val gridDefinitionSource = gribInputStream.readUINT(1)
			if (gridDefinitionSource != 0) Logger.error("Unsupported grid definition source")

			/* [7-10] Number of Data Points */
			val numberOfDataPoints = gribInputStream.readUINT(4)

			/* [11] Number of Octets (for optional list of numbers defining number of points) */
			val nBytes = gribInputStream.readUINT(1)

			/* [12] Interpretation */
			val interpretation = gribInputStream.readUINT(1)

			/* [13-14] Grid Definition Template Number */
			val gridType = gribInputStream.readUINT(2)
			return when (gridType) {
				// Latitude/Longitude (also called Equidistant Cylindrical or Plate Caree)
				0 -> Grib2RecordGDSLatLon.readFromStream(gribInputStream, length, gridDefinitionSource,
						numberOfDataPoints, nBytes, interpretation)
				else -> throw NotSupportedException("Unsupported grid type: ${gridType}")
			}.takeIf { it.length == length } ?: throw NoValidGribException("GDS length mismatch")
		}
	}

	internal var localUseSection: Grib2RecordLUS? = null
	internal val productDefinitionSections = mutableListOf<Grib2RecordPDS>()

	override val number: Int = 3

	/** Grid definition template number (`=N`)
	 *  (see [Table 3.1](https://www.nco.ncep.noaa.gov/pmb/docs/grib2/grib2_doc/grib2_table3-1.shtml) */
	protected abstract val gridType: Int

	abstract val gridDeltaX: Double
	abstract val gridDeltaY: Double
	abstract val gridLatStart: Double
	abstract val gridLonStart: Double
	protected abstract val gridSizeX: Int
	protected abstract val gridSizeY: Int

	/** @see mt.edu.um.cf2.jgribx.api.GribRecord.cutOut */
	internal abstract fun cutOut(north: Double, east: Double, south: Double, west: Double)

	override fun writeTo(outputStream: GribOutputStream) {
		super.writeTo(outputStream)  // [1-5] length, section number
		outputStream.writeUInt(gridDefinitionSource, bytes = 1) // [6]
		outputStream.writeUInt(numberOfDataPoints, bytes = 4) // [7-10]
		outputStream.writeUInt(nBytes, bytes = 1) // [11]
		outputStream.writeUInt(interpretation, bytes = 1) // [12]
		outputStream.writeUInt(gridType, bytes = 2) // [13-14]
	}

	override fun equals(other: Any?) = this === other
			|| other is Grib2RecordGDS
			&& length == other.length
			&& number == other.number
			&& gridDefinitionSource == other.gridDefinitionSource
			&& numberOfDataPoints == other.numberOfDataPoints
			&& nBytes == other.nBytes
			&& interpretation == other.interpretation
			&& gridType == other.gridType

	override fun hashCode(): Int {
		var result = length
		result = 31 * result + number
		result = 31 * result + gridDefinitionSource
		result = 31 * result + numberOfDataPoints
		result = 31 * result + nBytes
		result = 31 * result + interpretation
		result = 31 * result + gridType
		return result
	}
}
