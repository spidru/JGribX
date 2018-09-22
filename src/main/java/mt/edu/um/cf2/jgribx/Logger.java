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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class Logger
{
    public enum LoggingMode {OFF,CONSOLE,LOCAL,REMOTE};
    private static LoggingMode mode;
    
    private static PrintWriter pw;
    
    /** Designates severe error events that typically lead to application abort. */
    public static final int FATAL   = 0;
    /** Designates error events that might still allow the application to continue running. */
    public static final int ERROR   = 1;
    /** Designates potentially problematic events. */
    public static final int WARNING = 2;
    /** Designates informational messages that highlight the progress of the application. */
    public static final int INFO    = 3;
    /** Designates fine-grained informational events that are mostly useful for debugging. */
    public static final int DEBUG   = 4;
    /** Designates finer-grained informational events that are useful for tracing a problem. */
    public static final int TRACE   = 5;
    
    private static int level;
    
    public static void setLoggingMode(LoggingMode mode)
    {
        Logger.mode = mode;
    }
    
    public static void setLevel(int lvl)
    {
         level = lvl;
    }
    
    public static void print(String msg, int lvl)
    {
        if (lvl > level) return;
        switch (lvl)
        {
            case FATAL:
                msg = "FATAL: " + msg;
                break;
            case ERROR:
                msg = "ERROR: " + msg;
                break;
            case WARNING:
                msg = "WARNING: " + msg;
                break;
            case INFO:
                msg = "INFO: " + msg;
                break;
            case DEBUG:
                msg = "DEBUG: " + msg;
                break;
            case TRACE:
                msg = "TRACE: " + msg;
                break;
            default:
                break;
        }
        switch (mode)
        {
            case OFF:
                break;
            case CONSOLE:
                System.out.print(msg);
                break;
            case LOCAL:
                if (pw == null)
                    pw = openLogFile(new File("log.txt"));
                pw.print(msg);
                break;
            case REMOTE:
                throw new UnsupportedOperationException("The current version of Logger does not yet support remote logging");
        }
    }
    
    public static void println(String msg, int lvl)
    {
        print(msg+"\r\n",lvl);
    }
    
    public static void println(int lvl)
    {
        print("\r\n",lvl);
    }
    
    public static void flush()
    {
        if (pw != null)
            pw.flush();
    }
    
    private static PrintWriter openLogFile(File file)
    {
        PrintWriter pw = null;
        if (!file.exists())
        {
            try
            {
                file.createNewFile();
                System.out.println("New log file created");
            }
            catch (IOException ioe)
            {
                System.out.println("Cannot create log file");   // FIXME always writing to stdout
            }
        }
        try
        {
            pw = new PrintWriter(new BufferedWriter(new FileWriter(file,true)));
        }
        catch (IOException ioe)
        {
            // TODO Add exception handler
        }
        return pw;
    }
}