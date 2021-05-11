package scheduleModifier;


import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.routes.NetworkRoute;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.misc.Time;
import org.matsim.pt.transitSchedule.api.Departure;
import org.matsim.pt.transitSchedule.api.TransitLine;
import org.matsim.pt.transitSchedule.api.TransitRoute;
import org.matsim.pt.transitSchedule.api.TransitRouteStop;
import org.matsim.pt.transitSchedule.api.TransitSchedule;
import org.matsim.pt.transitSchedule.api.TransitScheduleReader;
import org.matsim.pt.transitSchedule.api.TransitScheduleWriter;
import org.matsim.vehicles.Vehicle;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

public class ModifyScheduleInput {
	
	private static final String START_STOP_NAME = "Zürich HB";
	private static final String END_STOP_NAME = "Zürich Oerlikon";
	
	private static String scheduleNumber = "nRe_Rs_pCan";
	private static String mark = "nRe_Rs";
	private static String markkkk = "Partially";
    //private static String markkkk = "Fully";
	
	private static String filePath = "scenarios/optimizationScheduleInput/op_"+scheduleNumber+"/outputReschedule_"+mark+".csv";
	
	private static List<String> modifiedtransitRoutes = ReadTXTcolumn.getFileContent(filePath, 2);
	
	public static void main( final String... args ) throws ClassNotFoundException {
		String inputSchedule = "scenarios/optimizationScheduleInput/op_"+scheduleNumber+"/abmt_pt_schedule_after"+markkkk+"Cancel.xml";
		String outputSchedule = "scenarios/optimizationScheduleInput/op_"+scheduleNumber+"/abmt_pt_schedule_op_"+scheduleNumber+".xml";
		
	   
		Map<String, List<String>> transitLineNameMap = 
	     		ReadTXTcolumnMAP.getFileContent( filePath, 0 , 1 );
		
		//Map<String, List<String>> transitLineRoutesMap = ReadTXTcolumnMAP.getFileContent( filePath, 1 , 2 );
		
		Map<String, List<String>> transitRouteVehicleIDsMap = 
	    		ReadTXTcolumnMAP.getFileContent( filePath, 2 , 3 );
		
		Map<String, HashMap<String, List<Integer>>> vehicleIDStationsTimesMap = 
				ReadTXTcolumnMAPs.getFileContent( filePath );
		   
		Scenario scenario = ScenarioUtils.createScenario( ConfigUtils.createConfig() );
		new TransitScheduleReader( scenario ).readFile( inputSchedule );
		
		filterSchedule ( scenario.getTransitSchedule(), transitLineNameMap, transitRouteVehicleIDsMap, vehicleIDStationsTimesMap );
		
		new TransitScheduleWriter( scenario.getTransitSchedule() ).writeFile( outputSchedule );
	}

	private static void filterSchedule ( final TransitSchedule transitSchedule, Map<String, List<String>> transitLineNameMap,
			                             Map<String, List<String>> transitRouteVehicleIDsMap , Map<String, HashMap<String, List<Integer>>> vehicleIDStationsTimesMap ) {
		for ( TransitLine line  : copyValues( transitSchedule.getTransitLines() ) ) {
			if ( modifiedLine( line ,   transitLineNameMap) ) {
				String lineNameShortHand = lineNameShortHand (line ,   transitLineNameMap);
				for ( TransitRoute route : copyValues( line.getRoutes() ) ) {
				if ( modifiedRoute ( route ) ) {
					List<String> vehicleIDs = transitRouteVehicleIDsMap.get( route.getId().toString() );
					int modifiedNumbers = vehicleIDs.size();
					 //here a for loop based on the size of vehicleID of each transitRoute.
					for( int n = 0; n < modifiedNumbers ; n++ ) { 
				     List< TransitRoute > addFirstRouteList = addFirstRoute( line, route, vehicleIDs.get(n), n , lineNameShortHand, vehicleIDStationsTimesMap);
				     // for loop is only for printing the result??
				     for (int i = 0; i < addFirstRouteList.size(); i++) {
					     TransitRoute r = addFirstRouteList.get(i);
					     if ( whetherAdd (r) ) {
						     line.addRoute( r );
						     System.out.print("\n Add the first route \t" + r.getId() + "\t Stop Numbers are" + r.getStops().size()
						          + "\t departure numbers are" + r.getDepartures().size());
						 }
				     }
				
				     filterLine( line, route, vehicleIDs.get(n) );
					}
				}
				}
			}
			if ( line.getRoutes().isEmpty() ) transitSchedule.removeTransitLine( line );
		}
		System.out.println("\n Finish modifying schedule!");
	}

	private static boolean whetherAdd(TransitRoute route) {
		boolean whetherAdd = true;
		if (route.getStops().size() <= 1) {
			whetherAdd = false;
		}
		return whetherAdd;
	}
	
	private static String lineNameShortHand ( TransitLine line , Map<String, List<String>> transitLineNameMap ) {
		String lineNameShortHand = null;
		if ( modifiedLine ( line, transitLineNameMap  ) ){
			Set<String> keySet = transitLineNameMap.keySet();
	        for (String key : keySet) {
	        	String transitLineName = transitLineNameMap.get(key).get(0);
	        	if ( line.getId().toString().equals(transitLineName) ){
	        		lineNameShortHand = transitLineName;
	        	}
	        }
		}
		return lineNameShortHand;
	}

	private static boolean modifiedLine( TransitLine line , Map<String, List<String>> transitLineNameMap) {
		Set<String> lineSet = new HashSet<String>( );
		
		Set<String> keySet = transitLineNameMap.keySet();
        for (String key : keySet) {
        	String transitLineName = transitLineNameMap.get(key).get(0);
        	lineSet.add( transitLineName );
        }
        
		boolean modified = false;
		Iterator<String> it = lineSet.iterator();
		while ( it.hasNext() ) {
			if (line.getId().toString().equals(it.next().toString())){
				modified = true;
				System.out.println("Find disrupted line:\t" + line.getId().toString());
			}
		}
		return modified;
	}

	private static void filterLine( final TransitLine  targetLine, TransitRoute targetRoute, String targetVehicleID ) {
		filterDepartures( targetRoute, targetVehicleID );	
		if ( targetRoute.getDepartures().isEmpty() ) targetLine.removeRoute( targetRoute );
		
	}

	private static boolean modifiedRoute( TransitRoute route ) {	
		Set<String> routeSet = new HashSet<String>( modifiedtransitRoutes );		
		
		boolean modifiedRoute = false;
		Iterator<String> it = routeSet.iterator();
		while ( it.hasNext() ) {
			if (route.getId().toString().equals(it.next().toString())){
				modifiedRoute = true;
				System.out.println("Find disrupted line:\t" + route.getId().toString());
			}
		}
		return modifiedRoute;
	}

	@SuppressWarnings("unlikely-arg-type")
	private static <T> List<TransitRoute> addFirstRoute(TransitLine targetLine, TransitRoute targetRoute,  String targetVehicleID,
			                                            int n , String targetlineNameShortHand, 
			                                            Map<String, HashMap<String, List<Integer>>> targetvehicleIDStationsTimesMap ) {	
		List<TransitRoute> addFirstRoute = new ArrayList<>();
		
		TransitRoute newRoute = ( TransitRoute ) targetRoute.clone() ;
		// To get the first/last/middle stations + times
		HashMap<String, List<Integer>> stationTimeMap = targetvehicleIDStationsTimesMap.get( targetVehicleID );
	
		System.out.println( stationTimeMap );
		Set<String> stations = stationTimeMap.keySet();
		List<StationTimeClass> stationClassList = new ArrayList<StationTimeClass>();
		for (String station : stations) { 
			StationTimeClass sta = new StationTimeClass(); 
			sta.stationName = station;
			sta.arrivalTime = stationTimeMap.get(station).get(0);
			sta.departureTime = stationTimeMap.get(station).get(1);
			stationClassList.add(sta);
		}
		Collections.sort( stationClassList );  //Got a sorted list (station, arrTime, depTime) from min to max 
		int targetFirstStationArrTimeInt  = stationClassList.get(0).getArrivalTime() ;
		Double targetFirstStationArrTime = new Double(targetFirstStationArrTimeInt); 
		int targetLastStationDepTimeInt  = stationClassList.get(stationClassList.size()-1).getDepartureTime() ;
		Double targetLastStationDepTime = new Double(targetLastStationDepTimeInt);

		//departures
		double arrivalOffsetFirstStationOrignial = getArrivalOffset( newRoute , stationClassList.get(0).getStationName());
		double departureOffsetLastStationOrignial = getDepartureOffset( newRoute,  stationClassList.get(stationClassList.size()-1).getStationName() );
		
		Map<Id<Departure>, Departure> newDeps = new TreeMap<>();
		for ( Departure departure : copyValues( newRoute.getDepartures() ) ) {
				Departure newdeparture = ( Departure ) departure.clone() ;
				Id<Departure> dId = ( Id<Departure> ) departure.getId().clone();
				newDeps.put(dId, newdeparture);
		}	
		newRoute.setDepartures(newDeps);
		for ( Departure depN : copyValues( newDeps) ) {
			Id<Vehicle> vehicleID = depN.getVehicleId();
			if ( !vehicleID.toString().equals(targetVehicleID) ) {
				newDeps.remove(depN.getId());
			}
			else {
			depN.setDepartureTime( targetFirstStationArrTime - arrivalOffsetFirstStationOrignial );
			}
		}
		//stops
		List<TransitRouteStop> stops = newRoute.getStops();
		List<TransitRouteStop> newstops = new ArrayList<>();
		for ( int i=0;  i< stops.size();  i++ ) {
			TransitRouteStop oldStop = stops.get(i);
			TransitRouteStop newStop = (TransitRouteStop) oldStop.clone() ;
			newstops.add(newStop);
		}
		Iterator<TransitRouteStop> stopIt = newstops.iterator();
		while ( stopIt .hasNext() ) {
			TransitRouteStop stopN = stopIt.next();
			// rewrite first station
			if (stopN.getStopFacility().getName().equals( stationClassList.get(0).getStationName() ) ) {
				int targetFirstStationDepTimeInt  = stationClassList.get(0).getDepartureTime() ;
				Double targetFirstStationDepTime = new Double(targetFirstStationDepTimeInt);
				stopN.setDepartureOffset( targetFirstStationDepTime - targetFirstStationArrTime + arrivalOffsetFirstStationOrignial );
			}
			if ( stationClassList.size() == 3  ) {
				if (stopN.getStopFacility().getName().equals( stationClassList.get(1).getStationName() ) ) {
				int targetMiddleStationArrTimeInt  = stationClassList.get(1).getArrivalTime() ;
				Double targetMiddleStationArrTime = new Double(targetMiddleStationArrTimeInt); 
				int targetMiddleStationDepTimeInt  = stationClassList.get(1).getDepartureTime() ;
				Double targetMiddleStationDepTime = new Double(targetMiddleStationDepTimeInt);
				stopN.setDepartureOffset( targetMiddleStationDepTime - targetFirstStationArrTime + arrivalOffsetFirstStationOrignial );
				stopN.setArrivalOffset(targetMiddleStationArrTime - targetFirstStationArrTime + arrivalOffsetFirstStationOrignial );
				}
			}
			if  (stopN.getStopFacility().getName().equals( stationClassList.get(stationClassList.size()-1).getStationName() ) ) {
				int targetLastStationArrTimeInt  = stationClassList.get(stationClassList.size()-1).getArrivalTime() ;
				Double targetLastStationArrTime = new Double(targetLastStationArrTimeInt); 
				
				stopN.setDepartureOffset( targetLastStationDepTime - targetFirstStationArrTime + arrivalOffsetFirstStationOrignial );
				stopN.setArrivalOffset(targetLastStationArrTime - targetFirstStationArrTime + arrivalOffsetFirstStationOrignial );				
			}
			if ( stopN.getDepartureOffset() > departureOffsetLastStationOrignial ) {
				stopN.setDepartureOffset( stopN.getDepartureOffset() + (targetLastStationDepTime - targetFirstStationArrTime + arrivalOffsetFirstStationOrignial)
						                                   - departureOffsetLastStationOrignial);
				stopN.setArrivalOffset( stopN.getArrivalOffset() +  (targetLastStationDepTime - targetFirstStationArrTime + arrivalOffsetFirstStationOrignial)
                        - departureOffsetLastStationOrignial);
				}
		}
			
		//set stops		
		newRoute.setStops(newstops);
		//set route
		NetworkRoute routeaa = (NetworkRoute) newRoute.getRoute().clone() ;
		newRoute.setRoute(routeaa);

		//change Id
		Id<TransitRoute> routeId = newRoute.getId();
		String routeOldId = routeId.toString();
		newRoute.setId(Id.createTranistRouteId( routeOldId + "-" + String.valueOf(n) ));

		addFirstRoute.add(newRoute);
				
		return addFirstRoute;
		
	}

	private static void filterDepartures( final TransitRoute route,  String targetVehicleID ) {
	    for ( Departure departure : copyValues( route.getDepartures() ) ) {  
	    	Id<Vehicle> vehicleID = departure.getVehicleId();
	    	if ( vehicleID.toString().equals(targetVehicleID) ) {
				    route.removeDeparture( departure );
		    }
		}
	}

	//To get Arrival Offset
		private static double getArrivalOffset(final TransitRoute route ,final String stopName ) {
			for ( TransitRouteStop stop : route.getStops() ) {
				if ( stop.getStopFacility().getName().equals( stopName ) ) return stop.getArrivalOffset();
			}
			throw new RuntimeException( "could not find "+stopName+" in "+route );
		}
		//To get Departure Offset
		private static double getDepartureOffset( final TransitRoute route , final String stopName ) {
			for ( TransitRouteStop stop : route.getStops() ) {
				if ( stop.getStopFacility().getName().equals( stopName ) ) return stop.getDepartureOffset();
			}
			throw new RuntimeException( "could not find "+stopName+" in "+route );
		}
		
	// need to copy collections we iterate on, as one cannot modify a collection one iterates on
	private static <V> Iterable<V> copyValues( final Map<?,V> map ) {
		return new ArrayList<>( map.values() );
	}
	
}