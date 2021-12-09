package timetableMatcher;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;

public class ReadDienstellen {
	
	public static HstListen didok(String pathToFile) throws IOException {

		File file = new File(pathToFile); 
		List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8); 

		
		HashMap<Integer, String> names = new HashMap<Integer, String>();
		HashMap<Integer, String> abkuerzungen = new HashMap<Integer, String>();

		for (String line : lines) { 
			   String[] array = line.split(";"); 
			  int bpuic = Integer.valueOf(array[3]); //BPUIC
			  String name = array[5]; //Station Name
			  String abkuerz = array[7]; //Abkuuerzung
			  names.put(bpuic, name);
			  abkuerzungen.put(bpuic, abkuerz);
			}
		
		HstListen listen = new HstListen();
		listen.setNames(names);
		listen.setAbkuerzungen(abkuerzungen);
		
		return listen;

}
}
