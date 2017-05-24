# JGribX

## Introduction
JGribX is a GRIB decoder written in Java. It is essentially a fork of [JGrib](http://jgrib.sourceforge.net/), which as far as I know is now no longer being actively developed. JGribX currently only supports GRIB-1 files, but support for GRIB-2 is being planned.

## Improvements on JGrib
Here is a shortlist of the major changes made since JGrib (version 7 beta):
 * supports grid definition values given as south latitude and west longitude
 * looks up parameter information from locally stored Grib Parameter Tables (GPT) (instead of a single hardcoded GPT)
