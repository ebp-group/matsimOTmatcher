package timetableMatcher;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import OTtimetableObject.Timetable;

/**
 * Class for reading a OpenTrack timetable in xml format. 
 * IMPORTANT: Before reading it in, it is necessary to manually remove the first three lines of the .xml timetable, or to remove it in the read file, otherwise the unmarshaller 
 * will not be able to read it. Here an example of the three lines to be removed: 
 * <?xml version="1.0"?>
*<?xml-stylesheet?>
*<!DOCTYPE timetable SYSTEM "Scenario 0_base.dtd">
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
