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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import mt.edu.um.cf2.jgribx.GribCodes;
import mt.edu.um.cf2.jgribx.GribCodes.Discipline;
import mt.edu.um.cf2.jgribx.Logger;

/**
 *
 * @author AVLAB-USER3
 */
public class Grib2Parameter
{
    private GribCodes.Discipline discipline;
    private GribCodes.ParameterCategory category;
    private int index;
    private String abbrev;
    private String desc;
    private String units;
    
    private static List<Grib2Parameter> paramList = new ArrayList();
    private static boolean defaultLoaded = false;
    
    public Grib2Parameter(Discipline discipline, GribCodes.ParameterCategory category, int index, String abbrev, String desc, String units)
    {
        this.discipline = discipline;
        this.category = category;
        this.index = index;
        this.abbrev = abbrev;
        this.desc = desc;
        this.units = units;
    }
    
    public static void loadDefaultParameters()
    {
        String filename;
        
        for (GribCodes.Discipline discipline : GribCodes.Discipline.values())
        {
            for (GribCodes.ParameterCategory category : GribCodes.ParameterCategory.values())
            {
                filename = "/" + discipline + "-" + category.toString() + ".txt";                
                Logger.println("Resource path: " + filename, Logger.INFO);
                InputStream is = Grib2Parameter.class.getResourceAsStream(filename);
                if (is == null)
                {
                    Logger.println("Cannot find " + filename, Logger.ERROR);
                    break;
                }
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(is)))
                {
                    String line;
                    Pattern pattern = Pattern.compile("(\\d+)\\s*:\\s*(.*?)\\s*:\\s*(.*?)\\s*:\\s*(\\w*)");
                    Matcher m;
                    while ((line = reader.readLine()) != null)
                    {
                        m = pattern.matcher(line);
                        if (m.find())
                        {
                            int index = Integer.parseInt(m.group(1));
                            String paramDesc = m.group(2);
                            String paramUnits = m.group(3);
                            String paramName = m.group(4);
                            paramList.add(new Grib2Parameter(discipline, category, index, paramName, paramDesc, paramUnits));
                        }
                    }
                }
                catch (IOException e)
                {
                    Logger.println("Cannot read " + filename, Logger.ERROR);
                }
            }
        }
        defaultLoaded = true;
    }
    
    public static Grib2Parameter getParameter(Discipline discipline, int category, int index)
    {
        for (Grib2Parameter parameter : paramList)
        {
            if ((parameter.discipline == discipline) &&
                    (parameter.category.ordinal() == category) &&
                    (parameter.index == index)
                )
            {
                return parameter;
            }
        }
        return null;
    }
    
    public static boolean isDefaultLoaded()
    {
        return defaultLoaded;
    }
    
    public String getDescription()
    {
        return desc;
    }
    
    public String getCode()
    {
        return abbrev;
    }
    
    public String getUnits()
    {
        return units;
    }
}
