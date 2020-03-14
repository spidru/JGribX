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

import java.util.HashMap;
import java.util.Map;

/**
 *
 * 02/06/2017   Andrew Spiteri      initial version
 */
public class GribCodes
{
   /**
    * Returns the name of the originating centre corresponding to the specified ID.
    * Source: <a href="http://www-lehre.informatik.uni-osnabrueck.de/~fbstark/diplom/docs/data/GRIB/index.html">http://www-lehre.informatik.uni-osnabrueck.de/~fbstark/diplom/docs/data/GRIB/index.html</a>
    * @param id
    * @return the name of the originating centre corresponding to the specified ID
    */
    public static String getCentreName(int id)
    {
        String name;
        switch(id)
        {
            case 1:
            case 2:
            case 3:
                name = "Melbourne (WMC)";
                break;
            case 4:
            case 5:
            case 6:
                name = "Moscow (WMC)";
                break;
            case 7:
                name = "US National Weather Service, National Centers for Environmental Prediction (NCEP)";
                break;
            case 8:
                name = "US National Weather Service Telecommunications Gateway (NWSTG)";
                break;
            case 9:
                name = "US National Weather Service - Other";
                break;
            case 10:
            case 11:
                name = "Cairo (RSMC/RAFC)";
                break;
            case 12:
            case 13:
                name = "Dakar (RSMC/RAFC)";
                break;
            case 14:
            case 15:
                name = "Nairobi (RSMC/RAFC)";
                break;
            case 18:
            case 19:
                name = "Tunis-Casablanca (RSMC)";
                break;
            case 20:
                name = "Las Palmas (RAFC)";
                break;
            case 21:
                name = "Algiers (RSMC)";
                break;
            case 22:
                name = "ACMAD";
                break;
            case 23:
                name = "Mozambique (NMC)";
                break;
            case 24:
                name = "Pretoria (RSMC)";
                break;
            case 25:
                name = "La Réunion (RSMC)";
                break;
            case 26:
            case 27:
                name = "Khabarovsk (RSMC)";
                break;
            case 28:
            case 29:
                name = "New Delhi (RSMC/RAFC)";
                break;
            case 30:
            case 31:
                name = "Novosibirsk (RSMC)";
                break;
            case 32:
                name = "Tashkent (RSMC)";
                break;
            case 33:
                name = "Jeddah (RSMC)";
                break;
            case 34:
            case 35:
                name = "Tokyo (RSMC), Japan Meterological Agency";
                break;
            case 36:
                name = "Bangkok";
                break;
            case 37:
                name = "Ulan Bator";
                break;
            case 38:
            case 39:
                name = "Beijing (RSMC)";
                break;
            case 40:
                name = "Seoul";
                break;
            case 41:
            case 42:
                name = "Buenos Aires (RSMC/RAFC)";
                break;
            case 43:
            case 44:
                name = "Brasilia (RSMC/RAFC)";
                break;
            case 45:
                name = "Santiago";
                break;
            case 46:
                name = "Brazilian Space Agency - INPE";
                break;
            case 51:
                name = "Miami (RSMC/RAFC)";
                break;
            case 52:
                name = "Miami (RSMC/RAFC), National Hurricane Center";
                break;
            case 53:
            case 54:
                name = "Montreal (RSMC)";
                break;
            case 55:
                name = "San Francisco";
                break;
            case 57:
                name = "US Air Force - Air Force Global Weather Central";
                break;
            case 58:
                name = "Fleet Numerical Meteorology and Oceanography Center, Monterey, CA, USA";
                break;
            case 59:
                name = "The NOAA Forecast Systems Laboratory, Boulder, CO, USA";
                break;
            case 60:
                name = "United States National Center for Atmospheric Research (NCAR)";
                break;
            case 64:
                name = "Honolulu";
                break;
            case 65:
            case 66:
                name = "Darwin (RSMC)";
                break;
            case 67:
                name = "Melbourne (RSMC)";
                break;
            case 69:
            case 70:
                name = "Wellington (RSMC/RAFC)";
                break;
            case 71:
                name = "Nadi (RSMC)";
                break;
            case 74:
            case 75:
                name = "UK Meteorological Office, Bracknell (RSMC)";
                break;
            case 76:
                name = "Moscow (RSMC/RAFC)";
                break;
            case 78:
            case 79:
                name = "Offenbach (RSMC)";
                break;
            case 80:
            case 81:
                name = "Rome (RSMC)";
                break;
            case 82:
            case 83:
                name = "Norrköping";
                break;
            case 85:
                name = "Toulouse (RSMC)";
                break;
            case 86:
                name = "Helsinki";
                break;
            case 87:
                name = "Belgrade";
                break;
            case 88:
                name = "Oslo";
                break;
            case 89:
                name = "Prague";
                break;
            case 90:
                name = "Episkopi";
                break;
            case 91:
                name = "Ankara";
                break;
            case 92:
                name = "Frankfurt/Main (RAFC)";
                break;
            case 93:
                name = "London (WAFC)";
                break;
            case 94:
                name = "Copenhagen";
                break;
            case 95:
                name = "Rota";
                break;
            case 96:
                name = "Athens";
                break;
            case 97:
                name = "European Space Agency (ESA)";
                break;
            case 98:
                name = "European Centre for Medium Range Weather Forecasts (ECMWF) (RSMC)";
                break;
            case 99:
                name = "De Bilt";
                break;
            case 110:
                name = "Hong Kong";
                break;
            case 160:
                name = "US NOAA/NESDIS";
                break;
            case 210:
                name = "Frascati (ESA/ESRIN)";
                break;
            case 211:
                name = "Lanion";
                break;
            case 212:
                name = "Lisboa";
                break;
            case 213:
                name = "Reykjavik";
                break;
            case 254:
                name = "EUMETSAT Operation Centre";
                break;
            case 255:
                name = "Missing value";
                break;    
            default:
                name = "Reserved";
                break;
        }
        return name;
    }
    
    public static String getProcessName(int id)
    {
        String name;
        switch(id)
        {
            case 2:
                name = "Ultra Violet Index Model";
                break;
            case 3:
                name = "NCEP/ARL Transport and Dispersion Model";
                break;
            case 4:
                name = "NCEP/ARL Smoke Model";
                break;
            case 5:
                name = "Satellite Derived Precipitation and temperatures, from IR (See PDS Octet 41... dfor specific satellite ID)";
                break;
            case 6:
                name = "NCEP/ARL Dust Model";
                break;
            case 10:
                name = "Global Wind-Wave Forecast Model";
                break;
            case 11:
                name = "Global Multi-Grid Wave Model (Static Grids)";
                break;
            case 12:
                name = "Probabilistic Storm Surge (P-Surge)";
                break;
            case 13:
                name = "Hurricane Multi-Grid Wave Model";
                break;
            case 14:
                name = "Extra-tropical Storm Surge Atlantic Domain";
                break;
            case 15:
                name = "Nearshore Wave Prediction System (NWPS)";
                break;
            case 16:
                name = "Extra-Tropical Storm Surge (ETSS)";
                break;
            case 17:
                name = "Extra-tropical Storm Surge Pacific Domain";
                break;
            case 18:
                name = "Probabilistic Extra-Tropical Storm Surge (P-ETSS)";
                break;
            case 19:
                name = "Limited-area Fine Mesh (LFM) analysis";
                break;
            case 20:
                name = "Extra-tropical Storm Surge Micronesia Domain";
                break;
            case 25:
                name = "Snow Cover Analysis";
                break;
            case 30:
                name = "Forecaster generated field";
                break;
            case 31:
                name = "Value added post processed field";
                break;
            case 42:
                name = "Global Optimum Interpolation Analysis (GOI) from GFS model";
                break;
            case 43:
                name = "Global Optimum Interpolation Analysis (GOI) from \"Final\" run ";
                break;
            case 44:
                name = "Sea Surface Temperature Analysis";
                break;
            case 45:
                name = "Coastal Ocean Circulation Model";
                break;
            case 46:
                name = "HYCOM - Global";
                break;
            case 47:
                name = "HYCOM - North Pacific basin";
                break;
            case 48:
                name = "HYCOM - North Atlantic basin";
                break;
            case 49:
                name = "Ozone Analysis from TIROS Observations ";
                break;
            case 52:
                name = "Ozone Analysis from Nimbus 7 Observations ";
                break;
            case 53:
                name = "LFM-Fourth Order Forecast Model";
                break;
            case 64:
                name = "Regional Optimum Interpolation Analysis (ROI)";
                break;
            case 68:
                name = "80 wave triangular, 18-layer Spectral model from GFS model";
                break;
            case 69:
                name = "80 wave triangular, 18 layer Spectral model from \"Medium Range Forecast\" run";
                break;
            case 70:
                name = "Quasi-Lagrangian Hurricane Model (QLM)";
                break;
            case 71:
                name = "Hurricane Weather Research and Forecasting (HWRF) Model";
                break;
            case 72:
                name = "Hurricane Non-Hydrostatic Multiscale Model on the B Grid (HNMMB)";
                break;
            case 73:
                name = "Fog Forecast model - Ocean Prod. Center";
                break;
            case 74:
                name = "Gulf of Mexico Wind/Wave";
                break;
            case 75:
                name = "Gulf of Alaska Wind/Wave";
                break;
            case 76:
                name = "Bias corrected Medium Range Forecast";
                break;
            case 77:
                name = "126 wave triangular, 28 layer Spectral model from GFS model";
                break;
            case 78:
                name = "126 wave triangular, 28 layer Spectral model from \"Medium Range Forecast\" run";
                break;
            case 79:
                name = "Backup from the previous run";
                break;
            case 80:
                name = "62 wave triangular, 28 layer Spectral model from \"Medium Range Forecast\" run";
                break;
            case 81:
                name = "Analysis from GFS (Global Forecast System)";
                break;
            case 82:
                name = "Analysis from GDAS (Global Data Assimilation System)";
                break;
            case 83:
                name = "High Resolution Rapid Refresh (HRRR)";
                break;
            case 84:
                name = "MESO NAM Model (currently 12 km)";
                break;
            case 85:
                name = "Real Time Ocean Forecast System (RTOFS)";
                break;
            case 86:
                name = "Early Hurricane Wind Speed Probability Model";
                break;
            case 87:
                name = "CAC Ensemble Forecasts from Spectral (ENSMB)";
                break;
            case 88:
                name = "NOAA Wave Watch III (NWW3) Ocean Wave Model";
                break;
            case 89:
                name = "Non-hydrostatic Meso Model (NMM) (Currently 8 km)";
                break;
            case 90:
                name = "62 wave triangular, 28 layer spectral model extension of the \"Medium Range Forecast\" run";
                break;
            case 91:
                name = "62 wave triangular, 28 layer spectral model extension of the GFS model";
                break;
            case 92:
                name = "62 wave triangular, 28 layer spectral model run from the \"Medium Range Forecast\" final analysis";
                break;
            case 93:
                name = "62 wave triangular, 28 layer spectral model run from the T62 GDAS analysis of the \"Medium Range Forecast\" run";
                break;
            case 94:
                name = "T170/L42 Global Spectral Model from MRF run";
                break;
            case 95:
                name = "T126/L42 Global Spectral Model from MRF run";
                break;
            case 96:
                name = "Global Forecast System Model";
                break;
            case 98:
                name = "Climate Forecast System Model -- Atmospheric model (GFS) coupled to a multi level ocean model .   Currently GFS spectral model at T62, 64 levels coupled to 40 level MOM3 ocean model.";
                break;
            case 99:
                name = "Miscellaneous Test ID";
                break;
            case 104:
                name = "National Blend GRIB";
                break;
            case 105:
                name = "Rapid Refresh (RAP)";
                break;
            case 106:
                name = "Reserved";
                break;
            case 107:
                name = "Global Ensemble Forecast System (GEFS)";
                break;
            case 108:
                name = "LAMP";
                break;
            case 109:
                name = "RTMA (Real Time Mesoscale Analysis)";
                break;
            case 110:
                name = "NAM Model - 15km version";
                break;
            case 111:
                name = "NAM model, generic resolution (Used in SREF processing)";
                break;
            case 112:
                name = "WRF-NMM model, generic resolution (Used in various runs) NMM=Nondydrostatic Mesoscale Model (NCEP)";
                break;
            case 113:
                name = "Products from NCEP SREF processing";
                break;
            case 114:
                name = "NAEFS Products from joined NCEP, CMC global ensembles";
                break;
            case 115:
                name = "Downscaled GFS from NAM eXtension";
                break;
            case 116:
                name = "WRF-EM model, generic resolution (Used in various runs) EM - Eulerian Mass-core (NCAR - aka Advanced Research WRF)";
                break;
            case 117:
                name = "NEMS GFS Aerosol Component";
                break;
            case 118:
                name = "URMA (UnRestricted Mesoscale Analysis)";
                break;
            case 119:
                name = "WAM (Whole Atmosphere Model)";
                break;
            case 120:
                name = "Ice Concentration Analysis";
                break;
            case 121:
                name = "Western North Atlantic Regional Wave Model";
                break;
            case 122:
                name = "Alaska Waters Regional Wave Model";
                break;
            case 123:
                name = "North Atlantic Hurricane Wave Model";
                break;
            case 124:
                name = "Eastern North Pacific Regional Wave Model";
                break;
            case 125:
                name = "North Pacific Hurricane Wave Model";
                break;
            case 126:
                name = "Sea Ice Forecast Model";
                break;
            case 127:
                name = "Lake Ice Forecast Model";
                break;
            case 128:
                name = "Global Ocean Forecast Model";
                break;
            case 129:
                name = "Global Ocean Data Analysis System (GODAS)";
                break;
            case 130:
                name = "Merge of fields from the RUC, NAM, and Spectral Model ";
                break;
            case 131:
                name = "Great Lakes Wave Model";
                break;
            case 132:
                name = "High Resolution Ensemble Forecast (HREF)";
                break;
            case 133:
                name = "Great Lakes Short Range Wave Model";
                break;
            case 140:
                name = "North American Regional Reanalysis (NARR)";
                break;
            case 141:
                name = "Land Data Assimilation and Forecast System";
                break;
            case 150:
                name = "NWS River Forecast System (NWSRFS)";
                break;
            case 151:
                name = "NWS Flash Flood Guidance System (NWSFFGS)";
                break;
            case 152:
                name = "WSR-88D Stage II Precipitation Analysis";
                break;
            case 153:
                name = "WSR-88D Stage III Precipitation Analysis";
                break;
            case 180:
                name = "Quantitative Precipitation Forecast generated by NCEP";
                break;
            case 181:
                name = "River Forecast Center Quantitative Precipitation Forecast mosaic generated by NCEP";
                break;
            case 182:
                name = "River Forecast Center Quantitative Precipitation estimate mosaic generated by NCEP";
                break;
            case 183:
                name = "NDFD product generated by NCEP/HPC";
                break;
            case 184:
                name = "Climatological Calibrated Precipitation Analysis - CCPA";
                break;
            case 190:
                name = "National Convective Weather Diagnostic generated by NCEP/AWC";
                break;
            case 191:
                name = "Current Icing Potential automated product genterated by NCEP/AWC";
                break;
            case 192:
                name = "Analysis product from NCEP/AWC";
                break;
            case 193:
                name = "Forecast product from NCEP/AWC";
                break;
            case 195:
                name = "Climate Data Assimilation System 2 (CDAS2)";
                break;
            case 196:
                name = "Climate Data Assimilation System 2 (CDAS2) - used for regeneration runs";
                break;
            case 197:
                name = "Climate Data Assimilation System (CDAS)";
                break;
            case 198:
                name = "Climate Data Assimilation System (CDAS) - used for regeneration runs";
                break;
            case 199:
                name = "Climate Forecast System Reanalysis (CFSR) -- Atmospheric model (GFS) coupled to a multi level ocean, land and seaice model.   Currently GFS spectral model at T382, 64 levels coupled to 40 level MOM4 ocean model.";
                break;
            case 200:
                name = "CPC Manual Forecast Product";
                break;
            case 201:
                name = "CPC Automated Product";
                break;
            case 210:
                name = "EPA Air Quality Forecast - Currently North East US domain";
                break;
            case 211:
                name = "EPA Air Quality Forecast - Currently Eastern US domain";
                break;
            case 215:
                name = "SPC Manual Forecast Product";
                break;
            case 220:
                name = "NCEP/OPC automated product";
                break;
            case 221:
            case 222:
            case 223:
            case 224:
            case 225:
            case 226:
            case 227:
            case 228:
            case 229:
            case 230:
                name = "Reserved for WPC products";
                break;
            case 255:
                name = "Missing";
                break;
            default:
                name = "Reserved";
                break;
        }
        return name;
    }
    
    public static enum DataRepresentation
    {
        GridPointData_SimplePacking, MatrixValueAtGridPoint_SimplePacking,
        GridPointData_ComplexPacking, GridPointData_ComplexPackingAndSpatialDifferencing
    }
    
    public static enum Discipline
    {
        METEOROLOGICAL(0), HYDROLOGICAL(1), LAND_SURFACE(2), SPACE(3), OCEANOGRAPHIC(10);
        
        private int value;
        private static final Map map = new HashMap<>();
        
        private Discipline(int value)
        {
            this.value = value;
        }
        
        static
        {
            for (Discipline disc : Discipline.values())
                map.put(disc.value, disc);
        }
        
        public static Discipline valueOf(int discipline)
        {
            return (Discipline) map.get(discipline);
        }
        
        public int getValue()
        {
            return value;
        }
    }
    
    public static enum ParameterCategory
    {
        TEMPERATURE(0), MOISTURE(1), MOMENTUM(2), MASS(3);
        
        private int value;
        private static final Map map = new HashMap<>();
        
        private ParameterCategory(int value)
        {
            this.value = value;
        }
        
        static
        {
            for (ParameterCategory cat : ParameterCategory.values())
                map.put(cat.value, cat);
        }
        
        public static ParameterCategory valueOf(int cat)
        {
            return (ParameterCategory) map.get(cat);
        }
        
        public int getValue()
        {
            return value;
        }
    }
}
