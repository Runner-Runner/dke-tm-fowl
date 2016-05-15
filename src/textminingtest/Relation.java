package textminingtest;

public class Relation {
  private NamedEntity entity1;
  private NamedEntity entity2;
  private int weight = 1;

  private Relation(NamedEntity entity1, NamedEntity entity2)
  {
    this.entity1 = entity1;
    this.entity2 = entity2;
  }
  
  public static void createRelation(NamedEntity entity1, NamedEntity entity2)
  {
    Relation relation = new Relation(entity1, entity2);
    entity1.addRelation(entity2, relation);
    entity2.addRelation(entity1, relation);
  }

  public void addWeight()
  {
    weight++;
  }
  
  public NamedEntity getEntity1()
  {
    return entity1;
  }

  public NamedEntity getEntity2()
  {
    return entity2;
  }

  public int getWeight()
  {
    return weight;
  }
  
  
}
