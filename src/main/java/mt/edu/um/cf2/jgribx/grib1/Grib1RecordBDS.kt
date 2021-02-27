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
import java.util.*
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * ### [Section 4: Binary data section](https://apps.ecmwf.int/codes/grib/format/grib1/sections/4/)
 *
 *     | Octets | # | Key               | Type     | Content                                                        |
 *     |--------|---|-------------------|----------|----------------------------------------------------------------|
 *     | 1-3    | 3 | section4Length    | unsigned | Length of section                                              |
 *     | 4      |   | dataFlag          | codeflag | Flag (see Code table 11) (first 4 bits). Number of unused bits |
 *     |        |   |                   |          | at end of Section 4 (last 4 bits)                              |
 *     | 5-6    |   | binaryScaleFactor | signed   | Scale factor (E)                                               |
 *     | 7-10   |   | referenceValue    | real     | Reference value (minimum of packed values)                     |
 *     | 11     |   | bitsPerValue      | unsigned | Number of bits containing each packed value                    |
 *     | 12-nn  |   |                   |          | Variable, depending on the flag value in octet 4               |
 *
 * Constructs a `GribRecordBDS` object from a bit input stream.
 * A bit map indicates the grid points where no parameter value is defined.
 *
 * @param pds [Section 1: Product definition section](https://apps.ecmwf.int/codes/grib/format/grib1/sections/1/)
 * @param gds [Section 2: Grid description section](https://apps.ecmwf.int/codes/grib/format/grib1/sections/2/)
 * @param bms [Section 3: Bit-map section](https://apps.ecmwf.int/codes/grib/format/grib1/sections/3/)
 *
 * @param dataFlag          (`4`)     Flag (see [Code table 11](https://apps.ecmwf.int/codes/grib/format/grib1/flag/11/))
 *                                    (first 4 bits). Number of unused bits at end of Section 4 (last 4 bits)
 * @param binaryScaleFactor (`5-6`)   Scale factor (E)
 * @param referenceValue    (`7-10`)  Reference value (minimum of packed values)
 * @param bitsPerValue      (`11`)    Number of bits containing each packed value
 * @param data              (`12-nn`) Variable, depending on the flag value in octet 4
 *
 * @throws java.io.IOException If stream can not be opened etc.
 * @throws NotSupportedException If a required feature is not supported
 * @author Benjamin Stark
 * @author Jan Kubovy [jan@kubovy.eu]
 */
class Grib1RecordBDS(
		private val pds: Grib1RecordPDS,
		private val gds: Grib1RecordGDS,
		private val bms: Grib1RecordBMS?,

		private val dataFlag: Int,
		private val binaryScaleFactor: Int,
		internal val referenceValue: Float,
		private val bitsPerValue: Int,
		private val data: FloatArray) : Grib1Section {
	companion object {
		/** Constant value for an undefined grid value. */
		private const val UNDEFINED = 99999e20f

		internal fun readFromStream(gribInputStream: GribInputStream,
									pds: Grib1RecordPDS,
									gds: Grib1RecordGDS,
									bms: Grib1RecordBMS?): Grib1RecordBDS {
			// [1-3] Section Length
			val length = Grib1Section.readFromStream(gribInputStream)

			// Extract required information from PDS
			val decimalScale = pds.decimalScale

			// [4], 1st half (packing flag)
			val dataFlag = gribInputStream.readUINT(1)
			if (dataFlag and 240 != 0) {
				throw NotSupportedException("GribRecordBDS: No other flag " +
						"(octet 4, 1st half) than 0 (= simple packed floats as " +
						"grid point data) supported yet in BDS section.")
			}

			// [4], 2nd half (number of unused bits at end of this section)
			val unusedBits = dataFlag and 15

			// [5-6] (binary scale factor)
			val binaryScaleFactor = gribInputStream.readINT(2, Bytes2Number.INT_SM)

			// [7-10] (reference point = minimum value)
			val referenceValue = gribInputStream.readFloat(4, Bytes2Number.FLOAT_IBM)

			// [11] (number of bits per value)
			val bitsPerValue = gribInputStream.readUINT(1)
			val isConstant = bitsPerValue == 0

			// *** read values ************************************************************
			val ref = (10.0.pow(-decimalScale.toDouble()) * referenceValue).toFloat()
			val scale = (10.0.pow(-decimalScale.toDouble()) * 2.0.pow(binaryScaleFactor.toDouble())).toFloat()
			val data: FloatArray
			if (bms != null) {
				val bitmap: BooleanArray = bms.bitmap
				data = FloatArray(bitmap.size)
				bitmap.indices.forEach { i ->
					if (bitmap[i]) {
						if (!isConstant) {
							data[i] = ref + scale * gribInputStream.readUBits(bitsPerValue)
						} else { // rdg - added this to handle a constant valued parameter
							data[i] = ref
						}
					} else data[i] = UNDEFINED
				}
			} else {
				if (!isConstant) {
					data = FloatArray(((length - 11) * 8 - unusedBits) / bitsPerValue)
					data.indices.forEach { i ->
						val byte = gribInputStream.readUBits(bitsPerValue)
						data[i] = ref + scale * byte
					}
				} else {
					// constant valued - same min and max
					data = FloatArray(gds.gridCols * gds.gridRows)
					Arrays.fill(data, ref)
				}
			}
			gribInputStream.seekNextByte()
			gribInputStream.skip((unusedBits / 8).toLong())

			// rdg - added the check for a constant field - otherwise this fails
			if (!isConstant && data.size != gds.gridCols * gds.gridRows) {
				Logger.error("Grid should contain" +
						" ${gds.gridCols} * ${gds.gridRows} = ${gds.gridCols * gds.gridRows} values." +
						" But BDS section delivers only ${data.size}.")
			}

			return Grib1RecordBDS(pds, gds, bms, dataFlag, binaryScaleFactor, referenceValue, bitsPerValue, data)
					.takeIf { it.length == length }
					?: throw NoValidGribException("BDS length mismatch")
		}
	}

	/** Length in bytes of this BDS. */
	override val length: Int
		get() = 11 + ceil(data.size * bitsPerValue / 8.0).toInt() + (dataFlag and 15) / 8

	/**
	 * rdg - added this to prevent a divide by zero error if variable data empty
	 *
	 * Indicates whether the BMS is represented by a single value
	 * -  Octet 12 is empty, and the data is represented by the reference value.
	 */
	internal val isConstant: Boolean
		get() = bitsPerValue == 0


	private val minValue: Float
		get() = data.minOrNull() ?: -Float.MAX_VALUE

	private val maxValue: Float
		get() = data.maxOrNull() ?: Float.MAX_VALUE

	/**
	 * Data values ordered with the defined [grid][mt.edu.um.cf2.jgribx.grib2.Grib2RecordDS.gds].
	 *
	 * Since this iterates over data and instantiates an new array on every call it should only be use when one
	 * needs the while data for sequential access. For random access use one of the
	 * [mt.edu.um.cf2.jgribx.grib2.Grib2RecordDS.getValue] methods.
	 */
	internal val values: FloatArray
		get() = gds.dataIndices.map { data[it] }.toList().toFloatArray()

	internal fun getValue(sequence: Int) = gds.getDataIndex(sequence).let { data[it] }

	internal fun getValue(latitude: Double, longitude: Double): Float = gds.getDataIndex(latitude, longitude).let { data[it] }

	override fun writeTo(outputStream: GribOutputStream) {
		super.writeTo(outputStream)  // [1-3] length
		outputStream.writeUInt(dataFlag, bytes = 1) // [4]
		outputStream.writeSMInt(binaryScaleFactor, bytes = 2) // [5-6]
		outputStream.writeFloatIBM(referenceValue) // [7-10]
		outputStream.writeUInt(bitsPerValue, bytes = 1) // [11]

		val decimalScale = pds.decimalScale
		val ref = (10.0.pow(-decimalScale.toDouble()) * referenceValue)
		val scale = (10.0.pow(-decimalScale.toDouble()) * 2.0.pow(binaryScaleFactor.toDouble()))
		data.forEach { value ->
			val byte = ((value.toDouble() - ref) / scale).roundToInt()
			outputStream.writeUBits(byte, numBits = bitsPerValue)  // [12-nn]
		}
		outputStream.fillByte()
		val unusedBits = dataFlag and 15
		outputStream.write(ByteArray((unusedBits / 8)))
	}

	override fun equals(other: Any?) = this === other
			|| other is Grib1RecordBDS
			&& length == other.length
			&& dataFlag == other.dataFlag
			&& binaryScaleFactor == other.binaryScaleFactor
			&& referenceValue == other.referenceValue
			&& bitsPerValue == other.bitsPerValue
			&& data
			.mapIndexed { i, value -> value to other.data.getOrNull(i) }
			.all { (a, b) -> b != null && abs(a - b) < FLOAT_PRECISION }

	override fun hashCode(): Int {
		var result = length
		result = 31 * result + dataFlag
		result = 31 * result + binaryScaleFactor
		result = 31 * result + referenceValue.hashCode()
		result = 31 * result + bitsPerValue
		result = 31 * result + data.contentHashCode()
		return result
	}

	override fun toString(): String = listOfNotNull(
			"GRIB1 BDS section:",
			"\tMin/max value: ${minValue} ${maxValue}",
			"\tReference value: ${referenceValue}",
			"\tIs a constant: ${isConstant}",
			"\tBinary scale: ${binaryScaleFactor}",
			"\tNumber of bits: ${bitsPerValue}",
			"\tData: ${data.joinToString(", ")}")
			.joinToString("\n")
}
