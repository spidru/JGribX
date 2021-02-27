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

import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.pow

/**
 * A class that contains several static methods for converting multiple bytes into one float or integer.
 *
 * @author  Benjamin Stark
 * @version 1.0
 */
object Bytes2Number {
	/** Integer (Sign & Magnitude) */
	const val INT_SM = 1

	/** Integer (Two's Complement) */
	private const val INT_TC = 2

	const val FLOAT_IBM = 0
	const val FLOAT_IEEE754 = 1

	fun bytesToUint(b: Byte): Int {
		val bytes = ByteArray(1)
		bytes[0] = b
		return bytesToUint(bytes)
	}

	fun bytesToUint(bytes: ByteArray): Int {
		val nBytes = bytes.size
		require(nBytes <= Int.SIZE_BYTES) { "nBytes cannot be larger than ${Int.SIZE_BYTES} bytes" }
		var value = 0
		repeat(nBytes) { i ->
			value = value or (bytes[i].toInt() and 0xFF shl (nBytes - i - 1) * 8)
		}
		return value
	}

	fun uintToBytes(value: Int, nBytes: Int = Int.SIZE_BYTES): ByteArray {
		require(nBytes <= Int.SIZE_BYTES) { "nBytes cannot be larger than ${Int.SIZE_BYTES} bytes" }
		val bytes = ByteArray(nBytes)
		repeat(nBytes) { i ->
			bytes[i] = ((value shr ((nBytes - i - 1) * 8)) and 0xFF).toByte()
		}
		return bytes
	}

	fun bytesToLong(bytes: ByteArray): Long {
		val nBytes = bytes.size
		require(nBytes <= Long.SIZE_BYTES) { "nBytes cannot be larger than ${Long.SIZE_BYTES} bytes" }
		var value = 0L
		repeat(nBytes) { i ->
			value = value or (bytes[i].toLong() and 0xFF shl ((nBytes - i - 1) * 8))
		}
		return value
	}

	fun longToBytes(value: Long, nBytes: Int = Long.SIZE_BYTES): ByteArray {
		require(nBytes <= Long.SIZE_BYTES) { "nBytes cannot be larger than ${Long.SIZE_BYTES} bytes" }
		val bytes = ByteArray(nBytes)
		repeat(nBytes) { i ->
			bytes[i] = ((value shr ((nBytes - i - 1) * 8)) and 0xFF).toByte()
		}
		return bytes
	}

	fun bytesToInt(bytes: ByteArray, mode: Int): Int {
		var value = 0
		when (mode) {
			INT_SM -> {
				val negative = bytes[0].toInt() and 0x80 == 0x80
				bytes[0] = (bytes[0].toInt() and 0x7F).toByte()
				value = bytesToUint(bytes)
				if (negative) value *= -1
			}
			INT_TC -> {
				for (i in bytes.indices) {
					value = value or (bytes[i].toInt() shl (bytes.size - i - 1) * 8)
				}
			}
		}
		return value
	}

	/**
	 * Convert an array of bytes into a floating point in the specified format.
	 *
	 * @param bytes
	 * @param format
	 * @return a single value as a single precision IEEE754 float
	 * @see <a href="https://en.wikipedia.org/wiki/IBM_Floating_Point_Architecture">IBM_Floating_Point_Architecture</a>
	 */
	fun bytesToFloat(bytes: ByteArray, format: Int): Float = when (format) {
		FLOAT_IBM -> {
			val sign = if (bytes[0].toInt() and 0x80 == 0x80) -1 else 1
			val exponent: Int = (bytes[0].toInt() and 0x7F) - 64
			val mantissa = bytesToUint(bytes.copyOfRange(1, 4))
			(sign * 16.0.pow((exponent - 6).toDouble()) * mantissa).toFloat()
		}
		FLOAT_IEEE754 -> ByteBuffer
				.wrap(bytes)
				.order(ByteOrder.BIG_ENDIAN)
				.float
		else -> throw IllegalArgumentException("Invalid format specified")
	}

	/**
	 * Convert two bytes into a signed integer.
	 *
	 * @param a higher byte
	 * @param b lower byte
	 * @return integer value
	 */
	fun int2(a: Int, b: Int): Int = (1 - (a and 128 shr 6)) * (a and 127 shl 8 or b)

	/**
	 * Convert three bytes into a signed integer.
	 *
	 * @param a higher byte
	 * @param b middle part byte
	 * @param c lower byte
	 * @return integer value
	 */
	fun int3(a: Int, b: Int, c: Int): Int = (1 - (a and 128 shr 6)) * (a and 127 shl 16 or (b shl 8) or c)

	/**
	 * Convert four bytes into a signed integer.
	 *
	 * @param a highest byte
	 * @param b higher middle byte
	 * @param c lower middle byte
	 * @param d lowest byte
	 * @return integer value
	 */
	fun int4(a: Int, b: Int, c: Int, d: Int): Int {
		return (1 - (a and 128 shr 6)) * (a and 127 shl 24 or (b shl 16) or (c shl 8) or d)
	}

	/**
	 * Convert two bytes into an unsigned integer.
	 *
	 * @param a higher byte
	 * @param b lower byte
	 * @return integer value
	 */
	fun uint2(a: Int, b: Int): Int {
		return a shl 8 or b
	}

	/**
	 * Convert three bytes into an unsigned integer.
	 *
	 * @param a higher byte
	 * @param b middle byte
	 * @param c lower byte
	 * @return integer value
	 */
	fun uint3(a: Int, b: Int, c: Int): Int {
		return a shl 16 or (b shl 8) or c
	}

	/**
	 * Convert four bytes into a float value.
	 *
	 * @param a highest byte
	 * @param b higher byte
	 * @param c lower byte
	 * @param d lowest byte
	 * @return float value
	 */
	fun float4(a: Int, b: Int, c: Int, d: Int): Float {
		//byte test[] = {(byte)a, (byte) b, (byte) c, (byte) d};
		//return ByteBuffer.wrap(test).getFloat();
		val mant = b shl 16 or (c shl 8) or d
		if (mant == 0) return 0.0f
		val sgn = -((a and 128 shr 6) - 1)
		val exp = (a and 127) - 64
		return (sgn * 16.0.pow((exp - 6).toDouble()) * mant).toFloat()
	}
}
