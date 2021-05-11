package timetableMatcher;

public class MatchedTimetables {
	
	private String lineIdMATSim;
	private String routeIdMATSim;
	private String vehicleIdMATSim;
	private String courseIdOT;
	private double depTimeFirstStopMATsim;
	private double depTimeFirstStopOT;

	public MatchedTimetables() {}

	public MatchedTimetables(String lineIdMATSim, String routeIdMatsim, String vehicleIdMATSim, String courseIdOT, double depTimeFirstStopMATsim, double depTimeFirstStopOT) {
		super();
		this.setLineIdMATSim(lineIdMATSim);
		this.setCourseIdOT(courseIdOT);
		this.setRouteIdMATSim(routeIdMatsim);
		this.setRouteIdMATSim(vehicleIdMATSim);
		this.setDepTimeFirstStopMATsim(depTimeFirstStopMATsim);
		this.setDepTimeFirstStopOT(depTimeFirstStopOT);
	}

	public String getLineIdMATSim() {
		return lineIdMATSim;
	}

	public void setLineIdMATSim(String lineIdMATSim) {
		this.lineIdMATSim = lineIdMATSim;
	}

	public String getRouteIdMATSim() {
		return routeIdMATSim;
	}

	public void setRouteIdMATSim(String routeIdMATSim) {
		this.routeIdMATSim = routeIdMATSim;
	}

	public double getDepTimeFirstStopMATsim() {
		return depTimeFirstStopMATsim;
	}

	public void setDepTimeFirstStopMATsim(double depTimeFirstStopMATsim) {
		this.depTimeFirstStopMATsim = depTimeFirstStopMATsim;
	}

	public double getDepTimeFirstStopOT() {
		return depTimeFirstStopOT;
	}

	public void setDepTimeFirstStopOT(double depTimeFirstStopOT) {
		this.depTimeFirstStopOT = depTimeFirstStopOT;
	}

	public String getCourseIdOT() {
		return courseIdOT;
	}

	public void setCourseIdOT(String courseIdOT) {
		this.courseIdOT = courseIdOT;
	}

	public String getVehicleIdMATSim() {
		return vehicleIdMATSim;
	}

	public void setVehicleIdMATSim(String vehicleIdMATSim) {
		this.vehicleIdMATSim = vehicleIdMATSim;
	}


}
