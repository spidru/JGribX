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

import java.io.FilterInputStream
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.ceil
import kotlin.math.pow

/**
 * This class is an input stream wrapper that can read a specific number of bytes and bits from an input stream.
 *
 * @param inputStream input stream that will be wrapped
 * @author  Benjamin Stark
 * @version 1.0
 */
class GribInputStream(inputStream: InputStream?, private val onRead: (Long) -> Unit = {}) : FilterInputStream(inputStream) {
	inner class ByteCounter internal constructor(private val gribInputStream: GribInputStream,
												 private val length: Long? = null,
												 internal var read: Long = 0,
												 private val onClose: () -> Unit = {}) : AutoCloseable {
		override fun close() {
			length?.also { length ->
				gribInputStream.skip(length - read)
				byteCounters.remove(this)
			}
			onClose()
		}
	}

	/** Buffer for one byte which will be processed bit by bit. */
	private var bitBuf = 0

	/** Current bit position in <tt>bitBuf</tt>. */
	private var bitPos = 0

	private var markedCountBits: Long = 0

	/** A byte counter which can be used to keep track of the number of bytes read */
	var bitCounter: Long = 0
		private set

	val byteCounter: Int
		get() = ceil(bitCounter.toDouble() / 8).toInt()

	private var readBytes = 0L
	private var markedBytes = 0L

	private val byteCounters = mutableSetOf<ByteCounter>()

	fun createByteCounter(length: Long? = null, initial: Long = 0, onClose: () -> Unit = {}) =
			ByteCounter(this, length, initial, onClose).also { byteCounters.add(it) }

	fun destroyByteCounter(byteCounter: ByteCounter) = byteCounters.remove(byteCounter)

	private fun incrementByteCounters(increment: Number = 1): Long {
		byteCounters.forEach { it.read += increment.toLong() }
		readBytes += increment.toLong()
		return readBytes
	}

	private var peekBuffer: ByteArray = ByteArray(4) // Minimize recreation

	internal fun peek(bytes: Int): ByteArray {
		if (peekBuffer.size != bytes) peekBuffer = ByteArray(bytes)
		mark(bytes)
		read(peekBuffer)
		reset()
		resetBitCounter()
		return peekBuffer
	}

	@Synchronized
	override fun mark(readLimit: Int) {
		super.mark(readLimit)
		markedCountBits = bitCounter
		markedBytes = readBytes
	}

	@Synchronized
	override fun reset() {
		super.reset()
		incrementByteCounters(-(readBytes - markedBytes))
		bitCounter = markedCountBits
	}

	fun resetBitCounter() {
		bitCounter = 0
	}

	fun seekNextByte() {
		if (bitPos != 0) {
			bitCounter += bitPos.toLong()
			bitPos = 0
		}
	}

	/**
	 * Reads the specified number of bytes from the input stream and converts the byte array into an unsigned integer.
	 * @param nBytes     the number of bytes to read
	 * @return the unsigned integer value corresponding to the byte array
	 * @throws IOException
	 */
	fun readUInt(nBytes: Int): Int {
		val data = read(nBytes)
		return bytesToUint(data)
	}

	fun readSMInt(bytes: Int = 1): Int {
		val data = read(bytes)
		val negative = data[0].toInt() and 0x80 == 0x80
		data[0] = (data[0].toInt() and 0x7F).toByte()
		var value = bytesToUint(data)
		if (negative) value *= -1
		return value
	}

	fun readLong(bytes: Int = 4): Long {
		val data = read(bytes)
		require(bytes <= Long.SIZE_BYTES) { "nBytes cannot be larger than ${Long.SIZE_BYTES} bytes" }
		var value = 0L
		repeat(bytes) { i ->
			value = value or (data[i].toLong() and 0xFF shl ((bytes - i - 1) * 8))
		}
		return value
	}

	fun readFloatIEEE754(bytes: Int = 4) = ByteBuffer
			.wrap(read(bytes))
			.order(ByteOrder.BIG_ENDIAN)
			.float

	fun readFloatIBM(): Float {
		val data = read(4)
		val sign = if (data[0].toInt() and 0x80 == 0x80) -1 else 1
		val exponent: Int = (data[0].toInt() and 0x7F) - 64
		val mantissa = bytesToUint(data.copyOfRange(1, 4))
		return (sign * 16.0.pow((exponent - 6).toDouble()) * mantissa).toFloat()
	}

	/**
	 * Read specific number of bytes from the input stream.
	 *
	 * @param length number of bytes to read
	 * @return array of read bytes
	 * @throws IOException
	 */
	fun read(length: Int): ByteArray {
		val data = ByteArray(length)
		val numRead = read(data)
		if (numRead < length) {
			// retry reading
			val numReadRetry = read(data, numRead, data.size - numRead)
			if (numRead + numReadRetry < length) throw IOException("Unexpected end of input.")
		}
		return data
	}

	override fun read(): Int {
		val value = super.read()
		bitCounter += 8
		onRead(incrementByteCounters())
		return value
	}

	override fun read(b: ByteArray): Int {
		val read = super.read(b)
		onRead(incrementByteCounters(read))
		return read
	}

	override fun read(b: ByteArray, off: Int, len: Int): Int {
		val i = super.read(b, off, len)
		bitCounter += (len * 8).toLong()
		onRead(incrementByteCounters(len))
		return i
	}

	override fun skip(n: Long): Long {
		var skipped = 0L
		while (skipped < n) skipped += super.skip(n - skipped)
		bitCounter += n * 8
		onRead(incrementByteCounters(n))
		return n
	}

	/**
	 * Read an unsigned value from the given number of bits.
	 *
	 * @param numBits number of bits used for the unsigned value
	 * @return value read from <tt>numBits</tt> bits as long
	 * @throws IOException
	 */
	fun readUBits(numBits: Int): Long {
		if (numBits == 0) return 0
		bitCounter += numBits.toLong()
		var bitsLeft = numBits
		var result: Long = 0
		if (bitPos == 0) {
			bitBuf = `in`.read()
			onRead(incrementByteCounters())
			bitPos = 8
		}
		while (true) {
			val shift = bitsLeft - bitPos
			if (shift > 0) { // Consume the entire buffer
				result = result or (bitBuf shl shift).toLong()
				bitsLeft -= bitPos

				// Get the next byte from the input stream
				bitBuf = `in`.read()
				onRead(incrementByteCounters())
				bitPos = 8
			} else { // Consume a portion of the buffer
				result = result or (bitBuf shr -shift).toLong()
				bitPos -= bitsLeft
				bitBuf = bitBuf and (0xff shr 8 - bitPos) // mask off consumed bits
				return result
			}
		}
	}

	fun readString(bytes: Int = 1): String {
		if (peekBuffer.size != bytes) peekBuffer = ByteArray(bytes)
		read(peekBuffer)
		return String(peekBuffer)
	}

	private fun bytesToUint(bytes: ByteArray): Int {
		val nBytes = bytes.size
		require(nBytes <= Int.SIZE_BYTES) { "nBytes cannot be larger than ${Int.SIZE_BYTES} bytes" }
		var value = 0
		repeat(nBytes) { i ->
			value = value or (bytes[i].toInt() and 0xFF shl (nBytes - i - 1) * 8)
		}
		return value
	}
}
