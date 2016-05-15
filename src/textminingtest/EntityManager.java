package textminingtest;

import java.util.HashMap;

public class EntityManager
{
  private HashMap<String, NamedEntity> entities;

  public EntityManager()
  {
    entities = new HashMap<>();
  }
  
  public void increaseRelation(String entityName1, String entityName2)
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
      Relation.createRelation(entity1, entity2);
    }
    else
    {
      relation.addWeight();
    }
  }

  public HashMap<String, NamedEntity> getEntities()
  {
    return entities;
  }
}
