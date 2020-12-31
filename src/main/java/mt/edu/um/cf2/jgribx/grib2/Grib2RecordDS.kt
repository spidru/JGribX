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
import kotlin.math.pow

/**
 * @author AVLAB-USER3
 */
class Grib2RecordDS {
	companion object {
		internal fun readFromStream(gribInputStream: GribInputStream,
									drs: Grib2RecordDRS,
									gds: Grib2RecordGDS,
									bms: Grib2RecordBMS): Grib2RecordDS? {
			val ds = Grib2RecordDS()
			ds.length = gribInputStream.readUINT(4)
			val section = gribInputStream.readUINT(1)
			if (section != 7) {
				Logger.error("Invalid DS")
				return null
			}

			val DD = 10.0.pow(drs.decimalScaleFactor.toDouble()).toFloat()
			val R = drs.refValue
			val EE = 2.0.pow(drs.binaryScaleFactor.toDouble()).toFloat()
			//val refVal = R / DD
			val NG = drs.nGroups
			if (NG == 0) Logger.error("Zero groups not supported yet")
			val os = drs.spatialDiffOrder
			val descriptorOctets = drs.spatialDescriptorOctets
			val ival1: Int
			var ival2 = 0
			val minsd: Int
			if (descriptorOctets > 0) {
				// first order spatial differencing g1 and gMin
				ival1 = gribInputStream.readINT(descriptorOctets, Bytes2Number.INT_SM)
				if (os == 2) { // second order spatial differencing h1, h2, hMin
					ival2 = gribInputStream.readINT(descriptorOctets, Bytes2Number.INT_SM)
				}
				minsd = gribInputStream.readINT(descriptorOctets, Bytes2Number.INT_SM)
			} else {
				val data = FloatArray(gds.numberOfDataPoints)
				for (i in data.indices) {
					data[i] = drs.missingValue
				}
				ds.data = data
				return ds
			}

			// Get reference values for groups
			///////////////////////////////////////
			/////// TESTING ONLY
			// in.seekNextByte();
			// int y1;
			// while ((y1 = in.readUINT(1)) == 0)
			// in.mark(1);
			// int y2 = in.readUINT(1);
			// System.out.println("Values: "+y1+" "+y2);
			// in.reset();
			// int y3 = (int) in.readUBits(1);
			// in.seekNextByte();
			// int y4 = (int) in.readUBits(4);
			///////////////////////////////////////
			val X1 = IntArray(NG)
			if (drs.nBits != 0) {
				gribInputStream.seekNextByte()
				for (i in 0 until NG) {
					X1[i] = gribInputStream.readUBits(drs.nBits).toInt()
				}
			}

			// Get number of bits used to encode each group
			val NB = IntArray(NG) // initialised to zero
			if (drs.groupWidthBits != 0) {
				gribInputStream.seekNextByte()
				for (i in 0 until NG) {
					NB[i] = gribInputStream.readUBits(drs.groupWidthBits).toInt()
					NB[i] += drs.refGroupWidths
				}
			}

			// Get the scaled group lengths
			val L = IntArray(NG)
			if (drs.nBitsScaledGroupLengths != 0) {
				gribInputStream.seekNextByte()
				for (i in 0 until NG) {
					L[i] = gribInputStream.readUBits(drs.nBitsScaledGroupLengths).toInt()
				}
			}
			var totalL = 0
			for (i in 0 until NG) {
				L[i] = L[i] * drs.groupLengthIncrement + drs.refGroupLengths
				totalL += L[i]
			}
			totalL -= L[NG - 1]
			totalL += drs.lastGroupLength
			L[NG - 1] = drs.lastGroupLength

			// test
			if (drs.missingValueManagement != 0) throw NotSupportedException("Missing Value Management is not supported")

			if (totalL != drs.nDataPoints) {
				println("nPoints != drs.nPoints: ${totalL} != ${drs.nDataPoints}")
				val data = FloatArray(drs.nDataPoints)
				for (i in 0 until drs.nDataPoints) {
					data[i] = drs.missingValue
				}
				ds.data = data
				return ds
			}
			var data = FloatArray(gds.numberOfDataPoints)

			// Get X2 values and calculate the results Y using the formula:
			//  Y = (R + (X1 + X2) * (2^E)) / (10^D)
			var count = 0
			var dataSize = 0
			var dataBitMap: BooleanArray? = null
			gribInputStream.seekNextByte()
			if (drs.missingValueManagement == 0) {
				for (i in 0 until NG) {
					if (NB[i] != 0) {
						for (j in 0 until L[i]) {
							data[count++] = (gribInputStream.readUBits(NB[i]).toInt() + X1[i]).toFloat()
						}
					} else {
						for (j in 0 until L[i]) {
							data[count++] = X1[i].toFloat()
						}
					}
				}
			} else if (drs.missingValueManagement == 1 || drs.missingValueManagement == 2) {
				dataBitMap = BooleanArray(gds.numberOfDataPoints)
				for (i in 0 until NG) {
					if (NB[i] != 0) {
						val msng1 = 2.0.pow(NB[i].toDouble()).toInt() - 1
						val msng2 = msng1 - 1
						for (j in 0 until L[i]) {
							data[count] = gribInputStream.readUBits(NB[i]).toFloat()
							if (data[count] == msng1.toFloat() || drs.missingValueManagement == 2 && data[count] == msng2.toFloat()) {
								dataBitMap[count] = false
							} else {
								dataBitMap[count] = true
								data[dataSize++] = data[count] + X1[i]
							}
							count++
						}
					} else {
						val msng1 = 2.0.pow(drs.nBits.toDouble()).toInt()
						val msng2 = msng1 - 1
						if (X1[i] == msng1) {
							for (j in 0 until L[i]) {
								dataBitMap[count++] = false
							}
						} else if (drs.missingValueManagement == 2 && X1[i] == msng2) {
							for (j in 0 until L[i]) {
								dataBitMap[count++] = false
							}
						} else {
							for (j in 0 until L[i]) {
								dataBitMap[count] = true
								data[dataSize++] = X1[i].toFloat()
								count++
							}
						}
					}
				}
			}
			gribInputStream.seekNextByte()

			// first order spatial differencing
			if (os == 1) {
				data[0] = ival1.toFloat()
				val itemp = if (drs.missingValueManagement == 0) gds.numberOfDataPoints else dataSize
				for (i in 1 until itemp) {
					data[i] += minsd.toFloat()
					data[i] = data[i] + data[i - 1]
				}
			} else if (os == 2) {
				data[0] = ival1.toFloat()
				data[1] = ival2.toFloat()
				val itemp = if (drs.missingValueManagement == 0) gds.numberOfDataPoints else dataSize
				for (i in 2 until itemp) {
					data[i] += minsd.toFloat()
					data[i] = data[i] + 2 * data[i - 1] - data[i - 2]
				}
			}
			if (drs.missingValueManagement == 0) {
				for (i in data.indices) {
					data[i] = (R + data[i] * EE) / DD
				}
			} else if (dataBitMap != null) { // (drs.missingValueManagement == 1 || drs.missingValueManagement == 2)
				var count2 = 0
				val tmp = FloatArray(gds.numberOfDataPoints)
				for (i in data.indices) {
					if (dataBitMap[i]) {
						tmp[i] = (R + data[count2++] * EE) / DD
					} else {
						tmp[i] = drs.missingValue
					}
				}
				data = tmp
			}

			// bit map is used
			if (bms.indicator != Grib2RecordBMS.Indicator.BITMAP_NONE) {
				Logger.error("Bitmap not supported yet")
				//val idx = 0
				//val tmp = FloatArray(gds.numberOfDataPoints)
				//for (i in 0 until gds.numberOfDataPoints) {
				//	if ((bitmap[i/8] & ))
				//}
			}
			ds.data = data
			return ds
		}
	}

	protected var length = 0
	var data: FloatArray = floatArrayOf()
}