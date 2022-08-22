package mt.edu.um.cf2.jgribx.grib2;

import mt.edu.um.cf2.jgribx.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class ParameterCategory
{
    private String name;
    private int value;

    public ParameterCategory(int categoryId)
    {
        name = getName(categoryId);
        value = categoryId;
    }

    protected abstract String getName(int value);

    public static List<ParameterCategory> getCategories(int discipline)
    {
        switch (discipline)
        {
            case 0:
                return Meteorological.getCategories();
            case 1:
                return Hydrological.getCategories();
            case 2:
                return LandSurface.getCategories();
            case 3:
                return SatelliteRemoteSensing.getCategories();
            case 10:
                return Oceanographic.getCategories();
            default:
                return null;
        }
    }

    public int getValue()
    {
        return value;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null || !ParameterCategory.class.isAssignableFrom(obj.getClass()))
        {
            return false;
        }
        return value == ((ParameterCategory) obj).value;
    }

    @Override
    public String toString()
    {
        return name;
    }

    public static class Meteorological extends ParameterCategory
    {
        private static final Map<Integer, String> entries = new HashMap();

        static {
            entries.put(0, "TEMPERATURE");
            entries.put(1, "MOISTURE");
            entries.put(2, "MOMENTUM");
            entries.put(3, "MASS");
            entries.put(6, "CLOUD");
            entries.put(7, "THERMODYNAMIC");
            entries.put(14, "TRACE_GASES");
        }

        public Meteorological(int categoryId)
        {
            super(categoryId);
        }

        protected String getName(int value)
        {
            return entries.get(value);
        }

        public static List<ParameterCategory> getCategories()
        {
            List<ParameterCategory> list = new ArrayList<>();
            for (Integer key : entries.keySet())
            {
                Logger.println("Adding Meteorological category: " + key, Logger.DEBUG);
                list.add(new ParameterCategory.Meteorological(key));
            }
            return list;
        }
    }

    public static class Hydrological extends ParameterCategory
    {
        private static final Map<Integer, String> entries = new HashMap<>();

        static {
            entries.put(0, "BASIC");
            entries.put(1, "PROBABILITIES");
        }

        public Hydrological(int categoryId)
        {
            super(categoryId);
        }

        protected String getName(int value)
        {
            return entries.get(value);
        }

        public static List<ParameterCategory> getCategories()
        {
            List<ParameterCategory> list = new ArrayList<>();
            for (Integer key : entries.keySet())
            {
                list.add(new ParameterCategory.Hydrological(key));
            }
            return list;
        }
    }

    public static class LandSurface extends ParameterCategory
    {
        private static final Map<Integer, String> entries = new HashMap<>();

        static {
            entries.put(0, "VEGETATION_BIOMASS");
            entries.put(1, "AGRICULTURAL_AQUACULTURAL");
            entries.put(3, "SOIL");
            entries.put(4, "FIRE_WEATHER");
        }

        public LandSurface(int categoryId)
        {
            super(categoryId);
        }

        protected String getName(int value)
        {
            return entries.get(value);
        }

        public static List<ParameterCategory> getCategories()
        {
            List<ParameterCategory> list = new ArrayList<>();
            for (Integer key : entries.keySet())
            {
                list.add(new ParameterCategory.LandSurface(key));
            }
            return list;
        }
    }

    public static class SatelliteRemoteSensing extends ParameterCategory
    {
        private static final Map<Integer, String> entries = new HashMap<>();

        static {
            entries.put(0, "IMAGE_FORMAT");
            entries.put(1, "QUANTITATIVE");
        }

        public SatelliteRemoteSensing(int categoryId)
        {
            super(categoryId);
        }

        protected String getName(int value)
        {
            return entries.get(value);
        }

        public static List<ParameterCategory> getCategories()
        {
            List<ParameterCategory> list = new ArrayList<>();
            for (Integer key : entries.keySet())
            {
                list.add(new ParameterCategory.SatelliteRemoteSensing(key));
            }
            return list;
        }
    }

    public static class Oceanographic extends ParameterCategory
    {
        private static final Map<Integer, String> entries = new HashMap<>();

        static {
            entries.put(0, "WAVES");
            entries.put(1, "CURRENTS");
        }

        public Oceanographic(int categoryId)
        {
            super(categoryId);
        }

        protected String getName(int value)
        {
            return entries.get(value);
        }

        public static List<ParameterCategory> getCategories()
        {
            List<ParameterCategory> list = new ArrayList<>();
            for (Integer key : entries.keySet())
            {
                list.add(new ParameterCategory.Oceanographic(key));
            }
            return list;
        }
    }
}
