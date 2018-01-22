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
package mt.edu.um.cf2.jgribx.grib2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import mt.edu.um.cf2.jgribx.GribInputStream;
import mt.edu.um.cf2.jgribx.GribRecord;
import mt.edu.um.cf2.jgribx.GribRecordIS;
import mt.edu.um.cf2.jgribx.Logger;
import mt.edu.um.cf2.jgribx.NoValidGribException;
import mt.edu.um.cf2.jgribx.NotSupportedException;
import mt.edu.um.cf2.jgribx.grib2.Grib2RecordGDS.ScanMode;

/**
 *
 * @author AVLAB-USER3
 */
public class Grib2Record extends GribRecord
{
    protected GribRecordIS is;
    protected Grib2RecordIDS ids;
    protected List<Grib2RecordLUS> lusList = new ArrayList();
    protected List<Grib2RecordGDS> gdsList = new ArrayList();
    protected List<Grib2RecordPDS> pdsList = new ArrayList();
    protected List<Grib2RecordDRS> drsList = new ArrayList();
    protected List<Grib2RecordBMS> bmsList = new ArrayList();
    protected List<Grib2RecordDS> dsList = new ArrayList();
    
    public static Grib2Record readFromStream(GribInputStream in, GribRecordIS is) throws IOException, NotSupportedException, NoValidGribException
    {
        Grib2Record record = new Grib2Record();
        long recordLength = is.getRecordLength() - is.getLength();
        
        Grib2RecordDRS drs = null;
        Grib2RecordGDS gds = null;
        Grib2RecordBMS bms = null;
        while (recordLength > 4)
        {
            int section;
            if (recordLength == 4)
            {
                break;
            }
            in.mark(10);
            int sectionLength = in.readUINT(4);
            if (sectionLength > recordLength)
            {
                Logger.println("Section appears to be larger than the remaining length in the record.", Logger.ERROR);
            }
            section = in.readUINT(1);
            in.reset();
            in.resetBitCounter();
            
            switch (section)
            {
                case 1:
                    record.ids = Grib2RecordIDS.readFromStream(in);
                    break;
                case 2:
                    break;
                case 3:
                    gds = Grib2RecordGDS.readFromStream(in);
                    record.gdsList.add(gds);
                    break;
                case 4:
                    record.pdsList.add(new Grib2RecordPDS(in, is.getDiscipline(), record.ids.referenceTime));
                    break;
                case 5:
                    drs = Grib2RecordDRS.readFromStream(in);
                    record.drsList.add(drs);
                    break;
                case 6:
                    bms = Grib2RecordBMS.readFromStream(in);
                    record.bmsList.add(bms);
                    break;
                case 7:
                    record.dsList.add(Grib2RecordDS.readFromStream(in, drs, gds, bms));
                    break;
                default:
                    throw new NoValidGribException("Invalid section encountered");
            }
            if (in.getByteCounter() != sectionLength)
            {
                Logger.println("Length of Section "+section+" does not match actual amount of bytes read", Logger.ERROR);
            }
            recordLength -= sectionLength;
        }
        return record;
    }
    
    @Override
    public int getCentreId()
    {
        return ids.getCentreId();
    }
    
    @Override
    public Calendar getForecastTime()
    {
        if (pdsList.size() > 1)
            Logger.println("Record contains multiple PDS's", Logger.WARNING);
        return pdsList.get(0).getForecastTime();
    }
    
    @Override
    public String getLevelCode()
    {
        if (pdsList.size() > 1)
            Logger.println("Record contains multiple PDS's", Logger.WARNING);
        return pdsList.get(0).getLevelCode();
    }
    
    @Override
    public String getLevelDescription()
    {
        if (pdsList.size() > 1)
            Logger.println("Record contains multiple PDS's", Logger.WARNING);
        return pdsList.get(0).getLevelDescription();
    }
    
    @Override
    public String getLevelIdentifier()
    {
        if (pdsList.size() > 1)
            Logger.println("Record contains multiple PDS's", Logger.WARNING);
        return pdsList.get(0).getLevelIdentifier();
    }
    
    @Override
    public float[] getLevelValues()
    {
        if (pdsList.size() > 1)
            Logger.println("Record contains multiple PDS's", Logger.WARNING);
        return pdsList.get(0).getLevel().getValues();
    }
       
    @Override
    public String getParameterCode()
    {
        if (pdsList.size() > 1)
            Logger.println("Record contains multiple PDS's", Logger.WARNING);
        return pdsList.get(0).getParameterAbbrev();
    }
    
    @Override
    public String getParameterDescription()
    {
        if (pdsList.size() > 1)
            Logger.println("Record contains multiple PDS's", Logger.WARNING);
        return pdsList.get(0).getParameterDescription();
    }
    
    @Override
    public int getProcessId()
    {
        if (pdsList.size() > 1)
            Logger.println("Record contains multiple PDS's", Logger.WARNING);
        return pdsList.get(0).getProcessId();
    }
    
    @Override
    public Calendar getReferenceTime()
    {
        return ids.referenceTime;
    }
    
    @Override
    public double getValue(double latitude, double longitude)
    {
        double value;
        
        if (gdsList.size() > 1)
            Logger.println("Record contains multiple GDS instances", Logger.WARNING);
        Grib2RecordGDS gds = gdsList.get(0);
//        double[] xcoords = gds.getGridXCoords();
//        double[] ycoords = gds.getGridYCoords();
        
        int j = (int) Math.round((latitude - gds.getGridLatStart()) / gds.getGridDeltaY());     // j = index_closest_latitude
        int i = (int) Math.round((longitude - gds.getGridLonStart()) / gds.getGridDeltaX());    // i = index_closest_longitude
        
//        double closest_latitude = ycoords[index_closest_latitude];
//        double closest_longitude = xcoords[index_closest_longitude];
        
        ScanMode scanMode = gds.scanMode;
        
        if (scanMode.iDirectionEvenRowsOffset || scanMode.iDirectionOddRowsOffset || scanMode.jDirectionOffset || !scanMode.rowsNiNjPoints || scanMode.rowsZigzag)
            System.err.println("Unsupported scan mode found");
        
        
        if (scanMode.iDirectionConsecutive)
        {
            value = dsList.get(0).data[gds.gridNi*j + i];
        }
        else
        {
            value = dsList.get(0).data[gds.gridNj*i + j];
        }
        
        return value;
    }
}
