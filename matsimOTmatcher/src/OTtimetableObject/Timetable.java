package OTtimetableObject;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


/**
 * @author Lucas Meyer de Freitas, EBP
 *IMPORTANT NOTE: This class will read the actual times if a timetable with actual runs is provided from OpenTrack and
 *planned times, if the planned timetable is provided. This is so because the structure of the OpenTrack XML run timetable 
 *always places the actual departure/arrival times directly below the planned ones. The class always keeps the last read one to the timetable object, therefore
 *as long as the actual timetable is provided, the code will always read in the planned one. 
 */
@XmlRootElement(name="timetable")
public class Timetable {

	private List<Course> courseList;

    public Timetable() {}

    public Timetable(List<Course> courseList) {
        super();
        this.setCourseList(courseList);
    }
    
    @XmlElement(name="course")
	public List<Course> getCourseList() {
		return courseList;
	}

	public void setCourseList(List<Course> courseList) {
		this.courseList = courseList;
	}
	/**
	 * Removes all entries of the timetable that are only actual. This is used to exclude courses that did not run in a simulation. 
	 * @param thisTimetable
	 */
	public void getActualTimesTimetable(Timetable thisTimetable) {
		
		
		//1- First remove the timetable entries that do not run
		courseList = thisTimetable.getCourseList();
		
			for (ListIterator<Course> course = courseList.listIterator(); course.hasNext();) {
				Course thisCourse = course.next();
			 List<TimetableEntry> timetableEntryList = thisCourse.getTimetableEntryList();
			 
				for (ListIterator<TimetableEntry> timetableEntry = timetableEntryList.listIterator(); timetableEntry.hasNext();) {
					TimetableEntry thisEntry = timetableEntry.next();
					
					//Remove the entry if both departure and arrival are planned
					if(thisEntry.getArrival().getType().equals("planned") && thisEntry.getDeparture().getType().equals("planned")) {
					 timetableEntry.remove();

					} 	
				}	
				
				if(timetableEntryList.isEmpty()) {
					course.remove();
				}
		}
		
		
		//2- Now check the ones which do run and replace HH:MM:SS with null
		for (Course course : courseList) {
			 List<TimetableEntry> timetableEntryList = course.getTimetableEntryList();
			 
				for (ListIterator<TimetableEntry> timetableEntry = timetableEntryList.listIterator(); timetableEntry.hasNext();) {
					TimetableEntry thisEntry = timetableEntry.next();
					
					if(thisEntry.getArrival().getArrivalTime().toString().equals("HH:MM:SS")) {
						thisEntry.setArrival(null);					 	
					} else if(thisEntry.getDeparture().getDepartureTime().toString().equals("HH:MM:SS")) {
						thisEntry.setDeparture(null);					 	

					}
				}				
		}
		
		

		
	}
    
    
}