package OTtimetableObject;

import java.util.List;

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
	
	public Timetable getActualTimesTimetable(Timetable thisTimetable) {
		
		
		
		return thisTimetable;	
	}
    
    
}