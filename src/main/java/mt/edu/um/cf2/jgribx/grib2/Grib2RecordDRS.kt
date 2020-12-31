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

import mt.edu.um.cf2.jgribx.Bytes2Number
import mt.edu.um.cf2.jgribx.GribInputStream
import mt.edu.um.cf2.jgribx.Logger
import mt.edu.um.cf2.jgribx.NotSupportedException

/**
 *
 * @author AVLAB-USER3
 */
class Grib2RecordDRS {
	companion object {
		fun readFromStream(gribInputStream: GribInputStream): Grib2RecordDRS? {
			val drs = Grib2RecordDRS()
			drs.length = gribInputStream.readUINT(4)
			val section = gribInputStream.readUINT(1)
			if (section != 5) {
				Logger.error("DRS contains an incorrect section number")
				return null
			}
			drs.nDataPoints = gribInputStream.readUINT(4)

			/* [10-11] Data representation template number */
			val packingType = gribInputStream.readUINT(2)
			when (packingType) {
				3 -> {
					/* Grid Point Data - Complex Packing and Spatial Differencing */drs.refValue =
							gribInputStream.readFloat(4, Bytes2Number.FLOAT_IEEE754)
					drs.binaryScaleFactor = gribInputStream.readINT(2, Bytes2Number.INT_SM)
					drs.decimalScaleFactor = gribInputStream.readINT(2, Bytes2Number.INT_SM)
					drs.nBits = gribInputStream.readUINT(1)
					/*val type = */gribInputStream.readUINT(1)
					/*val splitMethod = */gribInputStream.readUINT(1)
					drs.missingValueManagement = gribInputStream.readUINT(1)
					val missing1 = gribInputStream.readUINT(4)
					val missing2 = gribInputStream.readUINT(4)
					drs.nGroups = gribInputStream.readUINT(4)
					drs.refGroupWidths = gribInputStream.readUINT(1)
					drs.groupWidthBits = gribInputStream.readUINT(1)
					drs.refGroupLengths = gribInputStream.readUINT(4)
					drs.groupLengthIncrement = gribInputStream.readUINT(1)
					drs.lastGroupLength = gribInputStream.readUINT(4)
					drs.nBitsScaledGroupLengths = gribInputStream.readUINT(1)
					drs.spatialDiffOrder = gribInputStream.readUINT(1)
					drs.spatialDescriptorOctets = gribInputStream.readUINT(1)
					when (drs.missingValueManagement) {
						0 -> drs.missingValue = Float.NaN
						1 -> drs.missingValue = missing1.toFloat()
						2 -> drs.missingValue = missing2.toFloat() // FIXME not sure about this
						else -> {
						}
					}
				}
				else -> throw NotSupportedException("Data Representation type ${packingType} not supported")
			}
			return drs
		}
	}

	internal var binaryScaleFactor = 0
	internal var decimalScaleFactor = 0

	// Number of octets required in the data section to specify extra descriptors needed for spatial differencing
	internal var spatialDescriptorOctets = 0

	/** Length increment for the group lengths (see Note 14) */
	internal var groupLengthIncrement = 0

	/** Number of bits used for the group widths (after the reference value in octet 36 has been removed) */
	internal var groupWidthBits = 0

	/** True length of last group */
	internal var lastGroupLength = 0
	protected var length = 0
	internal var missingValue = 0f
	internal var missingValueManagement = 0
	internal var nBits = 0
	internal var nBitsScaledGroupLengths = 0
	internal var nDataPoints = 0
	internal var nGroups = 0
	internal var refGroupLengths = 0
	internal var refGroupWidths = 0
	internal var refValue = 0f
	internal var spatialDiffOrder = 0
}