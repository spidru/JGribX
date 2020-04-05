package mt.edu.um.cf2.jgribx.grib2;

import java.util.*;

import static java.util.Map.entry;

public class ProductDiscipline
{
    private int value;
    private String name;
    private List<ParameterCategory> categories;

    private static final Map<Integer, String> entries = Map.ofEntries(
            entry(0, "METEOROLOGICAL"),
            entry(1, "HYDROLOGICAL"),
            entry(2, "LAND_SURFACE"),
            entry(3, "SATELLITE_REMOTE_SENSING"),
            entry(10, "OCEANOGRAPHIC")
    );

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
