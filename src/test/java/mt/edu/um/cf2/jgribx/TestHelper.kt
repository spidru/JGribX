package mt.edu.um.cf2.jgribx

import kotlin.math.pow

fun Int.byteSpace() = (this * 8).bitSpace()

fun Int.bitSpace() = 2.0.pow(this).toInt()
