package mt.edu.um.cf2.jgribx;

public class GeneratingProcess
{
    public enum Type { ANALYSIS, FORECAST, UNKNOWN }

    private final String acronym;
    private final String description;

    private final Type type;

    public GeneratingProcess(Type processType)
    {
        type = processType;
        switch (processType)
        {
            case ANALYSIS:
                acronym = "ANL";
                description = "Analysis";
                break;
            case FORECAST:
                acronym = "FCST";
                description = "Forecast";
                break;
            default:
                acronym = "???";
                description = "Unsupported type";
                break;
        }
    }

    public String getAcronym() { return acronym; }
    public String getDescription() { return description; }
    public Type getType() { return type; }
}
