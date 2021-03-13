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
import mt.edu.um.cf2.jgribx.api.GribRecord
import java.io.IOException
import java.util.*

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
class Grib1Message private constructor(override val indicatorSection: GribRecordIS,
									   private val productDefinitionSection: Grib1RecordPDS,
									   override var gridDefinitionSection: Grib1RecordGDS,
									   private var bitmapSection: Grib1RecordBMS?,
									   private var binaryDataSection: Grib1RecordBDS) :
		GribRecord, GribMessage {

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
		get() = indicatorSection.messageLength

	override val parameterCode: String
		get() = productDefinitionSection.parameterAbbreviation

	override val parameterDescription: String
		get() = productDefinitionSection.parameterDescription

	override val processId: Int
		get() = productDefinitionSection.processId

	/** Get grid coordinates in longitude/latitude */
	val gridCoords: Array<DoubleArray>
		get() = gridDefinitionSection.coords

	/** Get data/parameter values as an array of float. */
	override val values: FloatArray
		get() {
			if (!binaryDataSection.isConstant) return binaryDataSection.values
			val gridSize: Int = gridDefinitionSection.cols * gridDefinitionSection.rows
			val values = FloatArray(gridSize)
			val ref: Float = binaryDataSection.referenceValue
			for (i in 0 until gridSize) values[i] = ref
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

	override fun getValue(sequence: Int): Float = binaryDataSection.getValue(sequence)

	override fun getValue(latitude: Double, longitude: Double): Float = binaryDataSection.getValue(latitude, longitude)

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

	override fun cutOut(north: Double, east: Double, south: Double, west: Double) =
			TODO("Not yet implemented")
}
