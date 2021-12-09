package eventHandling;

public class StopDepLoad {
	
	private String line;
	private String route;
	private int stopIndex;
	private int load;

	public StopDepLoad() {}
	
	
	public StopDepLoad(String line, String route, int stopIndex, int load) {
		super();
		this.setLine(line);
		this.setRoute(route);
		this.setStopIndex(stopIndex);
		this.setLoad(load);
	}

	public String getLine() {
		return line;
	}

	public void setLine(String line) {
		this.line = line;
	}

	public String getRoute() {
		return route;
	}

	public void setRoute(String route) {
		this.route = route;
	}

	public int getStopIndex() {
		return stopIndex;
	}

	public void setStopIndex(int stopIndex) {
		this.stopIndex = stopIndex;
	}


	public int getLoad() {
		return load;
	}


	public void setLoad(int load) {
		this.load = load;
	}


}
