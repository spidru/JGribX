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

import mt.edu.um.cf2.jgribx.*;
import java.io.IOException;

public abstract class Grib2RecordGDS
{
    protected double lat1;
    protected double lat2;
    protected double lon1;
    protected double lon2;
    protected int length;
    /**
     * Number of data points
     */
    protected int nDataPoints;
    protected double gridDi;
    protected double gridDj;
    protected int gridNi;
    protected int gridNj;
    private int gridType;
    protected int earthShape;
    protected ScanMode scanMode;
    
    protected class ScanMode
    {
        protected boolean iDirectionPositive;
        protected boolean jDirectionPositive;
        protected boolean iDirectionConsecutive;
        protected boolean rowsZigzag;
        protected boolean iDirectionEvenRowsOffset;
        protected boolean iDirectionOddRowsOffset;
        protected boolean jDirectionOffset;
        protected boolean rowsNiNjPoints;
        
        protected ScanMode(byte flags)
        {
            iDirectionPositive = (flags & 0x80) != 0x80;
            jDirectionPositive = (flags & 0x40) == 0x40;
            iDirectionConsecutive = (flags & 0x20) != 0x20;
            rowsZigzag = (flags & 0x10) == 0x10;
            iDirectionEvenRowsOffset = (flags & 0x08) == 0x08;
            iDirectionOddRowsOffset = (flags & 0x04) == 0x04;
            jDirectionOffset = (flags & 0x02) == 0x02;
            rowsNiNjPoints = (flags & 0x01) != 0x01;
        }
    }
    
    /**
     * This constructor initialises a generic {@link Grib2RecordGDS} 
     * by only reading the header data from a given {@link GribInputStream}.
     * @param in
     * @throws IOException 
     */
    public Grib2RecordGDS(GribInputStream in) throws IOException
    {
        // [1-4] Length of section in octets
        length = in.readUINT(4);
        
        // [5] Section number
        in.skip(1);
        
        /* [6] Grid Definition Source */
        int gridSource = in.readUINT(1);
        if (gridSource != 0)
        {
            Logger.println("Unsupported grid definition source", Logger.ERROR);
        }

        /* [7-10] Number of Data Points */
        nDataPoints = in.readUINT(4);

        /* [11] Number of Octets (for optional list of numbers defining number of points) */
        in.skip(1);

        /* [12] Interpretation */
        in.skip(1);

        /* [13-14] Grid Definition Template Number */
        gridType = in.readUINT(2);
        
        /* [15-xx] Grid Definition Template */
        // This part will be processed by constructors of child classes
    }
    
    public static Grib2RecordGDS readFromStream(GribInputStream in) throws IOException, NotSupportedException
    {
        Grib2RecordGDS gds = null;
        
        in.mark(15);
        
        // [1-4] Length of section in octets
        in.skip(4);
        
        // [5] Section number
        int section = in.readUINT(1);
        if (section != 3)
        {
            System.err.println("GDS section number is incorrect");
        }
        
        /* [6] Grid Definition Source */
        in.skip(1);

        /* [7-10] Number of Data Points */
        in.skip(4);

        /* [11] Number of Octets (for optional list of numbers defining number of points) */
        in.skip(1);

        /* [12] Interpretation */
        in.skip(1);

        /* [13-14] Grid Definition Template Number */
        int gridType = in.readUINT(2);
        
        in.reset();     // required since constructors below will read the GDS from the beginning
        
        switch (gridType)
        {
            case 0:
                // Latitude/Longitude (also called Equidistant Cylindrical or Plate Caree)
                gds = new Grib2RecordGDSLatLon(in);
                break;
            default:
                throw new NotSupportedException("Unsupported grid type: "+gridType);
        }
        
        return gds;
    }
    
    protected int getLength()
    {
        return length;
    }
    
    public int getNumberOfDataPoints()
    {
        return nDataPoints;
    }
    
    protected abstract double[][] getGridCoords();
    protected abstract double[] getGridXCoords();
    protected abstract double[] getGridYCoords();
    protected abstract double getGridDeltaX();
    protected abstract double getGridDeltaY();
    protected abstract double getGridLatStart();
    protected abstract double getGridLonStart();
    protected abstract int getGridSizeX();
    protected abstract int getGridSizeY();
}
