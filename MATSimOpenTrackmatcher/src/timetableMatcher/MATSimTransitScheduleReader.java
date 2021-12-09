package timetableMatcher;

import java.io.IOException;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.pt.transitSchedule.api.TransitScheduleReader;

public class MATSimTransitScheduleReader {
	
	public static Scenario readTransitSchedule(String pathToMATSimTransitSchedule) throws IOException {

	    Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		
		  new TransitScheduleReader(scenario).readFile(pathToMATSimTransitSchedule);		
		
		System.out.println("MATSim transit network sucessfuly read!");
		return scenario;
	}	
}
