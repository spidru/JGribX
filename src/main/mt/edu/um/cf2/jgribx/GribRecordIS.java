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
import mt.edu.um.cf2.jgribx.GribCodes.Discipline;


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
   
   protected Discipline discipline;

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
        byte[] octets = new byte[16];
        in.read(octets, 0, 8);
        
        String startCode = new String(Arrays.copyOfRange(octets, 0, 4));
        if (!startCode.equals("GRIB"))
        {
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
                is.discipline = getDiscipline(octets[6]);
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
     * Get the discipline associated with the specified code.
     * @param code the code value representing a particular discipline
     * @return the associated {@link Discipline}
     */
    private static Discipline getDiscipline(int code)
    {
        switch (code)
        {
            case 0:
                return Discipline.METEOROLOGICAL;
            case 1:
                return Discipline.HYDROLOGICAL;
            case 2:
                return Discipline.LAND_SURFACE;
            case 3:
            case 4:
                return Discipline.SPACE;
            case 10:
                return Discipline.OCEANOGRAPHIC;
            default:
                return null;
        }
    }

    public Discipline getDiscipline()
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
        byte code[] = new byte[4];
        
        while (in.available() > 0)
        {
            in.mark(4);
            in.read(code);
            if (Arrays.equals(code, "GRIB".getBytes()))
            {
                in.reset();
                return;
            }
            in.reset();
            in.read(1);     // skip 1 byte
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


