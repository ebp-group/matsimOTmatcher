package timetableMatcher;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.matsim.api.core.v01.Id;
import org.matsim.pt.transitSchedule.api.Departure;
import org.matsim.pt.transitSchedule.api.TransitLine;
import org.matsim.pt.transitSchedule.api.TransitRoute;
import org.matsim.pt.transitSchedule.api.TransitRouteStop;
import org.matsim.pt.transitSchedule.api.TransitSchedule;
import OTtimetableObject.Course;
import OTtimetableObject.Timetable;
import OTtimetableObject.TimetableEntry;
import common.Constants;

/**
 * @author Lucas Meyer de Freitas, EBP
 * In this class, the static timetables of OpenTrack and Matsim are matched at a course (trip) level. The result is a 
 * list of corresponding courses in both timetables. 
 * The OT timetable must be in an xml format. ->ATTENTION: The first lines of the xml OpenTrack output created by the list must be
 * deleted. 
 *
 */

public class Matcher {
	
	
	
	public List<MatchedTimetables> matchMATSimToOT(TransitSchedule schedule, Timetable otTimetable, HstListen hst) throws ParseException{

        List<MatchedTimetables> matched = new ArrayList<MatchedTimetables>();
		List<Course> courseList = otTimetable.getCourseList();
		
		
		//Before matching to OT timetable, filter it to get only stops which are actually served: 
		List<Course> filteredCoursesNonFiltered= getfilteredCourses(courseList);
		
		
		//Look into missing station ID's from both stops
		
		
		List<String> allStationID = new ArrayList<String>();
		for(Course thisCourse:filteredCoursesNonFiltered) {
			List<TimetableEntry> timetableEntries = thisCourse.getTimetableEntryList();
			for(TimetableEntry timEntry:timetableEntries) {
				allStationID.add(timEntry.getStationID());
			}
		}

		List<String> allStationNamesMATSIM = new ArrayList<String>();
				
		
//First ensure both transit stop lines have the same entries	
        Map<Id<TransitLine>, TransitLine> lines = schedule.getTransitLines();
        
        for (Map.Entry<Id<TransitLine>, TransitLine> entry : lines.entrySet()) {
        	TransitLine line = entry.getValue();
        	Map<Id<TransitRoute>, TransitRoute> routes = line.getRoutes();
        	for(Map.Entry<Id<TransitRoute>, TransitRoute>  entryRoute : routes.entrySet()) {
        		TransitRoute route = entryRoute.getValue();
 			   List<TransitRouteStop> stops = route.getStops();
    			for (TransitRouteStop stop: stops) {
    				String stopCode = getStopCode(stop, hst);
    				allStationNamesMATSIM.add(stopCode);
    			}
        	}
        }

        List<String> allStationNamesMATSIM_distinct = allStationNamesMATSIM.stream().distinct().collect(Collectors.toList());
        List<String> allStationID_distinct = allStationID.stream().distinct().collect(Collectors.toList());

        List<String> MatsimButNotInOT=allStationNamesMATSIM_distinct;
        List<String> OTButNotInMATSim=allStationID_distinct;

        
 	   OTButNotInMATSim.removeAll(allStationNamesMATSIM_distinct);
 	   MatsimButNotInOT.removeAll(allStationID_distinct);
 	   
       int i =0;

 	   List<Course> filteredCourses = getfilteredCoursesByRemovingNotFoundinMATSim(filteredCoursesNonFiltered, OTButNotInMATSim);
		for(Course thisCourse:filteredCourses) {
			List<TimetableEntry> timetableEntries = thisCourse.getTimetableEntryList();
			
			//Do not match if only one stop is available
			if(timetableEntries.size()<2 || thisCourse.getCourseID().toString().contains("X") || thisCourse.getCourseID().toString().startsWith("5")) {
				i++;
				System.out.println(thisCourse.getCourseID());
			}
		}
		System.out.println("COURSES WITH ONLY ONE STOP COUNT IS " + i);    	 
 	   
        
        for (Map.Entry<Id<TransitLine>, TransitLine> entry : lines.entrySet()) {
        	TransitLine line = entry.getValue();
        	//Now loop through routes
        	Map<Id<TransitRoute>, TransitRoute> routes = line.getRoutes();
        	
        	//_________ROUTE LEVEL________________//
        	for(Map.Entry<Id<TransitRoute>, TransitRoute>  entryRoute : routes.entrySet()) {
        		TransitRoute route = entryRoute.getValue();
     
        		String mode = route.getTransportMode();
        		//Only get rail
        		if(mode.equalsIgnoreCase("rail")) {
                String transitLine = route.getAttributes().getAttribute("02_TransitLine").toString();
                

        		String routeName = route.getAttributes().getAttribute("03_LineRouteName").toString();
        			   List<TransitRouteStop> stops = route.getStops();
        		Map<Id<Departure>, Departure> departures = route.getDepartures();
        			
        			//Put all stops of present MATSIm route in a list
        			List<String> stopCodes = new ArrayList<String>();
        			for (TransitRouteStop stop: stops) {
//        				System.out.println("Stop is "+j);
        				String stopCode = getStopCode(stop, hst);
        				stopCodes.add(stopCode);
        				allStationNamesMATSIM.add(stopCode);
        			}
        			
        			
        				//Now iterate through all courses of OT which could have a match: 
        			for(Course thisCourse:filteredCourses) {
        				
        				List<TimetableEntry> timetableEntries = thisCourse.getTimetableEntryList();
        				//Do not match if only one stop is available
        				if(timetableEntries.size()<2) {
        					
        				} else {
        				     List<String> stationIdList = timetableEntries.stream()
                            .map(TimetableEntry::getStationID)
                            .collect(Collectors.toList());
        				     
        				     //Check if all stopCodes of the MATSim timetable are found in the OT-course

 
				    		    
        				     int index=Collections.indexOfSubList(stationIdList , stopCodes);
        				     if(index==-1) {
            				     int indexInverse=Collections.indexOfSubList(stopCodes , stationIdList);
            				     if(indexInverse==-1) {
            				    	 
            				     } else {
            				    	 for(Map.Entry<Id<Departure>, Departure> departure:departures.entrySet()) {
             				    		double depTime = departure.getValue().getDepartureTime();
             				    		
             				    		
             						    DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        				    		    Date reference = dateFormat.parse("00:00:00");
        				    		    Date date = dateFormat.parse(timetableEntries.get(0).getDepartureTime());
        				    		    double seconds = (date.getTime() - reference.getTime()) / 1000L;
        				    		    
        				    		    

             				    		
             				    		if(Math.abs(depTime-seconds)<60*15) {
             				    			MatchedTimetables match = new MatchedTimetables();
             				    			match.setCourseIdOT(thisCourse.getCourseID());
             				    			match.setDepTimeFirstStopMATsim(depTime);
             				    			match.setDepTimeFirstStopOT(seconds);
             				    			match.setRouteIdMATSim(routeName);
             				    			match.setLineIdMATSim(transitLine);
             				    			
             				    			
             				    			//CHANGE THE MATSim TRANSIT SCHEDULE HERE
             				    			
             				    			

             				    			matched.add(match);
             				    		}
              				    	 }
            				     }

        				     }else {
        				    	//Find the one with the right departure time
    									    		    
    				    	 for(Map.Entry<Id<Departure>, Departure> departure:departures.entrySet()) {
    				    		double depTime = departure.getValue().getDepartureTime();
    				    		String vehicleIdMATSim = departure.getValue().getVehicleId().toString();
    				    		

    				    		
    						    DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
				    		    Date reference = dateFormat.parse("00:00:00");
				    		    Date date = dateFormat.parse(timetableEntries.get(index).getDepartureTime());
				    		    double seconds = (date.getTime() - reference.getTime()) / 1000L;
				    		    
				    		    
    				    		
    				    		if(Math.abs(depTime-seconds)<60*15) {
    				    			MatchedTimetables match = new MatchedTimetables();
    				    			match.setCourseIdOT(thisCourse.getCourseID());
    				    			match.setDepTimeFirstStopMATsim(depTime);
    				    			match.setDepTimeFirstStopOT(seconds);
    				    			match.setRouteIdMATSim(routeName);
    				    			match.setLineIdMATSim(transitLine);
    				    			match.setVehicleIdMATSim(vehicleIdMATSim);	    		
    				    			matched.add(match);
    				    			
    				    			
    				    			
    				    			
    				    			
    				    			
    				    			
    				    		}
     				    	 }
        				     }
        				     }
        				    	//Find the one with the right departure time
       
        				 
        			}   		
        		}
        	}  
        	//_________ROUTE LEVEL________________//
//        	System.out.println("Works for: "+counterWorks);
//    		System.out.println("Doesn't work for: "+counterDoesntWork);
        }
        
        


        
		
		
		return matched;

        
//		return null;		
	}
	
	public static String getStopCode(TransitRouteStop stop, HstListen liste) {
		String stopCode;
		try {
		stopCode = stop.getStopFacility().getAttributes().getAttribute("03_Stop_Code").toString();
		} catch(NullPointerException e) {

		return "notFound";
		}
		
		return stopCode;
		
	}
	
	
	public static List<Course>getfilteredCourses(List<Course> courseList){
		List<Course> filteredCourses= new ArrayList<Course>();
		for(Course course:courseList) {
			List<TimetableEntry> timetableEntries = course.getTimetableEntryList();
			List<TimetableEntry> filteredOne =
			    timetableEntries
			    .stream()
			    .filter(
			        t -> t.getStopInformation().equals("yes")
			        ).collect(Collectors.toList());
			Course filteredCourse = new Course();
			if(filteredOne.size() != 0) {
				filteredCourse.setCourseID(course.getCourseID());
				filteredCourse.setTimetableEntryList(filteredOne);
				filteredCourses.add(filteredCourse);
			}
		
		}

		return filteredCourses;
	}
	

 public static List<Course>getfilteredCoursesByRemovingNotFoundinMATSim(List<Course> courseList, List<String> notFoundInMatsim){
		List<Course> filteredCourses= new ArrayList<Course>();
		for(Course course:courseList) {
			List<TimetableEntry> timetableEntries = course.getTimetableEntryList();
			Set<String>  idHash  = new HashSet<>(notFoundInMatsim);

			List<TimetableEntry> newTimetableEntry = (timetableEntries.stream()
			                                      .filter(e -> !idHash.contains(e.getStationID()))
			                                      .collect(Collectors.toList()));
			
			List<TimetableEntry> newestTimetableEntry = (newTimetableEntry.stream()
                    .filter(e -> !"HH:MM:SS".equals(e.getDepartureTime()))
                    .collect(Collectors.toList()));
			
			if(newestTimetableEntry.size()>0) {
			Course filteredCourse = new Course();
				filteredCourse.setCourseID(course.getCourseID());
				filteredCourse.setTimetableEntryList(newestTimetableEntry);
				filteredCourses.add(filteredCourse);
			}
		
		}

		return filteredCourses;
	}
 
 public static Map<Id<TransitLine>, TransitLine> linesgetFilteredMATSimSchedulebyRemovingStopsNotFoundInOT(Map<Id<TransitLine>, TransitLine> lines, List<String> notFoundInOT){
	 for (Map.Entry<Id<TransitLine>, TransitLine> entry : lines.entrySet()) {
     	TransitLine line = entry.getValue();
     	    	
    	Map<Id<TransitRoute>, TransitRoute> routes = line.getRoutes();
      	for(Map.Entry<Id<TransitRoute>, TransitRoute>  entryRoute : routes.entrySet()) {
    		TransitRoute route = entryRoute.getValue();
			   List<TransitRouteStop> stops = route.getStops();
			   Map<Id<Departure>, Departure> departures = route.getDepartures();
			   int k=0;
			   for(TransitRouteStop stop:stops) {
				   if(notFoundInOT.contains(stop.getStopFacility().getAttributes().getAttribute("03_Stop_Code"))){
					  stops.remove(stop);
					  if(k==0) {
 				    	 for(Map.Entry<Id<Departure>, Departure> departure:departures.entrySet()) {
 				    		  double before = departure.getValue().getDepartureTime();
 				    		  System.out.println("Got Here");
 				    		  System.out.println("Dep Before is "+ before);
				    		  ownDepartureImpl newDep = new ownDepartureImpl(departure.getKey(), before+stops.get(k).getDepartureOffset().seconds());		    		  		    		  
 				    		  departure.setValue(newDep);
 				    		  
 				    		  System.out.println("Departure after is "+departure.getValue().getDepartureTime());
 				    		  
 				    	 }  
					  }
					  k++;
				   }
			   }
      	}

	 }
		return null;

	}
 
 
 public static void writeMatchedTable(List<MatchedTimetables> matchMATSimToOT) throws IOException {
		BufferedWriter matchedTimetablesWriter = new BufferedWriter(new FileWriter(Constants.MATCHED_TIMETABLES_FILE));
		matchedTimetablesWriter.write(
				"CourseIdOT;LineIdMATSim;RouteIdMATSim;VehicleIdMATSim;DepTimeFirstStopMATSim;DepTimeFirstStopOT");
		matchedTimetablesWriter.newLine();
		
		for(MatchedTimetables match:matchMATSimToOT) {

			String line =  match.getCourseIdOT() + ";" + match.getLineIdMATSim()  + ";" + match.getRouteIdMATSim() +";"+ match.getVehicleIdMATSim() +";"+
					match.getDepTimeFirstStopMATsim() + ";"+ match.getDepTimeFirstStopOT();
			
			matchedTimetablesWriter.write(line);
			matchedTimetablesWriter.newLine();
			
			
		}
		
		matchedTimetablesWriter.flush();
		matchedTimetablesWriter.close();

 }
	


}
