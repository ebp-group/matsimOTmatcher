package OTtimetableObject;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

import org.opengis.annotation.XmlElement;

/**
 * @author Lucas Meyer de Freitas, EBP
 *
 */
@XmlRootElement(name="arrivalTime")
public class Arrival {
	
	private String arrivalTime;
	private String type;
	
    public Arrival() {}
    
    public Arrival(String arrivalTime, String type) {
        super();

    }

	@XmlAttribute(name= "type")
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@XmlValue
	public String getArrivalTime() {
		return arrivalTime;
	}

	public void setArrivalTime(String arrivalTime) {
		this.arrivalTime = arrivalTime;
	}


}
