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

/**
 *
 * @author AVLAB-USER3
 */
public class GribRecordES
{
    boolean isValid;
    private static final String PATTERN = "7777";

    public GribRecordES(GribInputStream in) throws IOException
    {
        byte[] octets = in.read(4);
        String code = new String(octets);
        if (!code.equals(PATTERN))
        {
            Logger.println("Record has not ended correctly.", Logger.ERROR);
            isValid = false;
        }
        else
        {
            isValid = true;
        }
    }
    
    public static void seekNext(GribInputStream in) throws IOException
    {
        int nBytes = in.seekBytePattern(PATTERN.getBytes(), true);
        Logger.println("Skipped " + (nBytes - PATTERN.length()) + " bytes to end of record", Logger.INFO);
    }
}
