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
import mt.edu.um.cf2.jgribx.NotSupportedException;


/**
 * A class representing the binary data section (BDS) of a GRIB record.
 *
 * @author  Benjamin Stark
 * @version 1.0
 */

public class Grib1RecordBDS
{

   /**
    * Constant value for an undefined grid value.
    */
   public static final float UNDEFINED = 99999e20f;

   /**
    * Length in bytes of this BDS.
    */
   protected int length;

   /**
    * Binary scale factor.
    */
   protected int binscale;

   /**
    * Reference value, the base for all parameter values.
    */
   protected float refvalue;

   /**
    * Number of bits per value.
    */
   protected int numbits;

   /**
    * Array of parameter values.
    */
   protected float[] values;

   /**
    * Minimal parameter value in grid.
    */
   protected float minvalue = Float.MAX_VALUE;

   /**
    * Maximal parameter value in grid.
    */
   protected float maxvalue = -Float.MAX_VALUE;

   /**
    * rdg - added this to prevent a divide by zero error if variable data empty
    *
    * Indicates whether the BMS is represented by a single value
    *   -  Octet 12 is empty, and the data is represented by the reference value.
    */
   protected boolean isConstant = false;


   // *** constructors *******************************************************

   /**
    * Constructs a <tt>GribRecordBDS</tt> object from a bit input stream.
    * A bit map which indicates grid points where no parameter value is
    * defined is not available.
    *
    * @param in - bit input stream with BDS content
    * @param gds Grid Definition Section of record
    * @param pds Product Definition Section of record
    *
    * @throws IOException If stream can not be opened etc.
    * @throws NotSupportedException If a required feature is not supported
    */
   public Grib1RecordBDS(GribInputStream in, Grib1RecordGDS gds, Grib1RecordPDS pds)
           throws IOException, NotSupportedException
   {
      this(in, null, gds, pds);
   }


   /**
    * Constructs a <tt>GribRecordBDS</tt> object from a bit input stream.
    * A bit map indicates the grid points where no parameter value is defined.
    *
    * @param in Bit input stream containing BDS content
    * @param bms Bit Map Section of GRIB record
    * @param gds Grid Definition Section of record
    * @param pds Product Definition Section of record
    *
    * @throws IOException If stream can not be opened etc.
    * @throws NotSupportedException If a required feature is not supported
    */
   public Grib1RecordBDS(GribInputStream in, Grib1RecordBMS bms, Grib1RecordGDS gds, Grib1RecordPDS pds)
           throws IOException, NotSupportedException
   {
      byte[] octets = new byte[11];
      int unusedBits;

      /* Extract required information from PDS */
      int decimalScale = pds.decscale;

      in.read(octets);

      /* octets 1-3 (section length) */
      length = Bytes2Number.bytesToUint(Arrays.copyOfRange(octets, 0, 3));

      // octet 4, 1st half (packing flag)
      int flag = octets[3];
      if ((flag & 240) != 0)
      {
         throw new NotSupportedException("GribRecordBDS: No other flag " +
                 "(octet 4, 1st half) than 0 (= simple packed floats as " +
                 "grid point data) supported yet in BDS section.");
      }

      // octet 4, 2nd half (number of unused bits at end of this section)
      unusedBits = flag & 15;

      // octets 5-6 (binary scale factor)
      binscale = Bytes2Number.bytesToInt(Arrays.copyOfRange(octets, 4, 6), Bytes2Number.INT_SM);

      // octets 7-10 (reference point = minimum value)
      refvalue = Bytes2Number.bytesToFloat(Arrays.copyOfRange(octets, 6, 10), Bytes2Number.FLOAT_IBM);

      // octet 11 (number of bits per value)
      numbits = Bytes2Number.bytesToUint(octets[10]);
      isConstant = (numbits == 0);

      // *** read values ************************************************************

      float ref = (float) (Math.pow(10.0, -decimalScale) * this.refvalue);
      float scale = (float) (Math.pow(10.0, -decimalScale) * Math.pow(2.0, this.binscale));

      if (bms != null)
      {
         boolean[] bitmap = bms.getBitmap();

         this.values = new float[bitmap.length];
         for (int i = 0; i < bitmap.length; i++)
         {
            if (bitmap[i])
            {
               if (!isConstant){
                  this.values[i] = ref + scale * in.readUBits(this.numbits);
                  if (this.values[i] > this.maxvalue)
                     this.maxvalue = this.values[i];
                  if (this.values[i] < this.minvalue)
                     this.minvalue = this.values[i];
               }else{// rdg - added this to handle a constant valued parameter
                  this.values[i] = ref;
               }
            }
            else
               this.values[i] = Grib1RecordBDS.UNDEFINED;
         }
      }
      else
      {
         if (!isConstant){
            this.values = new float[((this.length - 11) * 8 - unusedBits) / this.numbits];

            for (int i = 0; i < values.length; i++)
            {
               this.values[i] = ref + scale * in.readUBits(this.numbits);

               if (this.values[i] > this.maxvalue)
                  this.maxvalue = this.values[i];
               if (this.values[i] < this.minvalue)
                  this.minvalue = this.values[i];
            }
         }
         else
         {
            // constant valued - same min and max
            values = new float[gds.grid_nx * gds.grid_ny];
            Arrays.fill(values, ref);
         }
      }
      in.seekNextByte();
      in.skip(unusedBits / 8);
   }


   // *** public methods *********************************************************

   /**
    * Get the length in bytes of this section.
    *
    * @return length in bytes of this section
    */
   public int getLength()
   {

      return this.length;
   }


   /**
    * Get the binary scale factor.
    *
    * @return binary scale factor
    */
   public int getBinaryScale()
   {

      return this.binscale;
   }

   /**
    * Get whether this BDS is single valued
    *
    * @return isConstant
    */
   public boolean getIsConstant()
   {
      return this.isConstant;
   }

   /**
    * Get the reference value all data values are based on.
    *
    * @return reference value
    */
   public float getReferenceValue()
   {
      return this.refvalue;
   }


   /**
    * Get number of bits used per parameter value.
    *
    * @return number of bits used per parameter value
    */
   public int getNumBits()
   {
      return this.numbits;
   }


   /**
    * Get data/parameter values as an array of float.
    *
    * @return  array of parameter values
    */
   public float[] getValues()
   {
      return this.values;
   }

   /**
    * Get data/parameter value as a float.
    * @param index
    *
    * @return  array of parameter values
    * @throws NoValidGribException
    */
   public float getValue(int index) throws NoValidGribException
   {
      if (index >=0 && index < values.length){
         return this.values[index];
      }
      throw new NoValidGribException("GribRecordBDS: Array index out of bounds");
   }


   /**
    * Get minimum value
    * @return mimimum value
    */
   public float getMinValue()
   {
      return minvalue;
   }

   /**
    * Get maximum value
    * @return maximum value
    */
   public float getMaxValue()
   {
      return maxvalue;
   }


   /**
    * Get a string representation of this BDS.
    *
    * @return string representation of this BDS
    */
   @Override
   public String toString()
   {
      return "    BDS section:" + '\n' +
              "        min/max value: " + this.minvalue + " " + this.maxvalue + "\n" +
              "        ref. value: " + this.refvalue + "\n" +
              "        is a constant: " + this.isConstant + "\n" +
              "        bin. scale: " + this.binscale + "\n" +
              "        num bits: " + this.numbits;
   }

}

