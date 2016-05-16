package textminingtest;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Iterator;

public class WekaParser {
	public static void entitiesToWeka(Collection<NamedEntity> entities, String output) throws FileNotFoundException, UnsupportedEncodingException{
		PrintWriter relationwriter = new PrintWriter(output+"-relation.arff", "UTF-8");
		relationwriter.println("@relation entities");
		for(NamedEntity ne : entities){
			relationwriter.println("@attribute "+ne.getName()+" REAL");
		}
		relationwriter.println("@data");
		for(NamedEntity ne : entities){
			Iterator<NamedEntity> it = entities.iterator();
			while(it.hasNext()){
				Relation r = ne.getRelation(it.next());
				if(r == null){
					relationwriter.print(0);
				}
				else{
					relationwriter.print(r.getWeight());
				}
				if(it.hasNext())
					relationwriter.print(",");
			}
			relationwriter.println();
		}
		relationwriter.close();
	}
}
