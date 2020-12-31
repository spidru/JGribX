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

import mt.edu.um.cf2.jgribx.Bytes2Number.bytesToFloat
import mt.edu.um.cf2.jgribx.Bytes2Number.bytesToInt
import mt.edu.um.cf2.jgribx.Bytes2Number.bytesToUint
import java.io.FilterInputStream
import java.io.IOException
import java.io.InputStream
import kotlin.math.ceil

/**
 * This class is an input stream wrapper that can read a specific number of bytes and bits from an input stream.
 *
 * @param inputStream input stream that will be wrapped
 * @author  Benjamin Stark
 * @version 1.0
 */
class GribInputStream(inputStream: InputStream?, private val onRead: (Long) -> Unit) : FilterInputStream(inputStream) {
	/** Buffer for one byte which will be processed bit by bit. */
	protected var bitBuf = 0

	/** Current bit position in <tt>bitBuf</tt>. */
	protected var bitPos = 0

	private var markedCountBits: Long = 0

	/** A byte counter which can be used to keep track of the number of bytes read */
	var bitCounter: Long = 0
		private set

	val byteCounter: Int
		get() = ceil(bitCounter.toDouble() / 8).toInt()

	private var readBytes = 0L
	private var markedBytes = 0L

	@Synchronized
	override fun mark(readLimit: Int) {
		super.mark(readLimit)
		markedCountBits = bitCounter
		markedBytes = readBytes
	}

	@Synchronized
	override fun reset() {
		super.reset()
		readBytes -= (readBytes - markedBytes)
		bitCounter = markedCountBits
	}

	fun resetBitCounter() {
		bitCounter = 0
	}

	fun seekNextByte() {
		if (bitPos != 0) {
			bitCounter += bitPos.toLong()
			// System.out.println("countBits += "+bitPos);
			bitPos = 0
		}
	}

	/**
	 * Reads the specified number of bytes from the input stream and converts the byte array into an unsigned integer.
	 * @param nBytes     the number of bytes to read
	 * @return the unsigned integer value corresponding to the byte array
	 * @throws IOException
	 */
	fun readUINT(nBytes: Int): Int {
		val data = read(nBytes)
		return bytesToUint(data)
	}

	fun readINT(nBytes: Int, format: Int): Int {
		val data = read(nBytes)
		return bytesToInt(data, format)
	}

	fun readFloat(nBytes: Int, format: Int): Float {
		val data = read(nBytes)
		return bytesToFloat(data, format)
	}

	/**
	 * Read an unsigned 8 bit value.
	 *
	 * @return unsigned 8 bit value as integer
	 * @throws IOException
	 */
	fun readUI8(): Int {
		val ui8 = `in`.read()
		if (ui8 < 0) throw IOException("End of input.")
		onRead(++readBytes)
		return ui8
	}

	/**
	 * Read specific number of unsigned bytes from the input stream.
	 *
	 * @param length number of bytes to read and return as integers
	 * @return unsigned bytes as integer values
	 * @throws IOException
	 */
	fun readUI8(length: Int): IntArray {
		val data = IntArray(length)
		var read = 0
		var i = 0
		while (i < length && read >= 0) {
			read = this.read()
			data[i] = read
			i++
		}
		if (read < 0) throw IOException("End of input.")
		return data
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
		val numRead = this.read(data)
		if (numRead < length) {
			// retry reading
			val numReadRetry = this.read(data, numRead, data.size - numRead)
			if (numRead + numReadRetry < length) throw IOException("Unexpected end of input.")
		}
		return data
	}

	override fun read(): Int {
		val value = super.read()
		bitCounter += 8
		onRead(++readBytes)
		return value
	}

	override fun read(b: ByteArray, off: Int, len: Int): Int {
		val i = super.read(b, off, len)
		bitCounter += (len * 8).toLong()
		readBytes += len
		onRead(readBytes)
		return i
	}

	override fun skip(n: Long): Long {
		var skipped = 0L
		while (skipped < n) skipped += super.skip(n - skipped)
		bitCounter += n * 8
		readBytes += n
		onRead(readBytes)
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
			bitPos = 8
		}
		while (true) {
			val shift = bitsLeft - bitPos
			if (shift > 0) {

				// Consume the entire buffer
				result = result or (bitBuf shl shift).toLong()
				bitsLeft -= bitPos

				// Get the next byte from the input stream
				bitBuf = `in`.read()
				bitPos = 8
			} else {

				// Consume a portion of the buffer
				result = result or (bitBuf shr -shift).toLong()
				bitPos -= bitsLeft
				bitBuf = bitBuf and (0xff shr 8 - bitPos) // mask off consumed bits
				readBytes += (numBits / 8)
				onRead(readBytes)
				return result
			}
		}
	}

	/**
	 * Read a signed value from the given number of bits
	 *
	 * @param numBits number of bits used for the signed value
	 * @return value read from <tt>numBits</tt> bits as integer
	 * @throws IOException
	 */
	fun readSBits(numBits: Int): Int {
		// Get the number as an unsigned value.
		var uBits = readUBits(numBits)

		// Is the number negative?
		if (uBits and (1L shl numBits - 1) != 0L) {

			// Yes. Extend the sign.
			uBits = uBits or (-1L shl numBits)
		}
		return uBits.toInt()
	}
}