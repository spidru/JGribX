package mt.edu.um.cf2.jgribx

/**
 * Floats encoding/decoding results in loss of precision, here we define the max precision we can work with.
 * This is than used when comparing two floats to determine whether they are equal.
 */
internal const val FLOAT_PRECISION = 0.000015
