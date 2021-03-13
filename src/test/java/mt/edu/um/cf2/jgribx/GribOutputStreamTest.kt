package mt.edu.um.cf2.jgribx

import org.junit.Assert.assertEquals
import org.junit.BeforeClass
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.math.pow

class GribOutputStreamTest {
	companion object {
		@BeforeClass
		@JvmStatic
		fun setUpBeforeClass() {
			Logger.mode = Logger.LoggingMode.CONSOLE
			JGribX.setLoggingLevel(Logger.DEBUG)
		}
	}

	@Test
	fun testWriteUBits() {
		listOf(1, 2, 3, 5, 7).forEach { nbits ->
			val interval = (0 until 2.0.pow(nbits).toInt() - 1)
			val bos = ByteArrayOutputStream()
			val gos = GribOutputStream(bos)

			interval.forEach { gos.writeUBits(it.toLong(), nbits) }
			gos.fillByte()

			var i = 0
			bos.toByteArray().forEach { byte ->
				repeat(8) { bit ->
					if (i % nbits == 0) print("\nB[${nbits}|${bit}-${(bit + nbits - 1) % 8}] ")
					print("${byte.toInt() shr (8 - bit - 1) and 0x01}")
					i++
				}
			}
			println()

			val gis = GribInputStream(ByteArrayInputStream(bos.toByteArray()))
			interval.forEach {
				val value = gis.readUBits(nbits)
				println("V[${nbits}] ${value}")
				assertEquals(it, value.toInt())
			}
		}
	}
}
