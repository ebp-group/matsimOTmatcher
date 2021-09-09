package scheduleModifier;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.matsim.pt.transitSchedule.TransitRouteStopImpl;
import org.matsim.pt.transitSchedule.api.Departure;
import org.matsim.pt.transitSchedule.api.TransitLine;
import org.matsim.pt.transitSchedule.api.TransitRoute;
import org.matsim.pt.transitSchedule.api.TransitRouteStop;
import org.matsim.pt.transitSchedule.api.TransitSchedule;
import org.matsim.visum.VisumNetwork.Stop;
import org.matsim.api.core.v01.Id;
import org.matsim.core.population.routes.NetworkRoute;
import OTtimetableObject.Course;
import OTtimetableObject.Timetable;
import OTtimetableObject.TimetableEntry;
import timetableMatcher.HstListen;
import timetableMatcher.MatchedTimetables;

/**
 * @author Lucas Meyer de Freitas, EBP
 * This class modifies the MATSim schedule based on the OpenTrack timetable, or any timetable in OpenTrack-xml timetable format. 
 * It is not necessary for both timetables to match 100%. The station-to-station travel times between stations not found in the MATSim timetable 
 * are taken from MATSim itself. The workflow of the class is as follows:
 * 1-Look for matched courses: In MATSim this refers to a unique route in a line and a specific departure. 
 * 2-Remove this departure from the MATSim route
 * 3-Create a new route with updated departure (from OpenTrack) and compute new stop arrival and departure times for all stops.
 * 4-Add newly created route to the line. This new route therefore represents one unique course. 
 *
 * Newly created routes have an additional _X suffix at the route number. 
 *
 *
 *When working with the actual timetable (actual instead of planned OT-arrival and departure times), sometimes a stop might not actually have an actual time, eg. 
 *the XML file logs HH:MM:SS instead of a time stamp. This can happen for three reasons: 
 *
 *-The train does not stop at a station but only passes it. This is where stopInformation=”no”. Then actual arrival time is always “HH:MM:SS” and actual departure time is the time the train passed the station. 
 *These are not stations though, only Betriepspunkte (or passing points) and are not relevant for MATSim which only considers stations. 
*-End stop of a line: Then actual departure is “HH:MM:SS” except when a dwell time has been set, then it will show an actual departure time. 
*-First stop of a line: Then actual arrival will be “HH:MM:SS” and only actual departure time will be shown. 
*
*When this occurs, the code assigns a 00:00:00 value to the actual arrival or departure times, meaning that the remaining values of arrival or departure offset are invalid for the rest of the line. 
 */

public class TransitScheduleModifier {
	
	// Create a Logger
    Logger logger
        = Logger.getLogger(
        		TransitScheduleModifier.class.getName());
	
	
 public void modifySchedule(TransitSchedule transitSchedule, List<MatchedTimetables> matchedTimetables , Timetable otTimetable, HstListen hst, boolean useActualTimesOnly){
		int i=0;
		int k=0;
				
		//Prepare Hashmap of all stations: 
	      ArrayList<String> names = new ArrayList<String>(hst.getNames().values());
	      ArrayList<String> abkz = new ArrayList<String>(hst.getAbkuerzungen().values());
	      
	      //Remove planned departure and arrival times if so selected
	      if(useActualTimesOnly) {
	    	  otTimetable.getActualTimesTimetable(otTimetable);	   
	      }

		//Copy the transit schedule
		TransitSchedule modifiedTransitSchedule = transitSchedule;
	   	int counter=0;

		//Iterate through matched courses to find the matching MATSim and OT courses
		for ( Iterator<MatchedTimetables> iter = matchedTimetables.iterator(); iter.hasNext(); ) {
			MatchedTimetables thisOne = iter.next();
			double depTime = thisOne.getDepTimeFirstStopMATsim();
			String courseIdOT = thisOne.getCourseIdOT();
			double deptimeOT = thisOne.getDepTimeFirstStopOT();
			String vehicleIdMatsim = thisOne.getVehicleIdMATSim();
			
			//Get OT course for the matched one
			Course course = otTimetable.getCourseList().stream()
					  .filter(idOT -> courseIdOT.equals(idOT.getCourseID()))
					  .findAny()
					  .orElse(null);
			
			if(Objects.isNull(course)) {
				//Do nothing
			} else {
				
			List<TimetableEntry> timetableEntries = course.getTimetableEntryList();
			
			//Create timetableEntryList only for stops which actually have a stop information 
			List<TimetableEntry> timetableStops = getCoursesWithStops(timetableEntries);
			if(timetableStops.size()==1) {
				//do nothing
			}else {
				
			
		int lineInd =0;

		for (TransitLine line  : copyValues( transitSchedule.getTransitLines() ) ) {
			TransitLine modifiedLine = modifiedTransitSchedule.getTransitLines().get(line.getId());
			
			
			for ( TransitRoute route : copyValues(line.getRoutes() ) ) {
				
				//Get elements from new transit schedule
				TransitRoute modifiedRoute = modifiedLine.getRoutes().get(route.getId());
				Id<TransitRoute> idRoute = modifiedRoute.getId();
				
				//Get matched routes

				if(lineInMatchedLines(route, thisOne)) {
					

		Map<Id<Departure>, Departure> departures = route.getDepartures();

		List<Id<Departure>> depKeys= new ArrayList<Id<Departure>>();

		//Put matched departures in a list
   	 for(Map.Entry<Id<Departure>, Departure> departure:departures.entrySet()) {   	

			if(departure.getValue().getDepartureTime()==depTime) {
   		Id<Departure> key = departure.getKey();
   		depKeys.add(key);
			}
   	 }
   	 
   	 //Iterate through matched departures
   	 for( Iterator<Id<Departure>> iterKeys = depKeys.iterator();iterKeys.hasNext();) {
   	             Id<Departure> depKey = iterKeys.next();
   	             Departure thisDeparture = departures.get(depKey);
   	             
   	                //0-Get corresponding OpenTrack course
   	             
   	             List<TransitRouteStop> stops = modifiedRoute.getStops();

   	             
   	          		
				    //1-Copy route and departure id into a new route
					TransitRoute routeNew = modifiedRoute;
					
					//2-Remove modified departure from existing route
					modifiedRoute.removeDeparture(thisDeparture);
	   	 
					
					//4-Update all other departure times along route departure time from OT
					DepartureImplNew depNew = new DepartureImplNew(depKey, deptimeOT);
					
					depNew.setVehicleId(thisDeparture.getVehicleId());
					
					//5-Create new stops with appropriate stop informations
					//Check if stop is available in list:
					
					List<String> allStationNamesMATSIM = new ArrayList<String>();
					
					List<TransitRouteStop> newStops = new ArrayList<TransitRouteStop>();

					double arrOffsetPrevious=0;
					double depOffsetPrevious=0;
					
					double matsimArrPrevious=0;
					double matsimDepPrevious=0;
					
					int aa = 0;
					
					 double arrOffset=0;
					 double depOffset=0;
					 
					for (TransitRouteStop stop: stops) {
						String stopCode = stop.getStopFacility().getAttributes().getAttribute("03_Stop_Code").toString();
	    				allStationNamesMATSIM.add(stopCode);
	    					
	    				double thisDepOffset = 0;
	    				double thisArrOffset = 0;


	    				if(aa!=0) {
	    					thisDepOffset = stop.getDepartureOffset().seconds();
	    					thisArrOffset = stop.getArrivalOffset().seconds();
	    				}
	    				
	    			    				
	    				 TimetableEntry otStop = timetableStops.stream()
	    						 .filter(l -> l.getStationID().equals(stopCode))
	    						 .findAny()
	    						 .orElse(null);
	    				 
		 				 TransitRouteStopImpl.Builder builder = new TransitRouteStopImpl.Builder();

	    				 //If stop is not found, just add the time from the last stop
	    				 if(Objects.isNull(otStop) && aa != 0) {
	    					builder.arrivalOffset(arrOffsetPrevious + thisArrOffset - matsimArrPrevious);
	 						builder.departureOffset(depOffsetPrevious + thisDepOffset - matsimDepPrevious);
	 						builder.stop(stop.getStopFacility());
	 						builder.awaitDepartureTime(true);
	 						newStops.add(builder.build());
	 						
	 						if(thisDepOffset!=0) {
	 						 arrOffsetPrevious = thisArrOffset;
							 depOffsetPrevious = thisDepOffset;
	 						}
	 					
	    				 } else {

	    					if(aa==0) {
	    						 arrOffset = 0;
	    	    				 depOffset = 0;	
	    					} else {
	    						
	    				
	    				String arrivalTime;
	    				String departureTime;
	    				
	    			
	    		        logger.setLevel(Level.WARNING);

	    				try {
	     				  arrivalTime = otStop.getArrival().getArrivalTime();
	    				} catch (NullPointerException nullOne) {
	    					 arrivalTime="00:00:00";
	    					 if(aa != 0) {
	    						 logger.warning("Course " + course.getCourseID() + ", stop "+ otStop.getStationID() + ". Null actual arrival time value, all arrival times beyond this point for this course are invalid");
	    					 }
	    				}
	    				
	    				try {
	    					departureTime = otStop.getDeparture().getDepartureTime();
	    				} catch (NullPointerException nullTwo) {
	    					departureTime="00:00:00";
	    					 logger.warning("Course " + course.getCourseID() + ", stop "+ otStop.getStationID() + ". Null actual departure time value, all arrival times beyond this point for this course are invalid");
	    				}
	    				 String[] arr = arrivalTime.split(":");
	    				 String[] dep = departureTime.split(":");
	    				 
	    				 double arrDouble = Double.parseDouble(arr[0])*3600 + Double.parseDouble(arr[1])*60 + Double.parseDouble(arr[2]);
	    				 double depDouble = Double.parseDouble(dep[0])*3600 + Double.parseDouble(dep[1])*60 + Double.parseDouble(dep[2]);
	    				
	    				 if(arrDouble==0) {
	    					 arrOffset=stop.getArrivalOffset().seconds();
	    				 } else {
	    					 arrOffset = arrDouble-deptimeOT;
	    				 }
	    				 
	    				 if(depDouble==0) {
	    					 depDouble=stop.getDepartureOffset().seconds();
	    				 } else {
	    					 depOffset = depDouble-deptimeOT;	
	    				 }
	    				  
	    				
	    				 }
	    					
						builder.arrivalOffset(arrOffset);
						builder.departureOffset(depOffset);
						builder.stop(stop.getStopFacility());		
						builder.awaitDepartureTime(true);
						newStops.add(builder.build());
						
						 arrOffsetPrevious = arrOffset;
						 depOffsetPrevious = depOffset;
	    				 }
	    				 
	    				 matsimArrPrevious = thisArrOffset;
	    				 matsimDepPrevious = thisDepOffset;
	    		
						
					aa++;
					}
					NetworkRoute netRoute = routeNew.getRoute();
					String newId=idRoute+"_"+counter;
					if(newId.contentEquals("18911_1_27_7")) {
						System.out.println(newId);
					}
					
					Id<TransitRoute> id = Id.create(newId, TransitRoute.class);
					
					TransitRouteImpl newRoute = new TransitRouteImpl(id, netRoute, newStops, "rail", modifiedRoute.getAttributes());
				
					
					newRoute.addDeparture(depNew);
										
					//Last step: Update line and then the transitSchedule: 
					modifiedLine.addRoute(newRoute);
					
					List<TransitRouteStop> stopsNewRoute = newRoute.getStops();
		   	          for (int oo = 0; oo < stopsNewRoute.size(); oo++) {
		   	        	  TransitRouteStop stopNR = stopsNewRoute.get(oo);
		   	        	  
		   	          }			
				
					i++;
						counter++;
					
	
							}
					
				}
				}

			lineInd++;
			
	}
		}
	}
			
		}	
		}
	

	// need to copy collections we iterate on, as one cannot modify a collection one iterates on
	private static <V> Iterable<V> copyValues( final Map<?,V> map ) {
		return new ArrayList<>( map.values() );
	}

	private static boolean lineInMatchedLines(TransitRoute route , MatchedTimetables matchedTimetablesEntry) {
  		  		
		boolean modified = false;
		
 		try {
					if(matchedTimetablesEntry.getLineIdMATSim().equals(route.getAttributes().getAttribute("02_TransitLine").toString()) && 
		matchedTimetablesEntry.getRouteIdMATSim().equals(route.getAttributes().getAttribute("03_LineRouteName").toString()))
		{
					modified=true;
		} 
		} catch(NullPointerException e) {
			modified = false;
		}
		

		
		return modified;
	}
	
	//Create timetableEntryList only for stops which actually have a stop information 
    public List<TimetableEntry> getCoursesWithStops(List<TimetableEntry> timetableEntries) {
    	String a = "yes";
        List<TimetableEntry> stops = timetableEntries.stream()
				  .filter(thisEntry -> a.equals(thisEntry.getStopInformation()))
				  .collect(Collectors.toList());
   
        return stops;
    }
   
	
}
