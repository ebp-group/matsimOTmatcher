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
