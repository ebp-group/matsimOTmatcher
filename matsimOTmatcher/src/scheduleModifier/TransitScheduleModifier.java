package scheduleModifier;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.matsim.pt.transitSchedule.TransitRouteStopImpl;
import org.matsim.pt.transitSchedule.api.Departure;
import org.matsim.pt.transitSchedule.api.TransitLine;
import org.matsim.pt.transitSchedule.api.TransitRoute;
import org.matsim.pt.transitSchedule.api.TransitRouteStop;
import org.matsim.pt.transitSchedule.api.TransitSchedule;
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
 */
public class TransitScheduleModifier {
	
	
	public void modifySchedule(TransitSchedule transitSchedule, List<MatchedTimetables> matchedTimetables , Timetable otTimetable, HstListen hst){
		int i=0;
		int k=0;
				
		//Prepare Hashmap of all stations: 
	      ArrayList<String> names = new ArrayList<String>(hst.getNames().values());
	      ArrayList<String> abkz = new ArrayList<String>(hst.getAbkuerzungen().values());
	      


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
			
			List<TimetableEntry> timetableEntries = course.getTimetableEntryList();
			
			//Create timetableEntryList only for stops which actually have a stop information 
			List<TimetableEntry> timetableStops = getCoursesWithStops(timetableEntries);
			
			
		int lineInd =0;

		for ( TransitLine line  : copyValues( transitSchedule.getTransitLines() ) ) {
			TransitLine modifiedLine = modifiedTransitSchedule.getTransitLines().get(line.getId());
			
			
			for ( TransitRoute route : copyValues(line.getRoutes() ) ) {
				
				//Get elements from new transit schedule
				TransitRoute modifiedRoute = modifiedLine.getRoutes().get(route.getId());
				Id<TransitRoute> idRoute = modifiedRoute.getId();
				
				//Get matched routes

				if(lineInMatchedLines(route, thisOne)) {
//					System.out.println("Matched line index is "+lineInd);
					

		Map<Id<Departure>, Departure> departures = route.getDepartures();

		List<Id<Departure>> depKeys= new ArrayList<Id<Departure>>();

		//Put matched departures in a list
   	 for(Map.Entry<Id<Departure>, Departure> departure:departures.entrySet()) {   	

			if(departure.getValue().getDepartureTime()==depTime) {
//				System.out.println("IT'S A MATCH!");
   		Id<Departure> key = departure.getKey();
   		depKeys.add(key);
			}
   	 }
   	 
   	 //Iterate through matched departures
   	 for( Iterator<Id<Departure>> iterKeys = depKeys.iterator();iterKeys.hasNext();) {
//   		 System.out.println("matched departure index "+counter);
   	             Id<Departure> depKey = iterKeys.next();
   	             Departure thisDeparture = departures.get(depKey);
   	             
   	                //0-Get corresponding OpenTrack course
   	             
   	             List<TransitRouteStop> stops = modifiedRoute.getStops();

   	             
   	          		
				    //1-Copy route and departure id into a new route
					TransitRoute routeNew = modifiedRoute;
//					System.out.println("Size 1 is "+ routeNew.getDepartures().size());
					
					//2-Remove modified departure from existing route
					modifiedRoute.removeDeparture(thisDeparture);
	   	 
					
					//4-Update all other departure times along route departure time from OT
					DepartureImplNew depNew = new DepartureImplNew(depKey, deptimeOT);
									
					
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

//		 				System.out.println(aa);
	    				 //If stop is not found, just add the time from the last stop
	    				 if(Objects.isNull(otStop) && aa != 0) {
	    					builder.arrivalOffset(arrOffsetPrevious + thisArrOffset - matsimArrPrevious);
	 						builder.departureOffset(depOffsetPrevious + thisDepOffset - matsimDepPrevious);
	 						builder.stop(stop.getStopFacility());		
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
	     				 String arrivalTime = otStop.getArrivalTime();
	    				 String departureTime = otStop.getDepartureTime();
	    				 String[] arr = arrivalTime.split(":");
	    				 String[] dep = departureTime.split(":");
	    				 double arrDouble = Double.parseDouble(arr[0])*3600 + Double.parseDouble(arr[1])*60 + Double.parseDouble(arr[2]);
	    				 double depDouble = Double.parseDouble(dep[0])*3600 + Double.parseDouble(dep[1])*60 + Double.parseDouble(dep[2]);
	    				
	    				  arrOffset = arrDouble-deptimeOT;
	    				  depOffset = depDouble-deptimeOT;	
	    				 }
	    					
						builder.arrivalOffset(arrOffset);
						builder.departureOffset(depOffset);
						builder.stop(stop.getStopFacility());		
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
					
					Id<TransitRoute> id = Id.create(newId, TransitRoute.class);
					
					TransitRouteImpl newRoute = new TransitRouteImpl(id, netRoute, newStops, vehicleIdMatsim);
					
					newRoute.addDeparture(depNew);
										
					//Last step: Update line and then the transitSchedule: 
					modifiedLine.addRoute(newRoute);
					
					List<TransitRouteStop> stopsNewRoute = newRoute.getStops();
//			          System.out.println("StopCodeNewRoute:");
		   	          for (int oo = 0; oo < stopsNewRoute.size(); oo++) {
		   	        	  TransitRouteStop stopNR = stopsNewRoute.get(oo);
//		   	        	  System.out.println(stopNR.getStopFacility().getAttributes().getAttribute("03_Stop_Code"));
		   	        	  
		   	          }			
				
					i++;
						counter++;
					
	
							}
//System.out.println("Total matched departures are: "+ counter);
					
				}
				}

			lineInd++;
			
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
