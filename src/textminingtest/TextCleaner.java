package textminingtest;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

public class TextCleaner {
	public static void main(String[] args) {
		try {
			cleanTxt("data/ArtemisFowl1.txt", "data/cleanTest.txt");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void cleanTxt(String input, String output) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(input));
		PrintWriter writer = new PrintWriter(output, "UTF-8");
		try {
		    String line = br.readLine();

		    while (line != null) {
		    	line = line.replaceAll("\\d","");
		    	if(line.isEmpty()||line.equals("Artemis Fowl")||line.startsWith("CHAPTER")||line.startsWith("Chapter :")){
		    		line = br.readLine();
		    		continue;
		    	}
		    	line = line.replaceAll("Artemis","Artemisss");
		    	writer.println(line);
		        line = br.readLine();
		    }
		    
		} finally {
		    br.close();
		}
		writer.close();
		System.out.println("clean");
	}
}
