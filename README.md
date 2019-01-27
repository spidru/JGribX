# JGribX

## Introduction
JGribX is a GRIB decoder written in Java. It is essentially a fork of [JGrib](http://jgrib.sourceforge.net/), which as far as I know is now no longer being actively developed. JGribX currently supports both GRIB-1 and GRIB-2 files.

The main objective of JGribX is to create an easy-to-use interface to extract data from GRIB files. 

## Improvements on JGrib
Here is a shortlist of the major changes made since JGrib (version 7 beta):
 * supports GRIB-2 files
 * supports grid definition values given as south latitude and west longitude
 * looks up parameter information from locally stored Grib Parameter Tables (GPT) (instead of a single hardcoded GPT)
 * validates each GRIB record
 * skips invalid/unsupported GRIB records (showing the appropriate errors/warnings)
 
## Important Notes
JGribX is still under active development and therefore contains a large amount of unsupported features and operations. These will be implemented gradually over time. If you would like certain features to be implemented, please open an issue containing all the relevant information.

### Parameter Codes
To uniquely identify and represent different parameters, each parameter has been given its own code. A list of parameter codes can be viewed [here](doc/SUPPORTED_PARAMETERS.md).

### Level Codes and LTVIDs
Similar to parameter codes, level codes are used to uniquely identify each level type and value(s). For example, an isobaric level is represented by the code **ISBL**. In addition, an isobaric level of 200 hPa is represented by the level type-value ID (LTVID) **ISBL:200**. A list of level codes can be viewed [here](doc/SUPPORTED_LEVELS.md).

## Usage
JGribX was originally designed to be a Java library, meaning that it did not have any useful functionality when run as a standalone app. However, a command-line interface is currently being developed which would allow JGribX to be used as a standalone app via command-line.

### Building a Library
Ensure that you have a copy of Gradle

Run the following command at the terminal
```
gradle clean build
``` 

### Library Interface
The simplistic library interface can be observed from the following code snippet (omitting extra stuff such as try-catches):

```java
/* Get the temperature at an isobaric level of 200 hPa above Valletta, Malta at 6th November 2017 14:00:00 */

GribFile gribFile = new GribFile("filename.grb");   // typically .grb or .grb2 extension

Calendar forecastDate = new GregorianCalendar(2017, 10, 6, 14, 0, 0);   // 6th November 2017 14:00:00
String parameterCode = "TMP";    // parameter code for temperature
String ltvid = "ISBL:200";       // LTVID (level type-value ID)
double latitude = 35.8985;       // latitude at point of interest
double longitude = 14.5133;      // longitude at point of interest

GribRecord record = gribFile.getRecord(forecastDate, parameterCode, ltvid);
double value = record.getValue(latitude, longitude);
```

Further examples on how to use JGribX can be found [here](https://github.com/spidru/JGribX/tree/master/src/test).

### Command-Line Interface
As of version 0.4, JGribX can also be used from command-line. For example, to get a quick summary of the contents of a GRIB file:

```bat
java -jar JGribX.jar -i path/to/file
```

## Downloads
Binary builds (currently as JAR files), together with the source, can be found in the [Releases](https://github.com/spidru/JGribX/releases) page. These JAR files are meant to be used as a library in another program. Current versions do not have useful functionality when run as a standalone app.