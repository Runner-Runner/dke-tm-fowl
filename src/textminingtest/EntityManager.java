package textminingtest;

import java.util.HashMap;

public class EntityManager
{
  private HashMap<String, NamedEntity> entities;

  public EntityManager()
  {
    entities = new HashMap<>();
  }
  
  public void increaseRelation(String entityName1, String entityName2, double sentiment)
  {
    NamedEntity entity1 = entities.get(entityName1);
    if(entity1 == null)
    {
      entity1 = new NamedEntity(entityName1);
      entities.put(entityName1, entity1);
    }
    
    NamedEntity entity2 = entities.get(entityName2);
    if(entity2 == null)
    {
      entity2 = new NamedEntity(entityName2);
      entities.put(entityName2, entity2);
    }
    
    Relation relation = entity1.getRelation(entity2);
    if(relation == null)
    {
      Relation.createRelation(entity1, entity2, sentiment);
    }
    else
    {
      relation.addWeight(sentiment);
    }
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
