package OTtimetableObject;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

import org.opengis.annotation.XmlElement;

/**
 * @author Lucas Meyer de Freitas, EBP
 *
 */
@XmlRootElement(name="departureTime")
public class Departure {
	
	private String departureTime;
	private String type;
	
    public Departure() {}
    
    public Departure(String departureTime, String type) {
        super();

    }

    @XmlValue
    public String getDepartureTime() {
		return departureTime;
	}

	public void setDepartureTime(String departureTime) {
		this.departureTime = departureTime;
	}

	@XmlAttribute(name= "type")
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}


}
