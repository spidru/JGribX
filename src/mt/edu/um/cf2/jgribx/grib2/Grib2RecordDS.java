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
import static mt.edu.um.cf2.jgribx.Bytes2Number.INT_SM;
import mt.edu.um.cf2.jgribx.GribInputStream;
import mt.edu.um.cf2.jgribx.NotSupportedException;
import mt.edu.um.cf2.jgribx.grib2.Grib2RecordBMS.Indicator;

/**
 *
 * @author AVLAB-USER3
 */
public class Grib2RecordDS
{
    protected int length;
    protected float data[]; 
    
    public static Grib2RecordDS readFromStream(GribInputStream in, Grib2RecordDRS drs, Grib2RecordGDS gds, Grib2RecordBMS bms) throws IOException, NotSupportedException
    {
        Grib2RecordDS ds = new Grib2RecordDS();
        
        ds.length = in.readUINT(4);
        int section = in.readUINT(1);
        if (section != 7)
        {
            System.err.println("Invalid DS");
            return null;
        }
        
        float DD = (float) Math.pow(10, drs.decimalScaleFactor);
        float R = drs.refValue;
        float EE = (float) Math.pow(2, drs.binaryScaleFactor);
        float refVal = R / DD;
        int NG = drs.nGroups;
        if (NG == 0)
            System.err.println("Zero groups not supported yet");
        int os = drs.spatialDiffOrder;
        int descriptorOctets = drs.spatialDescriptorOctets;
        int ival1 = 0;
        int ival2 = 0;
        int minsd = 0;
        if (descriptorOctets > 0)        
        {
            // first order spatial differencing g1 and gMin
            ival1 = in.readINT(descriptorOctets, INT_SM);       
            
            if (os == 2)
            {
                // second order spatial differencing h1, h2, hMin
                ival2 = in.readINT(descriptorOctets, INT_SM);
            }
            
            minsd = in.readINT(descriptorOctets, INT_SM);
        }
        else
        {
            float data[] = new float[gds.getNumberOfDataPoints()];
            for (int i = 0; i < data.length; i++)
            {
                data[i] = drs.missingValue;
            }
            ds.data = data;
            return ds;
        }
        
        // Get reference values for groups
        ///////////////////////////////////////
        /////// TESTING ONLY
//        in.seekNextByte();
//        int y1;
//        while ((y1 = in.readUINT(1)) == 0)
//            in.mark(1);
//        int y2 = in.readUINT(1);
//        System.out.println("Values: "+y1+" "+y2);
//        in.reset();
//        int y3 = (int) in.readUBits(1);
//        in.seekNextByte();
//        int y4 = (int) in.readUBits(4);
        ///////////////////////////////////////
        int X1[] = new int[NG];
        if (drs.nBits != 0)
        {
            in.seekNextByte();
            for (int i = 0; i < NG; i++)
            {
                X1[i] = (int) in.readUBits(drs.nBits);
            }
        }
        
        // Get number of bits used to encode each group
        int NB[] = new int[NG];     // initialised to zero
        if (drs.groupWidthBits != 0)
        {
            in.seekNextByte();
            for (int i = 0; i < NG; i++)
            {
                NB[i] = (int) in.readUBits(drs.groupWidthBits);
                NB[i] += drs.refGroupWidths;
            }
        }
        
        // Get the scaled group lengths
        int L[] = new int[NG];
        if (drs.nBitsScaledGroupLengths != 0)
        {
            in.seekNextByte();
            for (int i = 0; i < NG; i++)
            {
                L[i] = (int) in.readUBits(drs.nBitsScaledGroupLengths);
            }
        }
        
        int totalL = 0;
        for (int i = 0; i < NG; i++)
        {
            L[i] = L[i] * drs.groupLengthIncrement + drs.refGroupLengths;
            totalL += L[i];
        }
        totalL -= L[NG-1];
        totalL += drs.lastGroupLength;
        L[NG-1] = drs.lastGroupLength;
        
        // test
        if (drs.missingValueManagement != 0)
        {
            throw new NotSupportedException("Missing Value Management is not supported");
        }
        else
        {
            if (totalL != drs.nDataPoints)
            {
                System.out.println("nPoints != drs.nPoints: "+totalL+" != "+drs.nDataPoints);
                float data[] = new float[drs.nDataPoints];
                for (int i = 0; i < drs.nDataPoints; i++)
                {
                    data[i] = drs.missingValue;
                }
                ds.data = data;
                return ds;
            }
        }
        
        float data[] = new float[gds.getNumberOfDataPoints()];
        
        // Get X2 values and calculate the results Y using the formula:
        //  Y = (R + (X1 + X2) * (2^E)) / (10^D)
        int count = 0;
        int dataSize = 0;
        boolean dataBitMap[] = null;
        in.seekNextByte();
        if (drs.missingValueManagement == 0)
        {
            for (int i = 0; i < NG; i++)
            {
                if (NB[i] != 0)
                {
                    for (int j = 0; j < L[i]; j++)
                    {
                        data[count++] = (int) in.readUBits(NB[i]) + X1[i];
                    }
                }
                else
                {
                    for (int j = 0; j < L[i]; j++)
                    {
                        data[count++] = X1[i];
                    }
                }
            }
        }
        else if (drs.missingValueManagement == 1 || drs.missingValueManagement == 2)
        {
            dataBitMap = new boolean[gds.getNumberOfDataPoints()];
            for (int i = 0; i < NG; i++)
            {
                if (NB[i] != 0)
                {
                    int msng1 = (int) Math.pow(2, NB[i]) - 1;
                    int msng2 = msng1 - 1;
                    for (int j = 0; j < L[i]; j++)
                    {
                        data[count] = (int) in.readUBits(NB[i]);
                        if (data[count] == msng1 || (drs.missingValueManagement == 2 && data[count] == msng2))
                        {
                            dataBitMap[count] = false;
                        }
                        else
                        {
                            dataBitMap[count] = true;
                            data[dataSize++] = data[count] + X1[i];
                        }
                        count++;
                    }
                }
                else
                {
                    int msng1 = (int) Math.pow(2, drs.nBits);
                    int msng2 = msng1 - 1;
                    if (X1[i] == msng1)
                    {
                        for (int j = 0; j < L[i]; j++)
                        {
                            dataBitMap[count++] = false;
                        }
                    }
                    else if (drs.missingValueManagement == 2 && X1[i] == msng2)
                    {
                        for (int j = 0; j < L[i]; j++)
                        {
                            dataBitMap[count++] = false;
                        }
                    }
                    else
                    {
                        for (int j = 0; j < L[i]; j++)
                        {
                            dataBitMap[count] = true;
                            data[dataSize++] = X1[i];
                            count++;
                        }
                    }
                }
            }
        }
        in.seekNextByte();
        
        // first order spatial differencing
        if (os == 1)
        {
            data[0] = ival1;
            int itemp;
            if (drs.missingValueManagement == 0)
            {
                itemp = gds.getNumberOfDataPoints();
            }
            else
            {
                itemp = dataSize;
            }
            for (int i = 1; i < itemp; i++)
            {
                data[i] += minsd;
                data[i] = data[i] + data[i-1];
            }
        }
        else if (os == 2)
        {
            data[0] = ival1;
            data[1] = ival2;
            int itemp;
            if (drs.missingValueManagement == 0)
            {
                itemp = gds.getNumberOfDataPoints();
            }
            else
            {
                itemp = dataSize;
            }
            for (int i = 2; i < itemp; i++)
            {
                data[i] += minsd;
                data[i] = data[i] + (2*data[i-1]) - data[i-2];
            }
        }
        
        if (drs.missingValueManagement == 0)
        {
            for (int i = 0; i < data.length; i++)
            {
                data[i] = (R + (data[i] * EE)) / DD;
            }
        }
        else if (drs.missingValueManagement == 1 || drs.missingValueManagement == 2)
        {
            int count2 = 0;
            float tmp[] = new float[gds.getNumberOfDataPoints()];
            for (int i = 0; i < data.length; i++)
            {
                if (dataBitMap[i])
                {
                    tmp[i] = (R + (data[count2++] * EE)) / DD;
                }
                else
                {
                    tmp[i] = drs.missingValue;
                }
            }
            data = tmp;
        }
        
        // bit map is used
        if (bms.indicator != Indicator.BITMAP_NONE)
        {
            System.err.println("Bitmap not supported yet");
            int idx = 0;
            float tmp[] = new float[gds.getNumberOfDataPoints()];
            for (int i = 0; i < gds.getNumberOfDataPoints(); i++)
            {
//                if ((bitmap[i/8] & ))
            }
        }
        
        ds.data = data;
        return ds;
    }
    
    
}
