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
import java.util.Calendar;
import mt.edu.um.cf2.jgribx.GribInputStream;
import mt.edu.um.cf2.jgribx.GribRecord;
import mt.edu.um.cf2.jgribx.GribRecordIS;
import mt.edu.um.cf2.jgribx.Logger;
import mt.edu.um.cf2.jgribx.NoValidGribException;
import mt.edu.um.cf2.jgribx.NotSupportedException;


/**
 * A class representing a single GRIB record. A record consists of five sections:
 * indicator section (IS), product definition section (PDS), grid definition section
 * (GDS), bitmap section (BMS) and binary data section (BDS). The sections can be
 * obtained using the getIS, getPDS, ... methods.<p>
 *
 * @author  Benjamin Stark
 * @version 1.0
 */

public class Grib1Record extends GribRecord
{

    /**
     * The indicator section.
     */
    protected GribRecordIS is;

    /**
     * The product definition section.
     */
    protected Grib1RecordPDS pds;

    /**
     * The grid definition section.
     */
    protected Grib1RecordGDS gds;

    /**
     * The bitmap section.
     */
    protected Grib1RecordBMS bms;

    /**
     * The binary data section.
     */
    protected Grib1RecordBDS bds;

    // *** constructors ************************************************************
    public Grib1Record(){}

    /**
     * Constructs a <tt>GribRecord</tt> object from a bit input stream.
     *
     * @param in bit input stream with GRIB record content
     * @param is
     * @return 
     *
     * @throws IOException           if stream can not be opened etc.
     * @throws NotSupportedException 
     * @throws NoValidGribException  if stream contains no valid GRIB file
     */
    public static Grib1Record readFromStream(GribInputStream in, GribRecordIS is) throws IOException,
        NotSupportedException, NoValidGribException
    {
        Grib1Record record = new Grib1Record();
        
        record.is = is;                                 // read Indicator Section
        
        /* Read PDS */
        in.resetBitCounter();
        record.pds = new Grib1RecordPDS(in);
        if (in.getByteCounter() != record.pds.length)
            throw new NoValidGribException("Incorrect PDS length");

        if (record.pds.gdsExists())
        {
            in.resetBitCounter();
            record.gds = Grib1RecordGDS.readFromStream(in);
            if (in.getByteCounter() != record.gds.length)
                throw new NoValidGribException("Incorrect GDS length"); 
        }
        else
        {
           throw new NoValidGribException("GribRecord: No GDS included.");
        }

        if (record.pds.bmsExists())
        {
            in.resetBitCounter();
            record.bms = new Grib1RecordBMS(in);     // read Bitmap Section
            if (in.getByteCounter() != record.bms.getLength())
                throw new NoValidGribException("Incorrect BMS length");
        }

        /* Read BDS */
        in.resetBitCounter();
        record.bds = new Grib1RecordBDS(in, record.pds.getDecimalScale(), record.bms);
        if (in.getByteCounter() != record.bds.length)
            throw new NoValidGribException("Incorrect BDS length");

        // number of values
        // rdg - added the check for a constant field - otherwise this fails
        if (!(record.bds.getIsConstant()) &&
            record.bds.getValues().length != record.gds.getGridNX() * record.gds.getGridNY())
        {
           Logger.println("Grid should contain " +
                 record.gds.getGridNX() + " * " + record.gds.getGridNY() + " = " +
                 record.gds.getGridNX() * record.gds.getGridNY() + " values.",
                   Logger.ERROR);
           Logger.println("But BDS section delivers only " +
                 record.bds.getValues().length + ".",
                   Logger.ERROR);
        }
      
      return record;
   }

    // *** public methods ******************************************************
    /**
     * Get the bitmap section of this GRIB record.
     *
     * @return bitmap section object
     */
    public Grib1RecordBMS getBMS()
    {
       return this.bms;
    }
    
    @Override
    public int getCentreId()
    {
        return pds.getCentreId();
    }
    
    @Override
    public Calendar getForecastTime()
    {
        return this.pds.forecastTime;
    }
    
    /**
     * Get the grid definition section of this GRIB record.
     *
     * @return grid definition section object
     */
    public Grib1RecordGDS getGDS()
    {
       return this.gds;
    }
    
    /**
     * Get the indicator section of this GRIB record.
     *
     * @return indicator section object
     */
    @Override
    public GribRecordIS getIS()
    {
       return this.is;
    }
    
    /**
     * Get the byte recordLength of this GRIB record.
     *
     * @return recordLength in bytes of GRIB record
     */
    public long getLength()
    {
       return is.getRecordLength();
    }
    
    /**
     * Get the parameter type of this GRIB record.
     *
     * @return name of parameter
     */
    @Override
    public String getParameterCode()
    {
       return this.pds.getParameterAbbreviation();
    }
    
    /**
     * Get a more detailed description of the parameter.
     *
     * @return description of parameter
     */
    @Override
    public String getParameterDescription()
    {
        return pds.getParameterDescription();
    }

    /**
     * Get the product definition section of this GRIB record.
     *
     * @return product definition section object
     */
    public Grib1RecordPDS getPDS()
    {
       return this.pds;
    }
    
    /**
     * Returns the ID corresponding to the generating process.
     * @return the ID corresponding to the generating process
     */
    @Override
    public int getProcessId()
    {
        return pds.getProcessId();
    }

    /**
     * Get the binary data section of this GRIB record.
     *
     * @return binary data section object
     */
    public Grib1RecordBDS getBDS()
    {
       return this.bds;
    }

    /**
     * Get grid coordinates in longitude/latitude
     * @return longitide/latituide as doubles
     */
    public double[] getGridCoords()
    {
       return gds.getGridCoords();
    }


    /**
     * Get data/parameter values as an array of float.
     *
     * @return  array of parameter values
     */
    public float[] getValues()
    {
       if (!(bds.getIsConstant())){
          return bds.getValues();
       }
       int gridSize = gds.getGridNX()*gds.getGridNY();
       float[] values = new float[gridSize];
       float ref = bds.getReferenceValue();
       for (int i = 0; i < gridSize;i++){
             values[i] = ref;
       }
       return values;
    }

    /**
     * Get a single value from the BDS using i/x, j/y index.
     *
     * Retrieves using a row major indexing.
     * @param i 
     * @param j 
     *
     * @return  array of parameter values
     * @throws NoValidGribException 
     */
    public float getValue(int i, int j) throws NoValidGribException
    {
       if (i >= 0 && i < gds.getGridNX() && j >= 0 && j < gds.getGridNY()){
          return bds.getValue(gds.getGridNX()*j + i);
       }
       throw new NoValidGribException ("GribRecord:  Array index out of bounds");
    }

     @Override
     public double getValue(double latitude, double longitude)
     {
         double value = Double.NaN;
         int i = (int) Math.round((longitude - gds.getGridLon1()) / gds.getGridDX());
         int j = (int) Math.round((latitude - gds.getGridLat1()) / gds.getGridDY());

         try
         {
             if ((gds.grid_scan & 0x20) != 0x20)
             {
                 // Adjacent points in i direction are consecutive
                 value = bds.getValue(gds.grid_nx*j + i);
             }
             else
             {
                 value = bds.getValue(gds.grid_ny*i + j);
             }
         }
         catch (NoValidGribException e)
         {
             Logger.println("Cannot find a value for the given lat-long", Logger.ERROR);
         }

         return value;
     }

    /**
     * Get the unit for the parameter.
     *
     * @return name of unit
     */
    public String getUnit()
    {
       return pds.getParameterUnits();
    }


    /**
     * Get the level code
     *
     * @return level code
     */
    @Override
    public String getLevelCode()
    {
       return pds.level.getCode();
    }

     @Override
    public String getLevelDescription()
    {
        return pds.level.getDescription();
    }
    
    @Override
    public String getLevelIdentifier()
    {
        return pds.level.getIdentifier();
    }
    
    @Override
    public float[] getLevelValues()
    {
        return pds.level.getValues();
    }

     /**
      * Get the analysis or forecast time of this GRIB record.
      *
      * @return analysis or forecast time
      */
     @Override
     public Calendar getReferenceTime()
     {
        return this.pds.getReferenceTime();
     }

     /**
      * Get a string representation of this GRIB record.
      *
      * @return string representation of this GRIB record
      */
     @Override
     public String toString()
     {
        // combine string representations of subsections
        return "GRIB record:\n" +
              this.is + "\n" +
              this.pds + "\n" +
              (this.pds.gdsExists() ? this.gds.toString() + "\n" : "") +
              (this.pds.bmsExists() ? this.bms.toString() + "\n" : "") +
              this.bds;
     }
}


