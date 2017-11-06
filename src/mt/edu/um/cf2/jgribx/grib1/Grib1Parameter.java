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

/**
 * Title:        JGrib
 * Description:  Class which represents a parameter from a PDS parameter table
 * Copyright:    Copyright (c) 2002
 * Company:      U.S. Air Force
 * @author Capt Richard D. Gonzalez
 * @version 1.0
 */

public class Grib1Parameter {

	/**
	 * Parameter number [0 - 255]
	 */
   protected int number;
   
   /**
    * Parameter abbreviation
    */
   protected String abbrev;
   
   /**
    * Parameter description
    */
   protected String description;

   /**
    * Parameter unit
    */
   protected String unit;

   /**
    * Constuctor - Default
    */
   public Grib1Parameter() {
      this.number=0;
      this.abbrev="UNDEF";
      this.description="undefined";
      this.unit="undefined";
   }

   /**
    * Constructor
    * @param aNum - Parameter number
 	* @param aName - Parameter name
 	* @param aDesc - Parameter description
 	* @param aUnit - Parameter unit
 	*/
   public Grib1Parameter(int aNum, String aName, String aDesc, String aUnit){
      this.number=aNum;
      this.abbrev=aName;
      this.description=aDesc;
      this.unit=aUnit;
   }

   /**
    * @return Parameter number
    */
   public int getNumber(){
      return number;
   }

   /**
    * @return Parameter abbreviation
    */
   public String getAbbreviation(){
      return abbrev;
   }

   /**
    * @return Parameter description
    */
   public String getDescription(){
      return description;
   }

   /**
    * @return Parameter unit
    */
   public String getUnit(){
      return unit;
   }

   
   /**
    * Overrides Object.toString()
    * 
    * @see java.lang.Object#toString()
    * @return String representation of the parameter
    */
   @Override
   public String toString(){
      return number + ":" + abbrev + ":" + description + " [" + unit +"]";
   }

   
   /**
    * Overrides Object.equals()
    * 
    * @see java.lang.Object#equals(java.lang.Object)
    * @return true/false
    */
   public boolean equals(Object obj){
      if (!(obj instanceof Grib1Parameter))
         return false;

      if (this == obj)
         return true;

      Grib1Parameter param = (Grib1Parameter)obj;

      if (abbrev != param.abbrev) return false;
      if (number != param.number) return false;
      if (description != param.description) return false;
      if (unit != param.unit) return false;

      return true;
   }

   /**
    * rdg - added this method to be used in a comparator for sorting while
    *       extracting records.
    * Not currently used in the JGrib library, but is used in a library I'm
    *    using that uses JGrib.
    * @param param to compare
    * @return - -1 if level is "less than" this, 0 if equal, 1 if level is "greater than" this.
    *
    */
   public int compare(Grib1Parameter param){
      if (this.equals(param))
         return 0;

      // check if param is less than this
      // really only one thing to compare because parameter table sets info
      // compare tables in GribRecordPDS
      if (number > param.number) return -1;

      return 1;
   }

}