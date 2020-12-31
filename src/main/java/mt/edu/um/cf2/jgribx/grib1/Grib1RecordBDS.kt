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

import mt.edu.um.cf2.jgribx.Bytes2Number
import mt.edu.um.cf2.jgribx.GribInputStream
import mt.edu.um.cf2.jgribx.NoValidGribException
import mt.edu.um.cf2.jgribx.NotSupportedException
import java.util.*
import kotlin.math.pow

/**
 * A class representing the binary data section (BDS) of a GRIB record.
 *
 * Constructs a <tt>GribRecordBDS</tt> object from a bit input stream.
 * A bit map indicates the grid points where no parameter value is defined.
 *
 * @param gribInputStream Bit input stream containing BDS content
 * @param bms Bit Map Section of GRIB record
 * @param gds Grid Definition Section of record
 * @param pds Product Definition Section of record
 * @throws java.io.IOException If stream can not be opened etc.
 * @throws NotSupportedException If a required feature is not supported
 * @author  Benjamin Stark
 * @version 1.0
 */
class Grib1RecordBDS(gribInputStream: GribInputStream, bms: Grib1RecordBMS?, gds: Grib1RecordGDS, pds: Grib1RecordPDS) {
	companion object {
		/** Constant value for an undefined grid value. */
		const val UNDEFINED = 99999e20f
	}

	/** Length in bytes of this BDS. */
	var length: Int

	/** Binary scale factor. */
	var binaryScale: Int
		protected set

	/** Reference value, the base for all parameter values. */
	var referenceValue: Float
		protected set

	/** Number of bits per value. */
	var numBits: Int
		protected set

	/** Array of parameter values. */
	var values: FloatArray
		protected set

	/** Minimal parameter value in grid. */
	var minValue = Float.MAX_VALUE
		protected set

	/** Maximal parameter value in grid. */
	var maxValue = -Float.MAX_VALUE
		protected set

	/**
	 * rdg - added this to prevent a divide by zero error if variable data empty
	 *
	 * Indicates whether the BMS is represented by a single value
	 * -  Octet 12 is empty, and the data is represented by the reference value.
	 */
	var isConstant = false
		protected set

	/**
	 * Constructs a <tt>GribRecordBDS</tt> object from a bit input stream.
	 * A bit map which indicates grid points where no parameter value is
	 * defined is not available.
	 *
	 * @param gribInputStream - bit input stream with BDS content
	 * @param gds Grid Definition Section of record
	 * @param pds Product Definition Section of record
	 *
	 * @throws java.io.IOException If stream can not be opened etc.
	 * @throws NotSupportedException If a required feature is not supported
	 */
	constructor(gribInputStream: GribInputStream, gds: Grib1RecordGDS, pds: Grib1RecordPDS) :
			this(gribInputStream, null, gds, pds)

	init {
		val octets = ByteArray(11)
		val unusedBits: Int

		// Extract required information from PDS
		val decimalScale = pds.decimalScale
		gribInputStream.read(octets)

		// octets 1-3 (section length)
		length = Bytes2Number.bytesToUint(octets.copyOfRange(0, 3))

		// octet 4, 1st half (packing flag)
		val flag = octets[3].toInt()
		if (flag and 240 != 0) {
			throw NotSupportedException("GribRecordBDS: No other flag " +
					"(octet 4, 1st half) than 0 (= simple packed floats as " +
					"grid point data) supported yet in BDS section.")
		}

		// octet 4, 2nd half (number of unused bits at end of this section)
		unusedBits = flag and 15

		// octets 5-6 (binary scale factor)
		binaryScale = Bytes2Number.bytesToInt(octets.copyOfRange(4, 6), Bytes2Number.INT_SM)

		// octets 7-10 (reference point = minimum value)
		referenceValue = Bytes2Number.bytesToFloat(octets.copyOfRange(6, 10), Bytes2Number.FLOAT_IBM)

		// octet 11 (number of bits per value)
		numBits = Bytes2Number.bytesToUint(octets[10])
		isConstant = numBits == 0

		// *** read values ************************************************************
		val ref = (10.0.pow(-decimalScale.toDouble()) * referenceValue).toFloat()
		val scale = (10.0.pow(-decimalScale.toDouble()) * 2.0.pow(binaryScale.toDouble())).toFloat()
		if (bms != null) {
			val bitmap: BooleanArray = bms.bitmap
			values = FloatArray(bitmap.size)
			for (i in bitmap.indices) {
				if (bitmap[i]) {
					if (!isConstant) {
						values[i] = ref + scale * gribInputStream.readUBits(numBits)
						if (values[i] > maxValue) maxValue = values[i]
						if (values[i] < minValue) minValue = values[i]
					} else { // rdg - added this to handle a constant valued parameter
						values[i] = ref
					}
				} else values[i] = UNDEFINED
			}
		} else {
			if (!isConstant) {
				values = FloatArray(((length - 11) * 8 - unusedBits) / numBits)
				for (i in values.indices) {
					values[i] = ref + scale * gribInputStream.readUBits(numBits)
					if (values[i] > maxValue) maxValue = values[i]
					if (values[i] < minValue) minValue = values[i]
				}
			} else {
				// constant valued - same min and max
				values = FloatArray(gds.gridNX * gds.gridNY)
				Arrays.fill(values, ref)
			}
		}
		gribInputStream.seekNextByte()
		gribInputStream.skip((unusedBits / 8).toLong())
	}

	/**
	 * Get data/parameter value as a float.
	 * @param index
	 *
	 * @return  array of parameter values
	 * @throws NoValidGribException
	 */
	fun getValue(index: Int): Float {
		if (index >= 0 && index < values.size) return values[index]
		throw NoValidGribException("GribRecordBDS: Array index out of bounds")
	}

	override fun toString(): String = listOfNotNull(
			"GRIB1 BDS section:",
			"\tMin/max value: ${minValue} ${maxValue}",
			"\tReference value: ${referenceValue}",
			"\tIs a constant: ${isConstant}",
			"\tBinary scale: ${binaryScale}",
			"\tNumber of bits: ${numBits}")
			.joinToString("\n")
}