/*
 * ============================================================================
 * JGribX
 * ============================================================================
 * Written by Andrew Spiteri <andrew.spiteri@um.edu.mt>
 * 
 * Licensed under MIT (https://github.com/spidru/JGribX/blob/master/LICENSE)
 * ============================================================================
 */
package mt.edu.um.cf2.jgribx;

public class JGribX
{
    private static String RES_PATH = "res/";
    
    /**
    * Defines version of JGribX
    */
    private static final String VERSION = "0.4-190127";
    
    public static void setLoggingLevel(int level)
    {
        Logger.setLevel(level);
    }
    
    public static void setResourcePath(String path)
    {
        if (!path.endsWith("/"))
            path += "/";
        RES_PATH = path;
    }
    
    /**
     * Returns the path to the resource directory.
     * @return the path to the resource directory.
     */
    public static String getResourcePath()
    {
        return RES_PATH;
    }
    
    /**
     * Returns the current version of JGribX
     * @return 
     */
    public static String getVersion()
    {
        return VERSION;
    }
}
