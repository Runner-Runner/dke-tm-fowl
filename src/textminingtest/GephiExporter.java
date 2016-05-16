package textminingtest;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.HashSet;

public class GephiExporter {
	
	public static void exportCSV(Collection<NamedEntity> entities, String name) throws 
          FileNotFoundException, UnsupportedEncodingException{
		PrintWriter nodewriter = new PrintWriter(name+"-nodes.csv", "UTF-8");
		PrintWriter edgewriter = new PrintWriter(name+"-edges.csv", "UTF-8");
		nodewriter.println("id,label,timeset,modularity_class");
		edgewriter.println("Source,Target,Type,id,label,timeset,weight");
		HashSet<Relation> visitedRelations= new HashSet<>();
		int id = 0;
		for(NamedEntity ne : entities){
			nodewriter.println(ne.getId()+","+ne.getName()+",,"+ne.getModularityClass());
			for(Relation r:ne.getRelationMap().values()){
				if(visitedRelations.contains(r))
					continue;
				visitedRelations.add(r);
				edgewriter.println(r.getEntity1().getId()+","+r.getEntity2().getId()+",Undirected,"+id+",,,"+r.getWeight());
				id++;
			}
		}
		edgewriter.close();
		nodewriter.close();
	}
}
