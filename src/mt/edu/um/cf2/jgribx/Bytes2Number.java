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
package mt.edu.um.cf2.jgribx;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;


/**
 * A class that contains several static methods for converting multiple bytes into
 * one float or integer.
 *
 * @author  Benjamin Stark
 * @version 1.0
 */

public class Bytes2Number
{
    /**
     * Integer (Sign & Magnitude)
     */
    public static final int INT_SM = 1;
    /**
     * Integer (Two's Complement)
     */
    public static final int INT_TC = 2;
    
    public static final int FLOAT_IBM = 0;
    public static final int FLOAT_IEEE754 = 1;
    
    public static int bytesToUint(byte b)
    {
        byte[] bytes = new byte[1];
        bytes[0] = b;
        return bytesToUint(bytes);
    }
    
    public static int bytesToUint(byte[] bytes)
    {
        int nBytes = bytes.length;
        if (nBytes > Integer.BYTES)
            throw new IllegalArgumentException("nBytes cannot be larger than 4");
        int value = 0;
        
        for (int i=0; i<nBytes; i++)
        {
            value |= ((bytes[i] & 0xFF) << ((nBytes - i - 1))*8);
        }
        return value;
    }
    
    public static long bytesToLong(byte[] bytes)
    {
        int nBytes = bytes.length;
        if (nBytes > Long.BYTES)
            throw new IllegalArgumentException("nBytes cannot be larger than " + Long.BYTES + " bytes");
        int value = 0;
        
        for (int i=0; i<nBytes; i++)
        {
            value |= ((bytes[i] & 0xFF) << ((nBytes - i - 1))*8);
        }
        return value;
    }
    
    public static int bytesToInt(byte[] bytes, int mode)
    {
        boolean negative;
        int value = 0;
        int nBytes = bytes.length;
        
        if (mode == INT_SM)
        {
            negative = (bytes[0] & 0x80) == 0x80;
            bytes[0] &= 0x7F;
            value = bytesToUint(bytes);
            if (negative) value *= -1;
        }
        else if (mode == INT_TC)
        {
            for (int i=0; i<nBytes; i++)
            {
                value |= (bytes[i] << ((nBytes - i - 1))*8);
            }
        }
        
        return value;
    }
    
    /**
    * Convert an array of bytes into a floating point in the specified format.
     * @param bytes
     * @param format
     * @return a single value as a single precision IEEE754 float
     * @see https://en.wikipedia.org/wiki/IBM_Floating_Point_Architecture
     */
    public static float bytesToFloat(byte[] bytes, int format)
    {
        switch (format)
        {
            case FLOAT_IBM:
                int sign = (bytes[0] & 0x80) == 0x80 ? -1 : 1;
                int exponent = (bytes[0] & 0x7F) - 64;
                int mantissa = bytesToUint(Arrays.copyOfRange(bytes, 1, 4));
                return (float) (sign * Math.pow(16.0, exponent - 6) * mantissa);
            case FLOAT_IEEE754:
                return ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).getFloat();
            default:
                throw new IllegalArgumentException("Invalid format specified");
        }
    }
	
   /**
    * Convert two bytes into a signed integer.
    *
    * @param a higher byte
    * @param b lower byte
    *
    * @return integer value
    */
   public static int int2(int a, int b)
   {

      return (1 - ((a & 128) >> 6)) * ((a & 127) << 8 | b);
   }


   /**
    * Convert three bytes into a signed integer.
    *
    * @param a higher byte
    * @param b middle part byte
    * @param c lower byte
    *
    * @return integer value
    */
   public static int int3(int a, int b, int c)
   {

      return (1 - ((a & 128) >> 6)) * ((a & 127) << 16 | b << 8 | c);
   }


   /**
    * Convert four bytes into a signed integer.
    *
    * @param a highest byte
    * @param b higher middle byte
    * @param c lower middle byte
    * @param d lowest byte
    *
    * @return integer value
    */
   public static int int4(int a, int b, int c, int d)
   {

      return (1 - ((a & 128) >> 6)) * ((a & 127) << 24 | b << 16 | c << 8 | d);
   }


   /**
    * Convert two bytes into an unsigned integer.
    *
    * @param a higher byte
    * @param b lower byte
    *
    * @return integer value
    */
   public static int uint2(int a, int b)
   {

      return a << 8 | b;
   }


   /**
    * Convert three bytes into an unsigned integer.
    *
    * @param a higher byte
    * @param b middle byte
    * @param c lower byte
    *
    * @return integer value
    */
   public static int uint3(int a, int b, int c)
   {

      return a << 16 | b << 8 | c;
   }


   /**
    * Convert four bytes into a float value.
    *
    * @param a highest byte
    * @param b higher byte
    * @param c lower byte
    * @param d lowest byte
    *
    * @return float value
    */
   public static float float4(int a, int b, int c, int d)
   {
       /*
        byte test[] = {(byte)a, (byte) b, (byte) c, (byte) d};
        return ByteBuffer.wrap(test).getFloat();
        */  
      int sgn, mant, exp;

      mant = b << 16 | c << 8 | d;
      if (mant == 0) return 0.0f;

      sgn = -(((a & 128) >> 6) - 1);
      exp = (a & 127) - 64;

      return (float) (sgn * Math.pow(16.0, exp - 6) * mant);
   }

}

