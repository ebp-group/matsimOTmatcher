package timetableMatcher;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import OTtimetableObject.Timetable;

/**
 * Class for reading a OpenTrack timetable in xml format. 
 * IMPORTANT: Before reading it in, it is necessary to remove the first three lines otherwise the Unmarshaller will not be able to read it. The first line of the xml file should be: 
 * "timetable title="OpenTrack timetable" application="OpenTrack" date="Wed Mar 31 13:23:32 2021""
 * 
 * @author Lucas Meyer de Freitas, EBP
 *
 */
public class OTxmlTimetableReader {

	public static Timetable readTimetable(String pathToOTtimetable) throws JAXBException {
		
			File file = new File(pathToOTtimetable);  

		    JAXBContext jaxbContext = JAXBContext.newInstance(Timetable.class);
	        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();  
	        Timetable timetable = (Timetable) jaxbUnmarshaller.unmarshal(file); 

			return timetable; 
	}	
}
