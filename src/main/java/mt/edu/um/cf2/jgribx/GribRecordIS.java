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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Arrays;
import mt.edu.um.cf2.jgribx.grib2.ProductDiscipline;


/**
 * A class that represents the indicator section (IS) of a GRIB record.
 *
 * 01/01/2001   Benjamin Stark          first version
 * 16/09/2002   Richard D. Gonzalez     modified to indicate support of GRIB edition 1 only
 * 24/05/2017   Andrew Spiteri          added support for GRIB2
 *
 */

public class GribRecordIS
{

   /**
    * Length in bytes of GRIB record.
    */
   protected long recordLength;

   /**
    * Length in bytes of IS section.
    * Section recordLength differs between GRIB editions 1 and 2
 Currently only GRIB edition 1 supported - recordLength is 8 octets/bytes.
    */
   protected int length;

   /**
    * Edition of GRIB specification used.
    */
   private int edition;

   private static String PATTERN = "GRIB";

   protected ProductDiscipline discipline;

   private long byteOffset;

    /**
     * Constructs a {@link GribRecordIS} object from a buffered input stream.
     *
     * @param in {@link BufferedInputStream} containing IS content
     * @return 
     * @throws NotSupportedException 
     * @throws mt.edu.um.cf2.jgribx.NoValidGribException 
     * @throws IOException           if stream can not be opened etc.
     */
    public static GribRecordIS readFromStream(GribInputStream in) throws NotSupportedException, NoValidGribException, IOException
    {
        GribRecordIS is = new GribRecordIS();
        is.byteOffset = in.getAbsoluteBytePosition();
        byte[] octets = new byte[16];
        in.read(octets, 0, 8);
        
        String startCode = new String(Arrays.copyOfRange(octets, 0, 4));
        if (!startCode.equals(PATTERN))
        {
            Logger.println("Start code not recognised: " + startCode, Logger.INFO);
            throw new NoValidGribException("Record does not have a valid GRIB header");
        }
      
        // check GRIB edition number
        is.edition = octets[7];
        switch (is.edition)
        {
            case 1:
                is.length = 8;
                break;
            case 2:
                is.discipline = new ProductDiscipline(octets[6]);
                is.length = 16;
                in.read(octets, 8, 8);
                break;
            default:
                throw new NotSupportedException("GRIB edition " + is.edition +
                " is not yet supported");
        }
      
        // recordLength of GRIB record
        if (is.edition == 1)
            is.recordLength = Bytes2Number.bytesToLong(Arrays.copyOfRange(octets, 4, 7));
        else if (is.edition == 2)
            is.recordLength = Bytes2Number.bytesToLong(Arrays.copyOfRange(octets, 8, 16));
        
        return is;
   }

    /**
     * Get the byte offset of the start of the section relative to the start of the file
     * @return The byte offset
     */
   public long getByteOffset() { return byteOffset; }

    public ProductDiscipline getDiscipline()
    {
        return discipline;
    }

   /**
    * Get the byte recordLength of this GRIB record.
    *
    * @return recordLength in bytes of GRIB record
    */
   public long getRecordLength()
   {
      return this.recordLength;
   }

   /**
    * Get the byte recordLength of the IS section.
    *
    * @return recordLength in bytes of IS section
    */
   public int getLength()
   {
      return this.length;
   }

    /**
     * Get the edition of the GRIB specification used.
     *
     * @return edition number of GRIB specification
     */
    public int getGribEdition()
    {
       return this.edition;
    }

    /**
     * Seeks the location of the next IS header.
     * If a valid header is found, the input stream is repositioned to the
     * position just before the found header.
     * @param in GRIB input stream to read from 
     * @throws IOException 
     */
    public static void seekNext(GribInputStream in) throws IOException
    {
        int nBytesConsumed = in.seekBytePattern(PATTERN.getBytes(), false);
        int nBytesSkipped = nBytesConsumed - PATTERN.length();

        if (nBytesSkipped > 0)
        {
            Logger.println("Extra " + nBytesSkipped + " bytes were found between end of last record "
                    + "and start of next record", Logger.WARNING);
        }
    }

   /**
    * Get a string representation of this IS.
    *
    * @return string representation of this IS
    */
   @Override
   public String toString()
   {

      return "    IS section:" + '\n' +
            "        Grib Edition " + this.edition + '\n' +
            "        length: " + this.recordLength + " bytes";
   }

}


