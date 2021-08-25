package timetableMatcher;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.bind.JAXBException;

import org.apache.commons.lang3.SerializationUtils;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.pt.transitSchedule.api.TransitLine;
import org.matsim.pt.transitSchedule.api.TransitRoute;
import org.matsim.pt.transitSchedule.api.TransitSchedule;
import org.matsim.pt.transitSchedule.api.TransitScheduleWriter;

import OTtimetableObject.Course;
import OTtimetableObject.Timetable;
import common.Constants;
import scheduleModifier.TransitScheduleModifier;


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
	    
		
	    //testing: 
	     List<Course> otFilter = otTimetable.getCourseList()
	    		  .stream()
	    		  .filter(c -> c.getCourseID().equals("29431"))
	    		  .collect(Collectors.toList());
	     
	



	    //Write to file
	    Matcher.writeMatchedTable(matchedTimetables);
	    
	    

	    
		//Modify MATSim transit schedule based on OpenTrack timetable
		TransitScheduleModifier modifier = new TransitScheduleModifier();

		
//		TransitSchedule newSchedule = modifier.modifySchedule(schedule, matchedTimetables, otTimetable, listenHst);
		modifier.modifySchedule(schedule, matchedTimetables, otTimetable, listenHst, true);

		//Write new Schedule to File
//		TransitScheduleWriter writer = new TransitScheduleWriter(schedule);
//		writer.writeFile(Constants.PATH_NEW_TRANSIT_SCHEDULE);
	
		//Now find missing ones: 
//	    List<String> lookup = otTimetable.getCourseList().stream()
//                .map(d -> d.getCourseID())
//                .collect(Collectors.toList());
//	    
//  Map<Id<TransitLine>, TransitLine> lines = schedule.getTransitLines();
//        
//	List<String> allRoutes = new ArrayList<String>();
//
//	int k=0;
//        for (Map.Entry<Id<TransitLine>, TransitLine> entry : lines.entrySet()) {
//        	System.out.println(k);
//        	TransitLine line = entry.getValue();
//        	Map<Id<TransitRoute>, TransitRoute> routes = line.getRoutes();
//        	for(Map.Entry<Id<TransitRoute>, TransitRoute>  entryRoute : routes.entrySet()) {
//        		TransitRoute route = entryRoute.getValue();
//        		
//        		String routeID = route.getAttributes().getAttribute("03_LineRouteName").toString();
//        		allRoutes.add(routeID);
//        		k++;
//        	}}
        
	    
	    
//	    List<String> matchedIds = matchedTimetables.stream()
//                .map(d -> d.getRouteIdMATSim())
//                .collect(Collectors.toList());
//
//	    List<String> listDistinct = matchedIds.stream().distinct().collect(Collectors.toList());
//
//
//
//	    allRoutes.removeAll(matchedIds);
	    


	
	}

}
