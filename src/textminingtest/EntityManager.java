package textminingtest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class EntityManager
{
  private HashMap<String, NamedEntity> entities;
  private HashSet<Relation> relations;

  public EntityManager()
  {
    entities = new HashMap<>();
    relations = new HashSet<>();
  }
  
  public void addDescriptor(String ne, ArrayList<String> descriptors){
	 getNamedEntity(ne).addDescriptors(descriptors);
  }
  
  private NamedEntity getNamedEntity(String name){
	  NamedEntity entity = entities.get(name);
	    if(entity == null)
	    {
	      entity = new NamedEntity(name);
	      entities.put(name, entity);
	    }
	    return entity;
  }
  public Relation increaseRelation(String entityName1, String entityName2, double sentiment)
  {
    NamedEntity entity1 = getNamedEntity(entityName1);
    
    NamedEntity entity2 = getNamedEntity(entityName2);
    
    Relation relation = entity1.getRelation(entity2);
    if(relation == null)
    {
      relation = Relation.createRelation(entity1, entity2, sentiment);
      relations.add(relation);
    }
    else
    {
      relation.addWeight(sentiment);
    }
    return relation;
  }

  public HashSet<Relation> getRelations() {
	return relations;
}

public HashMap<String, NamedEntity> getEntities()
  {
    return entities;
  }
  
//  public void merge(EntityManager em){
//	  HashSet<String> alreadyMerged = new HashSet<>();
//	  for(NamedEntity ne: em.entities.values()){
//		  NamedEntity entityFrom = entities.get(ne.getName());
//		  if(entityFrom == null){
//			  entityFrom = new NamedEntity(ne.getName());
//			  entities.put(entityFrom.getName(), entityFrom);
//		  }
//		  for(Entry<NamedEntity, Relation> entry : ne.getRelationMap().entrySet()){
//			  String to = entry.getKey().getName();
//			  if(alreadyMerged.contains(to))
//				  continue;
//			  NamedEntity entityTo = entities.get(to);
//			  if(entityTo == null){
//				  entityTo = new NamedEntity(to);
//				  entities.put(to, entityTo);
//			  }
//			  Relation re = entityFrom.getRelation(entityTo);
//			  if(re == null){
//				  re = Relation.createRelation(entityFrom, entityTo);
//				  re.increase(-1);
//			  }
//			  re.increase(entry.getValue().getWeight());
//		  }
//		  alreadyMerged.add(ne.getName());
//	  }
//  }
}
