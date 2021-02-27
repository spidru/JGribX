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
package mt.edu.um.cf2.jgribx

import java.io.FilterOutputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.experimental.and
import kotlin.experimental.or
import kotlin.math.abs

/**
 * This class is an output stream wrapper that can read a specific number of bytes and bits from an input stream.
 *
 * @param outputStream output stream that will be wrapped
 * @author  Jan Kubovy [jan@kubovy.eu]
 */
class GribOutputStream(outputStream: OutputStream) : FilterOutputStream(outputStream) {

	fun writeByte(value: Byte) = out.write(value.toInt())

	fun writeUInt(value: Int, bytes: Int = 1) = out.write(uintToBytes(value, bytes))

	fun writeSMInt(value: Int, bytes: Int = 1) {
		val data = uintToBytes(abs(value), bytes)
		data[0] = if (value < 0) data[0] or 0x80.toByte() else data[0] and 0x7F.toByte()
		out.write(data)
	}

	fun writeLong(value: Long, bytes: Int = 1) {
		require(bytes <= Long.SIZE_BYTES) { "nBytes cannot be larger than ${Long.SIZE_BYTES} bytes" }
		val data = ByteArray(bytes)
		repeat(bytes) { i ->
			data[i] = ((value shr ((bytes - i - 1) * 8)) and 0xFF).toByte()
		}
		out.write(data)
	}

	fun writeFloatIEEE754(value: Float, bytes: Int = 4) {
		val data = ByteBuffer.allocate(bytes).order(ByteOrder.BIG_ENDIAN).putFloat(value).array()
		out.write(data)
	}

	fun writeFloatIBM(value: Float) {
		if (value.isNaN()) throw NumberFormatException("Value must be finite")
		val bits = java.lang.Float.floatToIntBits(value)
		val s = if (bits shr 31 == 0) 1 else -1
		val e = bits shr 23 and 0xFF
		val m = if (e == 0) bits and 0x7FFFFF shl 1 else bits and 0x7FFFFF or 0x800000

		var exp = (e - 150) / 4 + 6
		var mant: Int
		val mantissaShift = (e - 150) % 4 // compensate for base 16

		mant = if (mantissaShift >= 0) m shr mantissaShift else m shr Math.abs(mantissaShift)
		if (mant > 0xFFFFFFF) {
			mant = mant shr 4
			exp++
		} // loose of precision */

		val a = (1 - s shl 6 or exp + 64).toByte()
		val bytes = byteArrayOf(a, (mant shr 16).toByte(), (mant shr 8).toByte(), mant.toByte())
		out.write(bytes)
	}

	private var bitsWritten: Int = 0
	private var byteToWrite: Int = 0

	fun writeUBits(value: Int, numBits: Int) = writeUBits(value.toLong(), numBits)

	fun writeUBits(value: Long, numBits: Int) {
		// bits = 7
		// 0b1111111
		// 000 001 010 011 100 101 110 111
		repeat(numBits) { bi ->
			val bit = (value shr (numBits - bi - 1)).toInt() and 0x01
			val bitInPos = bit shl (8 - bitsWritten++ - 1)
			byteToWrite = byteToWrite or bitInPos
			if (bitsWritten == 8) {
				out.write(byteToWrite)
				byteToWrite = 0
				bitsWritten = 0
			}
		}
	}

	fun fillByte() {
		if (bitsWritten > 0) {
			out.write(byteToWrite)
			byteToWrite = 0
			bitsWritten = 0
		}
	}

	private fun uintToBytes(value: Int, nBytes: Int = Int.SIZE_BYTES): ByteArray {
		require(nBytes <= Int.SIZE_BYTES) { "nBytes cannot be larger than ${Int.SIZE_BYTES} bytes" }
		val bytes = ByteArray(nBytes)
		repeat(nBytes) { i ->
			bytes[i] = ((value shr ((nBytes - i - 1) * 8)) and 0xFF).toByte()
		}
		return bytes
	}
}
