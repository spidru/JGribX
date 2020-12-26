package mt.edu.um.cf2.jgribx.grib2;

import java.util.*;

public class ProductDiscipline
{
    private final int value;
    private final String name;
    private final List<ParameterCategory> categories;

    private static final Map<Integer, String> entries = new HashMap<>();

    static {
        entries.put(0, "METEOROLOGICAL");
        entries.put(1, "HYDROLOGICAL");
        entries.put(2, "LAND_SURFACE");
        entries.put(3, "SATELLITE_REMOTE_SENSING");
        entries.put(10, "OCEANOGRAPHIC");
    }

    public ProductDiscipline(int discipline)
    {
        name = entries.get(discipline);
        value = discipline;
        categories = ParameterCategory.getCategories(discipline);
    }

    public List<ParameterCategory> getParameterCategories()
    {
        return categories;
    }

    public static List<ProductDiscipline> getValues()
    {
        List<ProductDiscipline> list = new ArrayList<>();
        for (Integer key : entries.keySet())
        {
            list.add(new ProductDiscipline(key));
        }
        return list;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null || !ProductDiscipline.class.isAssignableFrom(obj.getClass()))
        {
            return false;
        }
        return value == ((ProductDiscipline) obj).value;
    }

    @Override
    public String toString()
    {
        return entries.get(value);
    }
}
