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
package mt.edu.um.cf2.jgribx.grib1;

import java.io.IOException;
import java.util.Arrays;
import mt.edu.um.cf2.jgribx.Bytes2Number;
import mt.edu.um.cf2.jgribx.GribInputStream;
import mt.edu.um.cf2.jgribx.NoValidGribException;

/**
 * A class that represents the bitmap section (BMS) of a GRIB record. It
 * indicates grid points where no parameter value is defined.
 *
 * @author  Benjamin Stark
 * @version 1.0
 */

public class Grib1RecordBMS
{

   /**
    * Length in bytes of this section.
    */
   protected int length;

   /**
    * The bit map.
    */
   protected boolean[] bitmap;


   // *** constructors *******************************************************

   /**
    * Constructs a <tt>GribRecordBMS</tt> object from a bit input stream.
    *
    * @param in bit input stream with BMS content
    *
    * @throws IOException           if stream can not be opened etc.
    * @throws NoValidGribException  if stream contains no valid GRIB file
    */
   public Grib1RecordBMS(GribInputStream in) throws IOException, NoValidGribException
   {

      byte[] octets;
      int[] bitmask = {128, 64, 32, 16, 8, 4, 2, 1};

      octets = new byte[3];
      in.read(octets);

      // octets 1-3 (length of section)
      this.length = Bytes2Number.bytesToUint(Arrays.copyOfRange(octets, 0, 3));

      // read rest of section
      octets = new byte[this.length - 3];
      in.read(octets);
      int nBitsUnused = Bytes2Number.bytesToUint(octets[0]);

      // octets 5-6
      if (octets[1] != 0 || octets[2] != 0)
         throw new NoValidGribException("GribRecordBMS: No bit map defined here.");

      // create new bit map, octet 4 contains number of unused bits at the end
      this.bitmap = new boolean[(this.length - 6) * 8 - nBitsUnused];

      // fill bit map
      for (int i = 0; i < this.bitmap.length; i++)
         this.bitmap[i] = (octets[i / 8 + 3] & bitmask[i % 8]) != 0;
   }


   // *** public methods *********************************************************

   /**
    * Get length in bytes of this section.
    *
    * @return length in bytes
    */
   public int getLength()
   {

      return this.length;
   }


   /**
    * Get bit map.
    *
    * @return bit map as array of boolean values
    */
   public boolean[] getBitmap()
   {

      return this.bitmap;
   }


   /**
    * Get a string representation of this BMS.
    *
    * @return string representation of this BMS
    */
   @Override
   public String toString()
   {

      return "    BMS section:" + '\n' +
            "        bitmap length: " + this.bitmap.length;
   }

}



