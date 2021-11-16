# matsimOTmatcher
Collection of classes to provide communication between OpenTrack (functionality available under OpenTrack-API only) and the agent-based simulation MATSim. Basically, the available classes translate MATSim timetables to OpenTrack timetables and vice-versa. The functionality of each package is outlined below: 

## common
Constants defining the local paths of all necessary input and output files 

## eventHandling
Classes used to calculate train loads. Include modifications. 

### CalculateTrainLoads
Calculates train loads. In this version, only SBB-trains are selected. For changing the selection, modify line 35 of the code. 
This class calls MyTransitLoad (modification of original MATSim-core class) as well as the StopDepLoad. 
Results in a list with loads at each departure stop for each departure of each route (in rail operation language: for each individual course of each line). 

## OTTimetableObject
Collection of classes to transform the elements of the OpenTrack timetable. 
**IMPORTANT:** If a OpenTrack timetable after simulation is provided as an input, instead of a planned timetable, the code will automatically read the actual arrival and departure times, where available. Reason for this is that the actual values are always provided after the planned times (see example below). The xml-reader automatically reads both and keeps only the last entry.

![xml Example](https://github.com/ebp-group/matsimOTmatcher/blob/master/Images/xml%20Example.PNG)

## scheduleModifier
Package with classes to change the MATSim schedule based on the OpenTrack-timetable. The structure of the MATSim-timetable is not very transit friendly. Usual transit timetables (HAFAS, GTFS, OpenTrack-formats) have individual courses (also called trips) for each route, which allow for different travel times between stops for different times of the day. For implementing these time-of-day-dependant differentiations in MATSim, different routes have to be implemented, since the data structure consists of departure times (from first stops) followed by relative travel times from the fist stop. 

### TransitScheduleModifier
This is the class that calls all other classes in the package to change the schedule. To run it, the timetableMatcher should be run beforehand, since it needs a matchedTimetables list, which provides the correspondance between individual courses in MATSim and OpenTrack. Further details present in the class description.

## timetableMatcher
Package used for matching MATSim and OpenTrack (planned or actual) timetables at an individual trip (course) level. 

### Matcher 
This is the class that performs the actual matching of the timetables. The necessary inputs are described in the code. An important fact is that the matching is not perfect in the sense that it will provide a 100% match between timetables. This has to do with two sources of errors: 
- Timetable construction is an iterative process that also goes on, while a timetable is already operational. Depending on the date generation date of the used timetable, there will be mismatches. A full consistency is hard to be ensured. 
- The naming of lines and courses is not consistent between MATSim and OpenTrack.

For the reasons above, an algorithm was implemented that searches for the **best matches**. After testing we found that most of courses can correctly be matched (ca. 80-90%). Full matching will only be possible at the time that full consistency in naming conventions are given. 
The functioning of the algorithm is as follows: 
1- **Route level matching:** Find all candidate routes for matching based on corresponding stop sequences of both. It is taken into account that in MATSim, but mostly in OpenTrack a simulation might not cover the entire run-length of a line (a OpenTrack simulation covering the Zurich-Winterthur area will include IC1-trains running between St. Gallen and Geneve-Aeroport, but not necessarily all stops outside the study area will be available in the OpenTrack timetable file). Therefore the matching is made in both directions: Checking MATSim routes in OpenTrack and vice-versa. 

2-**Course level matching:** Now for each matched route, the algorithm iterates through all possible courses of OpenTrack to find MATSim matches within the same route. Candidates are defined as courses with the same stations and that depart the first matched stop between both timetables within a certain time-window (to take differences in the timetables into account). **IMPORTANT:** The tolerance parameter is set in the Constants.java class by the variable MATCHER_TIME_TOLERANCE. We suggest a 15min time slot. As a result there is a list of possible course-match candidates. 

3- The choice of the match at a course-level is made by choosing the courses of the list above that have the least difference in departure times. 


