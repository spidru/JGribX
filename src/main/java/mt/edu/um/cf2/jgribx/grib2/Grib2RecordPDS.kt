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
import mt.edu.um.cf2.jgribx.NoValidGribException
import mt.edu.um.cf2.jgribx.NotSupportedException
import java.util.*

/**
 * ### [Section 4: Product Definition Section](https://www.nco.ncep.noaa.gov/pmb/docs/grib2/grib2_doc/grib2_sect4.shtml)
 *
 *    | Octet     | # | Value                                                                                   |
 *    |-----------|---|-----------------------------------------------------------------------------------------|
 *    | 1-4       | 4 | Length of the section in octets (nn)                                                    |
 *    | 5         | 1 | Number of the section (4)                                                               |
 *    | 6-7       | 2 | Number of coordinate values after template (see note 1 below)                           |
 *    | 8-9       | 2 | Product definition template number (See Table 4.0)                                      |
 *    | 10-xx     |   | Product definition template (See product template 4.X, where X is the number given in   |
 *    |           |   | octets 8-9)                                                                             |
 *    | [xx+1]-nn |   | Optional list of coordinate values (see notes 2 and 3 below)                            |
 *
 * @param numberOfCoordinates (`6-7`) Number of coordinate values after template (see note 1 below)
 * @param parameter           Parameter
 * @param processId           Process ID
 *
 * @throws java.io.IOException if stream can not be opened etc.
 * @throws NotSupportedException
 */
abstract class Grib2RecordPDS protected constructor(val numberOfCoordinates: Int,
													val parameter: Grib2Parameter,
													internal val processId: Int) : Grib2Section {

	companion object {
		internal fun readFromStream(gribInputStream: GribInputStream,
									discipline: ProductDiscipline,
									referenceTime: Calendar): Grib2RecordPDS {
			/* [1-5] Length, section number */
			val length = Grib2Section.readFromStream(gribInputStream, 4)

			/* [6-7] Number of coordinate values after template */
			val numberOfCoordinates = gribInputStream.readUINT(2)
			if (numberOfCoordinates > 0) throw NotSupportedException("Hybrid coordinate values are not yet supported")

			/* [8-9] Template number */
			val templateId = gribInputStream.readUINT(2)
			return when (templateId) {
				0 -> Grib2RecordPDS0.readFromStream(gribInputStream, discipline, referenceTime, numberOfCoordinates)
				else -> {
					gribInputStream.skip(length - 9L)
					throw NotSupportedException("Unsupported template number: ${templateId}")
				}
			}.takeIf { it.length == length } ?: throw NoValidGribException("PDS length mismatch")
		}
	}

	override val number: Int = 4

	/** (`8-9`) Product definition template number (see
	 *  [Table 4.0](https://www.nco.ncep.noaa.gov/pmb/docs/grib2/grib2_doc/grib2_table4-0.shtml)) */
	protected abstract val templateId: Int

	abstract val forecastTime: Calendar
	abstract val level1: Grib2Level
	abstract val level2: Grib2Level?

	internal val dataSections = mutableListOf<Grib2RecordDS<*>>()

	/** The abbreviation representing the parameter. */
	val parameterAbbrev: String
		get() = parameter.code

	/** Description of the parameter. */
	val parameterDescription: String
		get() = parameter.description

	val parameterUnits: String
		get() = parameter.units

	val levelCode: String
		get() = level1.code

	val levelDescription: String
		get() = level1.description

	val levelIdentifier: String
		get() = level1.levelIdentifier

	override fun writeTo(outputStream: GribOutputStream) {
		super.writeTo(outputStream) // [1-5] length, section number
		outputStream.writeUInt(numberOfCoordinates, bytes = 2)
		outputStream.writeUInt(templateId, bytes = 2)
	}

	override fun equals(other: Any?) = this === other
			|| other is Grib2RecordPDS
			&& length == other.length
			&& number == other.number
			&& numberOfCoordinates == other.numberOfCoordinates
			&& templateId == other.templateId
			&& parameter == other.parameter
			&& processId == other.processId
			&& forecastTime == other.forecastTime
			&& level1 == other.level1
			&& level2 == other.level2

	override fun hashCode(): Int {
		var result = length
		result = 31 * result + number
		result = 31 * result + numberOfCoordinates
		result = 31 * result + templateId
		result = 31 * result + parameter.hashCode()
		result = 31 * result + processId
		result = 31 * result + forecastTime.hashCode()
		result = 31 * result + level1.hashCode()
		result = 31 * result + (level2?.hashCode() ?: 0)
		return result
	}

	override fun toString() = "Grib2RecordPDS(numberOfCoordinates=${numberOfCoordinates}, parameter=${parameter}," +
			" processId=${processId}, number=${number}, templateId=${templateId}, forecastTime=${forecastTime}," +
			" level1=${level1}, level2=${level2})"
}
