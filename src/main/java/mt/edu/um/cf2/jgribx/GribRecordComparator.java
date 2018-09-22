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

/**
  * A comparator class used with HashSet-s when storing/sorting Records as
  * they are read.
  *
  * Compares numerous features from the record information to sort according
  * to a time, level, level-type, y-axis, x-axis order
  *
  * Not implemented yet, and may not be.  Might be used when dealing with
  * multiple files that need to serve as single file.
  *
  * @author Capt Richard D. Gonzalez
  * @version 1.0
  */
import java.util.Comparator;
//import java.util.Calendar;


/**
 * @author rdg
 *
 */
public class GribRecordComparator implements Comparator {
   /**
    * Method required to implement Comparator.
    * If obj1 is less than obj2, return -1, if equal, return 0, else return 1
    * @param obj1 
    * @param obj2 
    * @return Integer value as result of compare operation 
    */
   @Override
   public int compare(Object obj1, Object obj2){
	  
	  // - peg - uncomment the variables, if long detailed check is re-implemented 
      //float z1;
      //float z2;
      //String levelType1;
      //String levelType2;
      //int gridSize1;
      //int gridSize2;
      //Calendar time1;
      //Calendar time2;
      GribRecord gr1;
      GribRecord gr2;
      //GribRecordPDS pds1;
      //GribRecordPDS pds2;
      //GribPDSLevel level1;
      //GribPDSLevel level2;
      //int check;


      // get the records
      gr1 = (GribRecord) obj1;
      gr2 = (GribRecord) obj2;

      // quick check to see if they're the same record
      if (gr1 == gr2) return 0;

      return -1;
/*
      // compare the GDS-s
      check = gr1.getGDS().compare(gr2.getGDS());

      if (check < 0) return -1;
      if (check > 0) return 1; // if not either, they are equal and we continue

      // compare the PDS-s
      check = gr1.getGDS().compare(gr2.getGDS());

      if (check < 0) return -1;
      if (check > 0) return 1; // if not either, they are equal and we continue

      // get the level
      level1 = gr1.getPDS().getPDSLevel();
      level2 = gr2.getPDS().getPDSLevel();

      // compare the levels
//      check = level1
      if (level1.getIndex() < level2.getIndex()) return -1;
      if (level1.getIndex() > level2.getIndex()) return 1;

      // compare the z levels
      z1 = level1.getValue1();
      z2 = level2.getValue1();
      // if the levels are supposed to decrease with height, reverse comparator
      if (!(level1.getIsIncreasingUp())) {
         z1 = -z1;
         z2 = -z2;
      }
      if (z1 < z2) return -1;
      if (z1 > z2) return 1; // if not either, then equal and we continue

      // compare the forecast times
//      pds1 = gr1.getPDS();
//      pds2 = gr2.getPDS();
*/

   } // end of method compare
}