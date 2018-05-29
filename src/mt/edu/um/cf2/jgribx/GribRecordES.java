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
    
    public GribRecordES(GribInputStream in) throws IOException
    {
        byte[] octets = in.read(4);
        String code = new String(octets);
        if (!code.equals("7777"))
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
        int countBytes = 0;
        int code[] = new int[4];
        while (in.available() > 0)
        {
            code[countBytes] = in.readUINT(1);
            if (code[0] == '7' && code[1] == '7' && code[2] == '7' && code[3] == '7')
                return;
            countBytes++;
            if (countBytes == 4) countBytes = 0;
        }
    }
}
