package OTtimetableObject;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


/**
 * @author Lucas Meyer de Freitas, EBP
 *
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
    
    
}