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

import mt.edu.um.cf2.jgribx.*
import mt.edu.um.cf2.jgribx.api.GribMessage
import java.io.IOException
import java.util.*
import kotlin.math.roundToInt

/**
 * A class representing a single GRIB message (and record). A GRIB message consists of five sections:
 *
 * - Indicator Section (IS),
 * - Product Definition Section (PDS),
 * - Grid Definition Section (GDS),
 * - Bit Map Section (BMS), and
 * - Binary Data Section (BDS).
 *
 * For GRIB1 a grib message contains exactly one GRIB records and therefore this class implements both
 *
 * @property productDefinitionSection The product definition section (PDS).
 * @property gridDefinitionSection The grid definition section (GDS).
 * @property bitmapSection The bitmap section (BMS).
 * @property binaryDataSection The binary data section (BDS).
 *
 * @author Benjamin Stark
 * @author Jan Kubovy [jan@kubovy.eu]
 */
class Grib1Message private constructor(indicatorSection: GribRecordIS,
									   private val productDefinitionSection: Grib1RecordPDS,
									   private var gridDefinitionSection: Grib1RecordGDS,
									   private var bitmapSection: Grib1RecordBMS?,
									   private var binaryDataSection: Grib1RecordBDS) :
		GribRecord(indicatorSection), GribMessage {

	companion object {
		/**
		 * Constructs a <tt>GribRecord</tt> object from a bit input stream.
		 *
		 * @param gribInputStream bit input stream with GRIB record content
		 * @param indicatorSection
		 * @return
		 *
		 * @throws IOException           if stream can not be opened etc.
		 * @throws NotSupportedException
		 * @throws NoValidGribException  if stream contains no valid GRIB file
		 */
		internal fun readFromStream(gribInputStream: GribInputStream,
									indicatorSection: GribRecordIS): Grib1Message {
			gribInputStream.resetBitCounter()
			val productDefinitionSection = Grib1RecordPDS.readFromStream(gribInputStream)
			if (gribInputStream.byteCounter != productDefinitionSection.length)
				throw NoValidGribException("Incorrect PDS length")

			val gridDefinitionSection = if (productDefinitionSection.gdsExists) {
				gribInputStream.resetBitCounter()
				Grib1RecordGDS.readFromStream(gribInputStream).also {
					if (gribInputStream.byteCounter != it.length) throw NoValidGribException("Incorrect GDS length")
				}
			} else throw NoValidGribException("GribRecord: No GDS included.")

			val bitmapSection: Grib1RecordBMS? = if (productDefinitionSection.bmsExists) {
				gribInputStream.resetBitCounter()
				Grib1RecordBMS.readFromStream(gribInputStream).also { // read Bitmap Section
					if (gribInputStream.byteCounter != it.length) throw NoValidGribException("Incorrect BMS length")
				}
			} else null

			gribInputStream.resetBitCounter()
			val binaryDataSection = Grib1RecordBDS.readFromStream(
					gribInputStream,
					productDefinitionSection,
					gridDefinitionSection,
					bitmapSection)
			if (gribInputStream.byteCounter != binaryDataSection.length)
				throw NoValidGribException("Incorrect BDS length")

			// number of values
			// rdg - added the check for a constant field - otherwise this fails
			if (!binaryDataSection.isConstant
					&& binaryDataSection.values.size != gridDefinitionSection.gridCols * gridDefinitionSection.gridRows) {
				Logger.error("Grid should contain" +
						" ${gridDefinitionSection.gridCols} * ${gridDefinitionSection.gridRows} =" +
						" ${gridDefinitionSection.gridCols * gridDefinitionSection.gridRows} values.")
				Logger.error("But BDS section delivers only ${binaryDataSection.values.size}.")
			}
			return Grib1Message(
					indicatorSection,
					productDefinitionSection,
					gridDefinitionSection,
					bitmapSection,
					binaryDataSection)
		}
	}

	override val records: List<GribRecord>
		get() = listOf(this)

	override val centreId: Int
		get() = productDefinitionSection.centre

	override val forecastTime: Calendar
		get() = productDefinitionSection.localForecastTime

	/** Get the byte recordLength of this GRIB record. */
	val length: Long
		get() = indicatorSection.recordLength

	override val parameterCode: String
		get() = productDefinitionSection.parameterAbbreviation

	override val parameterDescription: String
		get() = productDefinitionSection.parameterDescription

	override val processId: Int
		get() = productDefinitionSection.processId

	/** Get grid coordinates in longitude/latitude */
	val gridCoords: DoubleArray
		get() = gridDefinitionSection.gridCoords

	/** Get data/parameter values as an array of float. */
	val values: FloatArray
		get() {
			if (!binaryDataSection.isConstant) return binaryDataSection.values
			val gridSize: Int = gridDefinitionSection.gridCols * gridDefinitionSection.gridRows
			val values = FloatArray(gridSize)
			val ref: Float = binaryDataSection.referenceValue
			for (i in 0 until gridSize) {
				values[i] = ref
			}
			return values
		}

	/**
	 * Get the unit for the parameter.
	 *
	 * @return name of unit
	 */
	val unit: String
		get() = productDefinitionSection.parameterUnits

	override val levelCode: String
		get() = productDefinitionSection.level?.code ?: ""

	override val levelDescription: String
		get() = productDefinitionSection.level?.description ?: ""

	override val levelIdentifier: String
		get() = productDefinitionSection.level?.identifier ?: ""

	override val levelValues: FloatArray
		get() = productDefinitionSection.level?.values ?: floatArrayOf(Float.NaN, Float.NaN)

	override val referenceTime: Calendar
		get() = productDefinitionSection.referenceTime

	/**
	 * Get a single value from the BDS using i/x, j/y index.
	 *
	 * Retrieves using a row major indexing.
	 * @param i
	 * @param j
	 *
	 * @return  array of parameter values
	 * @throws NoValidGribException
	 */
	fun getValue(i: Int, j: Int): Float {
		if (i >= 0 && i < gridDefinitionSection.gridCols && j >= 0 && j < gridDefinitionSection.gridRows) {
			return binaryDataSection.getValue(gridDefinitionSection.gridCols * j + i)
		}
		throw NoValidGribException("GribRecord:  Array index out of bounds")
	}

	override fun getValue(latitude: Double, longitude: Double): Double {
		var value = Double.NaN
		val i = ((longitude - gridDefinitionSection.longitudeOfFirstGridPoint) / gridDefinitionSection.gridDeltaX).roundToInt()
		val j = ((latitude - gridDefinitionSection.latitudeOfFirstGridPoint) / gridDefinitionSection.gridDeltaY).roundToInt()
		try {
			value = if (gridDefinitionSection.scanningMode and 0x20 != 0x20) {
				// Adjacent points in i direction are consecutive
				binaryDataSection.getValue(gridDefinitionSection.gridCols * j + i).toDouble()
			} else {
				binaryDataSection.getValue(gridDefinitionSection.gridRows * i + j).toDouble()
			}
		} catch (e: NoValidGribException) {
			Logger.error("Cannot find a value for the given lat-long")
		}
		return value
	}

	override fun toString(): String = listOfNotNull(
			"GRIB1 record:",
			indicatorSection.toString().prependIndent("\t"),
			productDefinitionSection.toString().prependIndent("\t"),
			gridDefinitionSection.toString().prependIndent("\t"),
			bitmapSection?.toString()?.prependIndent("\t"),
			binaryDataSection.toString().prependIndent("\t"))
			.joinToString("\n")

	override fun writeTo(gribOutputStream: GribOutputStream) {
		indicatorSection.writeTo(gribOutputStream)
		productDefinitionSection.writeTo(gribOutputStream)
		gridDefinitionSection.writeTo(gribOutputStream)
		bitmapSection?.writeTo(gribOutputStream)
		binaryDataSection.writeTo(gribOutputStream)
		GribRecordES(true).writeTo(gribOutputStream)
	}
}
