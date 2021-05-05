package timetableMatcher;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.xml.bind.JAXBException;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.pt.transitSchedule.api.TransitLine;
import org.matsim.pt.transitSchedule.api.TransitRoute;
import org.matsim.pt.transitSchedule.api.TransitSchedule;
import OTtimetableObject.Timetable;
import common.Constants;

public class MainClass {

	public static void main(String[] args) throws IOException, JAXBException, ParseException {
		
		//Read MATSim transit network
		Scenario scenario = MATSimTransitScheduleReader.readTransitSchedule(Constants.PATH_MATSIM_TRANSIT_SCHEDULE);
		
		TransitSchedule schedule = scenario.getTransitSchedule();
		
//		CalculateTrainLoads loadCalc = new CalculateTrainLoads();
//		List<StopDepLoad> stopLoadList = loadCalc.loadsForTransitRoutes(scenario.getTransitSchedule(), Constants.PATH_MATSIM_EVENTS);
		
		
		//Read OT timetable
		Timetable otTimetable = OTxmlTimetableReader.readTimetable(Constants.PATH_OT_TIMETABLE);
			
		HstListen listenHst = ReadDienstellen.didok(Constants.PATH_DIENSTELLEN);

		Matcher matcher = new Matcher();
		List<MatchedTimetables> matchedTimetables = matcher.matchMATSimToOT(schedule, otTimetable, listenHst);
		
		
		//Now find missing ones: 
	    List<String> lookup = otTimetable.getCourseList().stream()
                .map(d -> d.getCourseID())
                .collect(Collectors.toList());
	    
  Map<Id<TransitLine>, TransitLine> lines = schedule.getTransitLines();
        
	List<String> allRoutes = new ArrayList<String>();

        for (Map.Entry<Id<TransitLine>, TransitLine> entry : lines.entrySet()) {
        	TransitLine line = entry.getValue();
        	Map<Id<TransitRoute>, TransitRoute> routes = line.getRoutes();
        	for(Map.Entry<Id<TransitRoute>, TransitRoute>  entryRoute : routes.entrySet()) {
        		TransitRoute route = entryRoute.getValue();
        		String routeID = route.getAttributes().getAttribute("03_LineRouteName").toString();
        		allRoutes.add(routeID);
        	}}
        
	    
	    
	    List<String> matchedIds = matchedTimetables.stream()
                .map(d -> d.getRouteIdMATSim())
                .collect(Collectors.toList());

	    List<String> listDistinct = matchedIds.stream().distinct().collect(Collectors.toList());


	    //SBB_2020_012-D-12042

	    allRoutes.removeAll(matchedIds);
	    

	    
	    //Write to file
	    Matcher.writeMatchedTable(matchedTimetables);
	
	    
	
	}

}
