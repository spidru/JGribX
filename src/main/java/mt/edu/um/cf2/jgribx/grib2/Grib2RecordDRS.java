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

import mt.edu.um.cf2.jgribx.*;
import mt.edu.um.cf2.jgribx.GribCodes.DataRepresentation;
import mt.edu.um.cf2.jgribx.grib2.Grib2RecordPDS;
import static mt.edu.um.cf2.jgribx.Bytes2Number.FLOAT_IEEE754;
import static mt.edu.um.cf2.jgribx.Bytes2Number.INT_SM;

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
    protected int originalFieldValuesType;
    protected int packingType;
    protected CompressionType compressionType;
    protected int compressionRatio;
    
    public static Grib2RecordDRS readFromStream(GribInputStream in) throws IOException, NotSupportedException, NoValidGribException {
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
        drs.packingType = in.readUINT(2);
        switch (drs.packingType)
        {
            case 0:
                /* Grid Point Data - Simple Packing */
                drs.refValue = in.readFloat(4, FLOAT_IEEE754);
                drs.binaryScaleFactor = in.readINT(2, INT_SM);
                drs.decimalScaleFactor = in.readINT(2, INT_SM);
                drs.nBits = in.readUINT(1);
                drs.originalFieldValuesType = in.readUINT(1);
                break;
            case 3:
                /* Grid Point Data - Complex Packing and Spatial Differencing */
                drs.refValue = in.readFloat(4, FLOAT_IEEE754);
                drs.binaryScaleFactor = in.readINT(2, INT_SM);
                drs.decimalScaleFactor = in.readINT(2, INT_SM);
                drs.nBits = in.readUINT(1);
                drs.originalFieldValuesType = in.readUINT(1);
                int splitMethod = in.readUINT(1);
                drs.missingValueManagement = in.readUINT(1);
                int primaryMissingValue = in.readUINT(4);
                int secondaryMissingValue = in.readUINT(4);
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
                        drs.missingValue = primaryMissingValue;
                        break;
                    case 2:
                        drs.missingValue = secondaryMissingValue;
                        break;
                    default:
                        Logger.println("Unexpected value for missingValueManagement", Logger.WARNING);
                        drs.missingValue = Float.NaN;
                        break;
                }
                break;
            case 4:
    			/* Grid point data - IEEE floating point data, more at :
    			 * https://codes.ecmwf.int/grib/format/grib2/templates/5/4/
    			 * and
    			 * https://codes.ecmwf.int/grib/format/grib2/ctables/5/7/
    			 */

    			// [12] Precision
    			int precision = in.readUINT(1);
    			drs.missingValue = Float.NaN;
    			
    			switch (precision)
    			{
    			case 1:
    				drs.nBits = 32;
    				break;
    			case 2:
    				drs.nBits = 64;
    				break;
    			case 3:
    				drs.nBits = 128;
    				break;
    			default:
    				throw new NotSupportedException("The precision code "+precision+" of floating-point numbers in "
    						+ drs.packingType +" is not supported");
    			}
    			break;
            case 40:
                /* Grid Point Data - JPEG 2000 code stream format */
                drs.refValue = in.readFloat(4, FLOAT_IEEE754);
                drs.binaryScaleFactor = in.readINT(2, INT_SM);
                drs.decimalScaleFactor = in.readINT(2, INT_SM);
                drs.nBits = in.readUINT(1);
                if (drs.nBits == 0 || drs.nBits > 38)
                {
                    throw new NoValidGribException("JPEG 2000 bit depth must be in the range [1, 38] (found: " + drs.nBits + ")");
                }
                drs.originalFieldValuesType = in.readUINT(1);
                drs.compressionType = getCompressionType(in.readUINT(1));
                drs.compressionRatio = in.readUINT(1);
                if (drs.compressionType != CompressionType.LOSSY && drs.compressionRatio != GribCodes.MISSING)
                {
                    throw new NoValidGribException("JPEG 2000 compression ratio should be set to missing if compression type is not lossy");
                }
                break;
            default:
                throw new NotSupportedException("Data Representation type "+ drs.packingType +" not supported");
        }
        return drs;
    }

    private enum CompressionType
    {
        LOSSLESS,
        LOSSY,
        RESERVED,
        MISSING
    }

    private static CompressionType getCompressionType(int code)
    {
        if (code == 0) return CompressionType.LOSSLESS;
        else if (code == 1) return CompressionType.LOSSY;
        else if (code >= 2 && code <= 254) return CompressionType.RESERVED;
        else return CompressionType.MISSING;
    }
}
