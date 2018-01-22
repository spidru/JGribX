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

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import mt.edu.um.cf2.jgribx.grib1.Grib1Record;
import mt.edu.um.cf2.jgribx.grib2.Grib2Record;

public abstract class GribRecord
{
    private GribRecordIS is;
    
    public static GribRecord readFromStream(GribInputStream in) throws IOException, NotSupportedException, NoValidGribException
    {
        GribRecord record = null;
        
        GribRecordIS is = GribRecordIS.readFromStream(in);
        if (is == null) return null;
        
        switch (is.getGribEdition())
        {
            case 1:
                record = Grib1Record.readFromStream(in, is);
                break;
            case 2:
                record = Grib2Record.readFromStream(in, is);
                break;
            default:
                throw new NoValidGribException("Unsupported GRIB edition "+is.getGribEdition());
        }
        
        GribRecordES es = new GribRecordES(in);
        if (!es.isValid)
        {
            throw new NoValidGribException("Grib End Section is invalid");
        }
        
        if (record != null)
        {
            record.is = is;
        }
        return record;
    }
    
    public GribRecordIS getIS()
    {
        return is;
    }
    
    /**
     * Returns the ID corresponding to the originating centre.
     * @return the ID corresponding to the originating centre
     */
    public abstract int getCentreId();
    public abstract Calendar getForecastTime();
    public abstract String getLevelCode();
    public abstract String getLevelDescription();
    
    /**
     * Returns the unique ID for the level code and value combination.
     * @return 
     */
    public abstract String getLevelIdentifier();
    public abstract float[] getLevelValues();
    public abstract String getParameterCode();
    public abstract String getParameterDescription();
    
    /**
     * Returns the ID corresponding to the generating process.
     * @return the ID corresponding to the generating process
     */
    public abstract int getProcessId();
    public abstract Calendar getReferenceTime();
    public abstract double getValue(double latitude, double longitude);
}
