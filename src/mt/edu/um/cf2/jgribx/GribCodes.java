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
 *
 * 02/06/2017   Andrew Spiteri      initial version
 */
public class GribCodes
{
    public static enum DataRepresentation
    {
        GridPointData_SimplePacking, MatrixValueAtGridPoint_SimplePacking,
        GridPointData_ComplexPacking, GridPointData_ComplexPackingAndSpatialDifferencing
    }
    
    public static enum Discipline
    {
        METEOROLOGICAL, HYDROLOGICAL, LAND_SURFACE, SPACE, OCEANOGRAPHIC
    }
    
    public static enum ParameterCategory
    {
        TEMPERATURE, MOISTURE, MOMENTUM, MASS
    }
}
