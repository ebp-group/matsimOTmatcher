package OTtimetableObject;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public class TimetableEntry {
	
	private String stopInformation;
	private String stationID;
	private String arrivalTime;
	private String departureTime;
	private Double waitTime;
	private Double delayTime;
	
    public TimetableEntry() {}
    
    public TimetableEntry(String stopInformation, String stationID, String arrivalTime, String departureTime, Double waitTime,
    		Double delayTime) {
        super();
        this.setStopInformation(stopInformation);
        this.setStationID(stationID);
        this.setArrivalTime(arrivalTime);
        this.setDepartureTime(departureTime);
        this.setWaitTime(waitTime);
        this.setDelayTime(delayTime);
    }

	@XmlAttribute(name= "stopInformation")
	public String getStopInformation() {
		return stopInformation;
	}

	public void setStopInformation(String stopInformation) {
		this.stopInformation = stopInformation;
	}

	@XmlElement(name="stationID")
	public String getStationID() {
		return stationID;
	}

	public void setStationID(String stationID) {
		this.stationID = stationID;
	}

	@XmlElement(name="arrivalTime")
	public String getArrivalTime() {
		return arrivalTime;
	}

	public void setArrivalTime(String arrivalTime) {
		this.arrivalTime = arrivalTime;
	}

	@XmlElement(name="departureTime")
	public String getDepartureTime() {
		return departureTime;
	}

	public void setDepartureTime(String departureTime) {
		this.departureTime = departureTime;
	}

	@XmlElement(name="waitTime")
	public Double getWaitTime() {
		return waitTime;
	}

	public void setWaitTime(Double waitTime) {
		this.waitTime = waitTime;
	}

	@XmlElement(name="delayTime")
	public Double getDelayTime() {
		return delayTime;
	}

	public void setDelayTime(Double delayTime) {
		this.delayTime = delayTime;
	}

}
