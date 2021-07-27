package OTtimetableObject;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;

public class Course {
	
	private String courseID;
	private List<TimetableEntry> timetableEntryList;

	public Course() {}
	
	public Course(String courseID, List<TimetableEntry> timetableEntryList) {
		super();
		this.setTimetableEntryList(timetableEntryList);
		this.setCourseID(courseID);
		
	}

    @XmlElement(name="courseID")
	public String getCourseID() {
		return courseID;
	}

	public void setCourseID(String courseID) {
		this.courseID = courseID;
	}
	
    @XmlElement(name="timetableEntry")
	public List<TimetableEntry> getTimetableEntryList() {
		return timetableEntryList;
	}

	public void setTimetableEntryList(List<TimetableEntry> timetableEntryList) {
		this.timetableEntryList = timetableEntryList;
	}
	
}
