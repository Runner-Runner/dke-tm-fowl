package textminingtest;

import java.util.HashMap;

public class NamedEntity
{
  private String name;

  private int modularityClass;
  private int id;
  private static int entityCount = 0;
  
  private HashMap<NamedEntity, Relation> relationMap;
  
  public NamedEntity(String name)
  {
	this.id = ++entityCount;
    this.name = name;
    relationMap = new HashMap<>();
  }
  
  public static int getEntityCount() {
	return entityCount;
}

public static void setEntityCount(int entityCount) {
	NamedEntity.entityCount = entityCount;
}

public HashMap<NamedEntity, Relation> getRelationMap() {
	return relationMap;
}

public int getId() {
	return id;
}

public void addRelation(NamedEntity otherEntity, Relation relation)
  {
    relationMap.put(otherEntity, relation);
  }

  public String getName()
  {
    return name;
  }

  public int getModularityClass()
  {
    return modularityClass;
  }

  public void setModularityClass(int modularityClass)
  {
    this.modularityClass = modularityClass;
  }
  
  public Relation getRelation(NamedEntity otherEntity)
  {
    return relationMap.get(otherEntity);
  }
}