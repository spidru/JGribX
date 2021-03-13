package mt.edu.um.cf2.jgribx.grib2

import mt.edu.um.cf2.jgribx.GribRecordIS
import mt.edu.um.cf2.jgribx.api.GribGridDefinitionSection
import mt.edu.um.cf2.jgribx.api.GribRecord
import java.util.*

/**
 * A GRIB2 Record containing a part of the GRIB2 message to enable repeated sections.
 *
 * @author AVLAB-USER3
 * @author Jan Kubovy [jan@kubovy.eu]
 */
class Grib2Record(override val indicatorSection: GribRecordIS,
				  private val identificationSection: Grib2RecordIDS,
				  private val productDefinitionSection: Grib2RecordPDS,
				  private val dataSection: Grib2RecordDS<*>) : GribRecord {

	override val gridDefinitionSection: GribGridDefinitionSection
		get() = dataSection.gds

	override val centreId: Int
		get() = identificationSection.centreId

	override val forecastTime: Calendar
		get() = productDefinitionSection.forecastTime

	override val levelCode: String
		get() = productDefinitionSection.levelCode

	override val levelDescription: String
		get() = productDefinitionSection.levelDescription

	override val levelIdentifier: String
		get() = productDefinitionSection.levelIdentifier

	override val levelValues: FloatArray
		get() = productDefinitionSection.let { floatArrayOf(it.level1.value, it.level2?.value ?: Float.NaN) }

	override val parameterCode: String
		get() = productDefinitionSection.parameterAbbrev

	override val parameterDescription: String
		get() = productDefinitionSection.parameterDescription

	override val processId: Int
		get() = productDefinitionSection.processId

	override val referenceTime: Calendar
		get() = identificationSection.referenceTime

	override val values: FloatArray
		get() = dataSection.values

	override fun getValue(sequence: Int): Float = dataSection.getValue(sequence)

	override fun getValue(latitude: Double, longitude: Double): Float = dataSection.getValue(latitude, longitude)

	override fun cutOut(north: Double, east: Double, south: Double, west: Double) = dataSection
			.cutOut(north, east, south, west)

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is Grib2Record) return false

		if (indicatorSection != other.indicatorSection) return false
		if (identificationSection != other.identificationSection) return false
		if (productDefinitionSection != other.productDefinitionSection) return false
		if (dataSection != other.dataSection) return false
		if (gridDefinitionSection != other.gridDefinitionSection) return false

		return true
	}

	override fun hashCode(): Int {
		var result = indicatorSection.hashCode()
		result = 31 * result + identificationSection.hashCode()
		result = 31 * result + productDefinitionSection.hashCode()
		result = 31 * result + dataSection.hashCode()
		result = 31 * result + gridDefinitionSection.hashCode()
		return result
	}
}
