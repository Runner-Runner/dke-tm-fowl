package textminingtest;

import java.util.HashMap;

public class NamedEntity
{
  private String name;

  private int modularityClass;
  
  private HashMap<NamedEntity, Relation> relationMap;
  
  public NamedEntity(String name)
  {
    this.name = name;
    relationMap = new HashMap<>();
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
