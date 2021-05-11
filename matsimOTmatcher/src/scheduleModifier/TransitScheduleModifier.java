package scheduleModifier;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.matsim.pt.transitSchedule.TransitRouteStopImpl;
import org.matsim.pt.transitSchedule.api.Departure;
import org.matsim.pt.transitSchedule.api.TransitLine;
import org.matsim.pt.transitSchedule.api.TransitRoute;
import org.matsim.pt.transitSchedule.api.TransitRouteStop;
import org.matsim.pt.transitSchedule.api.TransitSchedule;

import com.fasterxml.jackson.databind.deser.BuilderBasedDeserializer;

import org.matsim.api.core.v01.Id;
import OTtimetableObject.Timetable;
import timetableMatcher.MatchedTimetables;

public class TransitScheduleModifier {
	
	
	public TransitSchedule modifiedTransitSchedule(TransitSchedule transitSchedule, List<MatchedTimetables> matchedTimetables , Timetable otTimetable){
		int i=0;
		int k=0;

		
		TransitSchedule modifiedTransitSchedule = transitSchedule;
		
		for ( Iterator<MatchedTimetables> iter = matchedTimetables.iterator(); iter.hasNext(); ) {
			MatchedTimetables thisOne = iter.next();
			double depTime = thisOne.getDepTimeFirstStopMATsim();
			String lineIdMatsim = thisOne.getLineIdMATSim();
			String routeIdMatsim = thisOne.getRouteIdMATSim();
			String courseIdOT = thisOne.getCourseIdOT();
			double deptimeOT = thisOne.getDepTimeFirstStopOT();
			String vehicleIdMatsim = thisOne.getVehicleIdMATSim();
			
			
			
		for ( TransitLine line  : copyValues( transitSchedule.getTransitLines() ) ) {
			for ( TransitRoute route : copyValues( line.getRoutes() ) ) {
				
				//Get elements from new transit schedule
				TransitLine modifiedLine = modifiedTransitSchedule.getTransitLines().get(line.getId());
				TransitRoute modifiedRoute = modifiedLine.getRoutes().get(route.getId());
				
				if(lineInMatchedLines(route, thisOne)) {
					
					System.out.println(lineIdMatsim);
System.out.println(route.getDepartures().size());

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
		
				    //1-Copy route and departure id
					TransitRoute routeNew = modifiedRoute;
					System.out.println("Size 1 is "+ routeNew.getDepartures().size());
					//2-Remove departure from existing route
					modifiedRoute.removeDeparture(thisDeparture);
					System.out.println("Size 2 is "+ modifiedRoute.getDepartures().size());

					//3-Remove all other departures but current one from copied route
					Map<Id<Departure>, Departure> departuresCheck = routeNew.getDepartures();
					List<Departure> depKeysCheck= new ArrayList<Departure>();

				   	 for(Map.Entry<Id<Departure>, Departure> departureCheck:departuresCheck.entrySet()) {
				   		depKeysCheck.add(departureCheck.getValue());
							
				   	 }
				   	 
				   	 for( Iterator<Departure> iterKeysCheck = depKeysCheck.iterator();iterKeysCheck.hasNext();) {
		   	             Departure depKeyCheckREM = iterKeysCheck.next();
		   	          routeNew.removeDeparture(depKeyCheckREM);
				   	 }
				   	 
						System.out.println("Size 3 is "+ routeNew.getDepartures().size());

				   	 
					
					//4-New departure with updated departure time from OT
					DepartureImplNew depNew = new DepartureImplNew(depKey, deptimeOT);
					routeNew.addDeparture(depNew);
					
					System.out.println("Size 4 is "+ routeNew.getDepartures().size());

					
					//TODO
					TransitRouteStopImpl.Builder builder = new TransitRouteStopImpl.Builder();
					
							
					System.out.println("dep old is "+ thisDeparture.getDepartureTime());
					System.out.println("dep new is "+ deptimeOT);
					
					
					
					
					i++;
						System.out.println("COUNT IS "+i);		
								
							}
						
						
						

					
				}
				}
					


				
				
				
				
				

				 
				 
				 
				//Check if this specific line and route is found in the matched timetables
//				if(lineInMatchedLines(route, matchedTimetables) ) {
//				
//					TransitRoute newRoute = ( TransitRoute ) route;
//					
//	        	
//					
//
//					Map<Id<Departure>, Departure> newDeps = new TreeMap<>();
//					for ( Departure departure : copyValues( newRoute.getDepartures() ) ) {
//							Departure newdeparture = ( Departure ) departure.clone() ;
//							Id<Departure> dId = ( Id<Departure> ) departure.getId().clone();
//							newDeps.put(dId, newdeparture);
//					}	
//					newRoute.setDepartures(newDeps);
//					
//					
//			}
//			}
			
			

	}
	}
		return modifiedTransitSchedule;
			
			
			
			
			
			
			
		}
	


	
	
	
	
	
	// need to copy collections we iterate on, as one cannot modify a collection one iterates on
	private static <V> Iterable<V> copyValues( final Map<?,V> map ) {
		return new ArrayList<>( map.values() );
	}

	private static boolean lineInMatchedLines(TransitRoute route , MatchedTimetables matchedTimetablesEntry) {
  		  		
		boolean modified = false;
		
		if(matchedTimetablesEntry.getLineIdMATSim().equals(route.getAttributes().getAttribute("02_TransitLine").toString()) && 
		matchedTimetablesEntry.getRouteIdMATSim().equals(route.getAttributes().getAttribute("03_LineRouteName").toString()))
		{
					modified=true;


		}
		
		
		return modified;
	}
	
	
}
