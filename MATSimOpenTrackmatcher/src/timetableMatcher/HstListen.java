package timetableMatcher;

import java.util.HashMap;


public class HstListen {
	
	private HashMap<Integer, String> names;
	private HashMap<Integer, String> abkuerzungen;
	
	public HstListen() {}

	public HstListen(HashMap<Integer, String> names, HashMap<Integer, String> abkuerzungen) {
		super();
		this.setNames(names);
		this.setAbkuerzungen(abkuerzungen);
		
	}

	public HashMap<Integer, String> getNames() {
		return names;
	}

	public void setNames(HashMap<Integer, String> names) {
		this.names = names;
	}

	public HashMap<Integer, String> getAbkuerzungen() {
		return abkuerzungen;
	}

	public void setAbkuerzungen(HashMap<Integer, String> abkuerzungen) {
		this.abkuerzungen = abkuerzungen;
	}

}
