package mt.edu.um.cf2.jgribx

import kotlin.math.pow

fun Int.bytesSpace() = 2.0.pow(this * 8).toInt()
