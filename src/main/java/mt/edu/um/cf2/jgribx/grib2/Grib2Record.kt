package mt.edu.um.cf2.jgribx.grib2

import mt.edu.um.cf2.jgribx.GribRecord
import mt.edu.um.cf2.jgribx.GribRecordIS
import mt.edu.um.cf2.jgribx.Logger
import java.util.*
import kotlin.math.roundToInt

/**
 * A GRIB2 Record containing a part of the GRIB2 message to enable repeated sections.
 *
 * @author AVLAB-USER3
 * @author Jan Kubovy [jan@kubovy.eu]
 */
class Grib2Record(indicatorSection: GribRecordIS,
				  private var identificationSection: Grib2RecordIDS,
				  private var productDefinitionSection: Grib2RecordPDS,
				  private var dataSection: Grib2RecordDS<*>) : GribRecord(indicatorSection) {

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

	override fun getValue(latitude: Double, longitude: Double): Double {
		val gds = dataSection.gds
		if (gds !is Grib2RecordGDSLatLon) throw NotImplementedError("${gds::class.simpleName} not implemented")
		// double[] xcoords = gds.getGridXCoords();
		// double[] ycoords = gds.getGridYCoords();
		val j = ((latitude - gds.gridLatStart) / gds.gridDeltaY).roundToInt() // j = index_closest_latitude
		val i = ((longitude - gds.gridLonStart) / gds.gridDeltaX).roundToInt() // i = index_closest_longitude

		// double closest_latitude = ycoords[index_closest_latitude];
		// double closest_longitude = xcoords[index_closest_longitude];
		val scanMode = gds.scanMode
		if (scanMode.iDirectionEvenRowsOffset
				|| scanMode.iDirectionOddRowsOffset
				|| scanMode.jDirectionOffset
				|| !scanMode.rowsNiNjPoints
				|| scanMode.rowsZigzag) {
			Logger.error("Unsupported scan mode ${scanMode} found")
		}
		return if (scanMode.iDirectionConsecutive)
			dataSection.data[gds.gridNi * j + i].toDouble() else
			dataSection.data[gds.gridNj * i + j].toDouble()
	}
}
