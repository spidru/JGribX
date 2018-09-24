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
package mt.edu.um.cf2.jgribx.grib1;

import java.io.IOException;
import mt.edu.um.cf2.jgribx.Bytes2Number;
import mt.edu.um.cf2.jgribx.GribInputStream;
import mt.edu.um.cf2.jgribx.Logger;
import mt.edu.um.cf2.jgribx.NoValidGribException;
import mt.edu.um.cf2.jgribx.NotSupportedException;


/**
 * A class that represents the grid definition section (GDS) of a GRIB record
 * with a Lat/Lon grid projection.
 *
 * @author  Richard Gonzalez
 * based heavily on the original GribRecordGDS
 *
 * @version 1.0
 */

public class Grib1GDSLatLon extends Grib1RecordGDS
{

   // Attributes for Lat/Lon grid not included in GribRecordGDS

   // None!  The Lat/Lon grid is the most basic, and all attributes match
   //   the original GribRecordGDS

   /**
    * Constructs a <tt>GribRecordGDS</tt> object from a bit input stream.
    *
    * See Table D of NCEP Office Note 388 for details
    *
    * @param in bit input stream with GDS content 
    *
    * @throws IOException           if stream can not be opened etc.
    * @throws NoValidGribException  if stream contains no valid GRIB file
    * @throws NotSupportedException 
    */
    public Grib1GDSLatLon(GribInputStream in)
        throws IOException, NoValidGribException, NotSupportedException
    {
        super(in.read(6));

        if (this.grid_type != 0 && this.grid_type != 10)
        {
           throw new NoValidGribException("GribGDSLatLon: grid_type is not "+
             "Latitude/Longitude (read grid type " + grid_type + ", needed 0 or 10)");
        }

        // octets 7-8 (number of points along a parallel)
        grid_nx = in.readUINT(2);

        // octets 9-10 (number of points along a meridian)
        grid_ny = in.readUINT(2);

        // octets 11-13 (latitude of first grid point)
        grid_lat1 = in.readINT(3, Bytes2Number.INT_SM) / 1000.0;

        // octets 14-16 (longitude of first grid point)
        grid_lon1 = in.readINT(3, Bytes2Number.INT_SM) / 1000.0;

        // octet 17 (resolution and component flags -> 128 == 0x80 == increments given.)
        grid_mode = in.readUINT(1);

        /*
        TABLE 7 - RESOLUTION AND COMPONENT FLAGS
     (GDS Octet 17)

     Bit 		Value 		Meaning
     1	0	Direction increments not given
        1	Direction increments given

     2	0	Earth assumed spherical with radius = 6367.47 km
        1	Earth assumed oblate spheroid with size
           as determined by IAU in 1965:
           6378.160 km, 6356.775 km, f = 1/297.0

     3-4		reserved (set to 0)

     5	0	u- and v-components of vector quantities resolved relative to easterly and northerly directions
        1	u and v components of vector quantities resolved relative to the defined grid in the direction of increasing x and y (or i and j) coordinates respectively

     6-8		reserved (set to 0)
        */
        // octets 18-20 (latitude of last grid point)
        grid_lat2 = in.readINT(3, Bytes2Number.INT_SM) / 1000.0;

        // octets 21-23 (longitude of last grid point)
        grid_lon2 = in.readINT(3, Bytes2Number.INT_SM) / 1000.0;

        boolean incrementsGiven = (this.grid_mode & 0x80) == 0x80;
        boolean earthShapeSpheroid = (this.grid_mode & 0x40) == 0x40;
        boolean uvResolvedToGrid = (this.grid_mode & 0x08) == 0x08;

        if (incrementsGiven)
        {
            // MSB seems to indicate signedness, but this is taken care of by scanMode, so we use abs()
            grid_dx = Math.abs(in.readINT(2, Bytes2Number.INT_SM) / 1000.0);
            grid_dy = Math.abs(in.readINT(2, Bytes2Number.INT_SM) / 1000.0);
        }
        else
        {
            // calculate increments
            this.grid_dx = (this.grid_lon2 - this.grid_lon1) / this.grid_nx;
            this.grid_dy = (this.grid_lat2 - this.grid_lat1) / this.grid_ny;
        }
        
        if (earthShapeSpheroid)
        {
            Logger.println("GRIB record assumes Earth is an oblate spheroid. This is not supported yet.", Logger.ERROR);
        }
        
        if (uvResolvedToGrid)
        {
            Logger.println("GRIB record resolves u- and v-components of vector quantities are relative to the defined grid. This is not supported yet.", Logger.ERROR);
        }
        
        /* [28] Scan Mode */
        grid_scan = in.readUINT(1);
        boolean iPositiveDirection = (grid_scan & 0x80) != 0x80;
        boolean jPositiveDirection = (grid_scan & 0x40) == 0x40;
        
        if (!iPositiveDirection)
            this.grid_dx *= -1;
        if (!jPositiveDirection)
            this.grid_dy *= -1;
        
        in.read(4);     // dummy read
        
        switch (this.grid_type)
        {
           case 0:
              // Standard Lat/Lon grid, no rotation
              this.grid_latsp = -90.0;
              this.grid_lonsp = 0.0;
              this.grid_rotang = 0.0;
              break;
           case 10:
              // Rotated Lat/Lon grid, Lat (octets 33-35), Lon (octets 36-38), rotang (octets 39-42)
              //NB offset = 7 (octet = array index + 7)
              Logger.println("GRIB Record uses rotated LatLon grid. This is untested.", Logger.WARNING);
              grid_latsp = in.readINT(3, Bytes2Number.INT_SM) / 1000.0;
              grid_lonsp = in.readINT(3, Bytes2Number.INT_SM) / 1000.0;
              grid_rotang = in.readFloat(4, Bytes2Number.FLOAT_IBM);
              break;
           default:
              // No knowledge yet
              // NEED to fix this later, if supporting other grid types
              Logger.println("GRIB Record uses an unsupported type of LatLon grid.", Logger.ERROR);
              this.grid_latsp = Double.NaN;
              this.grid_lonsp = Double.NaN;
              this.grid_rotang = Double.NaN;
              break;
        }
   }

// *** public methods **************************************************************

   /**
    * @return 
    * @see net.sourceforge.jgrib.GribRecordGDS#isUVEastNorth()
    */
    @Override
    public boolean isUVEastNorth()
    {
        return (grid_mode & 0x08) == 0;
    }
  
   /** 
    * @see net.sourceforge.jgrib.GribRecordGDS#compare(net.sourceforge.jgrib.GribRecordGDS)
    */   
   public int compare(Grib1RecordGDS gds) {
    if (this.equals(gds)){
       return 0;
    }

    // not equal, so either less than or greater than.
    // check if gds is less, if not, then gds is greater
    if (grid_type > gds.grid_type) return -1;
    if (grid_mode > gds.grid_mode) return -1;
    if (grid_scan > gds.grid_scan) return -1;
    if (grid_nx > gds.grid_nx) return -1;
    if (grid_ny > gds.grid_ny) return -1;
    if (grid_dx > gds.grid_dx) return -1;
    if (grid_dy > gds.grid_dy) return -1;
    if (grid_lat1 > gds.grid_lat1) return -1;
    if (grid_lat2 > gds.grid_lat2) return -1;
    if (grid_latsp > gds.grid_latsp) return -1;
    if (grid_lon1 > gds.grid_lon1) return -1;
    if (grid_lon2 > gds.grid_lon2) return -1;
    if (grid_lonsp > gds.grid_lonsp) return -1;
    if (grid_rotang > gds.grid_rotang) return -1;

    // if here, then something must be greater than something else - doesn't matter what
    return 1;
 }   

   /**
    * @see net.sourceforge.jgrib.GribRecordGDS#hashCode
    * @return integer value of hashCode
    */   
   public int hashCode()
   {
      int result = 17;
      result = 37 * result + grid_nx;
      result = 37 * result + grid_ny;
      int intLat1 = Float.floatToIntBits((float) grid_lat1);
      result = 37 * result + intLat1;
      int intLon1 = Float.floatToIntBits((float) grid_lon1);
      result = 37 * result + intLon1;
      return result;
   }

   /**
    * @see net.sourceforge.jgrib.GribRecordGDS#equals(java.lang.Object)
    * @return true/false if objects are equal
 	*/   
   public boolean equals(Object obj)
   {
      if (!(obj instanceof Grib1RecordGDS))
      {
         return false;
      }
      if (this == obj)
      {
         // Same object
         return true;
      }
      Grib1RecordGDS gds = (Grib1RecordGDS) obj;

      if (grid_type != gds.grid_type) return false;
      if (grid_mode != gds.grid_mode) return false;
      if (grid_scan != gds.grid_scan) return false;
      if (grid_nx != gds.grid_nx) return false;
      if (grid_ny != gds.grid_ny) return false;
      if (grid_dx != gds.grid_dx) return false;
      if (grid_dy != gds.grid_dy) return false;
      if (grid_lat1 != gds.grid_lat1) return false;
      if (grid_lat2 != gds.grid_lat2) return false;
      if (grid_latsp != gds.grid_latsp) return false;
      if (grid_lon1 != gds.grid_lon1) return false;
      if (grid_lon2 != gds.grid_lon2) return false;
      if (grid_lonsp != gds.grid_lonsp) return false;
      if (grid_rotang != gds.grid_rotang) return false;

      return true;
   }

   /**
    * Get length in bytes of this section.
    *
    * @return length in bytes of this section
    */
   public int getLength()
   {
      return length;
   }

   /**
    * Get type of grid.  This is type 0.
    *
    * @return type of grid
    */
   public int getGridType()
   {
      return grid_type;
   }

   /**
    * @return true/false
    */
   public boolean isRotatedGrid()
   {
	   // Implicit IF-THEN
      return grid_type == 10;
   }

   /**
    * Get number of grid columns.
    *
    * @return number of grid columns
    */
   public int getGridNX()
   {
      return grid_nx;
   }

   /**
    * Get number of grid rows.
    *
    * @return number of grid rows.
    */
   public int getGridNY()
   {
      return grid_ny;
   }

   /**
    * Get latitude of grid start point.
    *
    * @return latitude of grid start point
    */
   public double getGridLat1()
   {
      return grid_lat1;
   }

   /**
    * Get longitude of grid start point.
    *
    * @return longitude of grid start point
    */
   public double getGridLon1()
   {
      return grid_lon1;
   }

   /**
    * Get grid mode. <i>Only 128 (increments given) supported so far.</i>
    *
    * @return grid mode
    */
   public int getGridMode()
   {
      return grid_mode;
   }

   /**
    * Get latitude of grid end point.
    *
    * @return latitude of grid end point
    */
   public double getGridLat2()
   {
      return grid_lat2;
   }

   /**
    * Get longitude of grid end point.
    *
    * @return longitude of grid end point
    */
   public double getGridLon2()
   {
      return grid_lon2;
   }

   /**
    * Get delta-Lon between two grid points.
    *
    * @return Lon increment
    */
   public double getGridDX()
   {
      return grid_dx;
   }

   /**
    * Get delta-Lat between two grid points.
    *
    * @return Lat increment
    */
   public double getGridDY()
   {
      return grid_dy;
   }

   /**
    * Get scan mode (sign of increments). <i>Only 64, 128 and 192 supported so far.</i>
    *
    * @return scan mode
    */
   public int getGridScanmode()
   {
      return grid_scan;
   }

   /**
    * Get longitide coordinates converted to the range +/- 180
    * @return longtitude as double
    */
   public double[] getXCoords()
   {
      return getXCoords(true);
   }

   /**
    * Get longitide coordinates
    * @param convertTo180 
    * @return longtitude as double
    */
   public double[] getXCoords(boolean convertTo180)
   {
      double[] coords = new double[grid_nx];

      int k = 0;

      for (int x = 0; x < grid_nx; x++)
      {
         double longi = grid_lon1 + x * grid_dx;

         if (convertTo180){ // move x-coordinates to the range -180..180
            if (longi >= 180.0) longi = longi - 360.0;
            if (longi < -180.0) longi = longi + 360.0;
         }else{ // handle wrapping at 360
            if (longi >= 360.0) longi = longi - 360.0;
         }
         coords[k++] = longi;
      }
      return coords;
   }

   /**
    * Get all latitude coordinates
    * @return latitude as double
    */
    @Override
    public double[] getYCoords()
    {
       double[] coords = new double[grid_ny];

       int k = 0;

       for (int y = 0; y < grid_ny; y++)
       {
          double lati = grid_lat1 + y * grid_dy;
          if (lati > 90.0 || lati < -90.0)
             Logger.println("GribGDSLatLon.getYCoords: latitude out of range (-90 to 90).", Logger.ERROR);

          coords[k++] = lati;
       }
       return coords;
    }

   /**
    * Get grid coordinates in longitude/latitude pairs
    * Longitude is returned in the range +/- 180 degrees
    * 
    * @see net.sourceforge.jgrib.GribRecordGDS#getGridCoords() 
    * @return longitide/latituide as doubles
    */
    @Override
    public double[] getGridCoords()
    {

       double[] coords = new double[grid_ny * grid_nx * 2];

       int k = 0;
       for (int y = 0; y < grid_ny; y++){
          for (int x = 0; x < grid_nx; x++){
             double longi = grid_lon1 + x * grid_dx;
             double lati = grid_lat1 + y * grid_dy;

             // move x-coordinates to the range -180..180
             if (longi >= 180.0) longi = longi - 360.0;
             if (longi < -180.0) longi = longi + 360.0;
             if (lati > 90.0 || lati < -90.0)
             {
                Logger.println("GribGDSLatLon.getGridCoords: latitude out of range (-90 to 90).", Logger.ERROR);
             }
             coords[k++] = longi;
             coords[k++] = lati;
          }
       }
       return coords;
    }


   /**
    * Get a string representation of this GDS.
    * TODO include more information about this projection
    * @return string representation of this GDS
    */
    @Override
    public String toString()
    {

       String str = "    GDS section:\n      ";
       if (this.grid_type == 0) str += "  LatLon Grid";
       if (this.grid_type == 10) str += "  Rotated LatLon Grid";

       str += "  (" + this.grid_nx + "x" + this.grid_ny + ")\n      ";
       str += "  lon: " + this.grid_lon1 + " to " + this.grid_lon2;
       str += "  (dx " + this.grid_dx + ")\n      ";
       str += "  lat: " + this.grid_lat1 + " to " + this.grid_lat2;
       str += "  (dy " + this.grid_dy + ")";

       if (this.grid_type == 10) {
         str += "\n        south pole: lon " + this.grid_lonsp + " lat " + this.grid_latsp;
         str += "\n        rot angle: " + this.grid_rotang;
       }

       return str;
    }
}


