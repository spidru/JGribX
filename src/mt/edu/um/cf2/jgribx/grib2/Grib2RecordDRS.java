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
import mt.edu.um.cf2.jgribx.Bytes2Number;
import mt.edu.um.cf2.jgribx.GribCodes.DataRepresentation;
import mt.edu.um.cf2.jgribx.GribInputStream;
import mt.edu.um.cf2.jgribx.grib2.Grib2RecordPDS;
import static mt.edu.um.cf2.jgribx.Bytes2Number.FLOAT_IEEE754;
import static mt.edu.um.cf2.jgribx.Bytes2Number.INT_SM;
import mt.edu.um.cf2.jgribx.Logger;
import mt.edu.um.cf2.jgribx.NotSupportedException;

/**
 *
 * @author AVLAB-USER3
 */
public class Grib2RecordDRS
{
    protected int binaryScaleFactor;
    protected int decimalScaleFactor;
    protected int spatialDescriptorOctets;        // Number of octets required in the data section to specify extra descriptors needed for spatial differencing
    /**
     * Length increment for the group lengths (see Note 14)
     */
    protected int groupLengthIncrement;
    /**
     * Number of bits used for the group widths (after the reference value in octet 36 has been removed)
     */
    protected int groupWidthBits;
    /**
     * True length of last group
     */
    protected int lastGroupLength;
    protected int length;
    protected float missingValue;
    protected int missingValueManagement;
    protected int nBits;
    protected int nBitsScaledGroupLengths;
    protected int nDataPoints;
    protected int nGroups;
    protected int refGroupLengths;
    protected int refGroupWidths;
    protected float refValue;
    protected int spatialDiffOrder;
    
    public static Grib2RecordDRS readFromStream(GribInputStream in) throws IOException, NotSupportedException
    {
        Grib2RecordDRS drs = new Grib2RecordDRS();
        drs.length = in.readUINT(4);
        int section = in.readUINT(1);
        if (section != 5)
        {
            Logger.println("DRS contains an incorrect section number", Logger.ERROR);
            return null;
        }
        drs.nDataPoints = in.readUINT(4);
        
        /* [10-11] Data representation template number */
        int packingType = in.readUINT(2);
        switch (packingType)
        {
            case 3:
                /* Grid Point Data - Complex Packing and Spatial Differencing */
                drs.refValue = in.readFloat(4, FLOAT_IEEE754);
                drs.binaryScaleFactor = in.readINT(2, INT_SM);
                drs.decimalScaleFactor = in.readINT(2, INT_SM);
                drs.nBits = in.readUINT(1);
                int type = in.readUINT(1);
                int splitMethod = in.readUINT(1);
                drs.missingValueManagement = in.readUINT(1);
                int missing1 = in.readUINT(4);
                int missing2 = in.readUINT(4);
                drs.nGroups = in.readUINT(4);
                drs.refGroupWidths = in.readUINT(1);
                drs.groupWidthBits = in.readUINT(1);
                drs.refGroupLengths = in.readUINT(4);
                drs.groupLengthIncrement = in.readUINT(1);
                drs.lastGroupLength = in.readUINT(4);
                drs.nBitsScaledGroupLengths = in.readUINT(1);
                drs.spatialDiffOrder = in.readUINT(1);
                drs.spatialDescriptorOctets = in.readUINT(1);
                
                // get missing value
                switch (drs.missingValueManagement)
                {
                    case 0:
                        drs.missingValue = Float.NaN;
                        break;
                    case 1:
                        drs.missingValue = missing1;
                        break;
                    case 2:
                        drs.missingValue = missing2;  // FIXME not sure about this
                        break;
                    default:
                        break;
                }
                break;
            default:
                throw new NotSupportedException("Data Representation type "+packingType+" not supported");
        }
        return drs;
    }
}
