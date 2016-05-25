package textminingtest;

import java.util.ArrayList;
import java.util.List;

public class Relation {
  private NamedEntity entity1;
  private NamedEntity entity2;
  private ArrayList<String> descriptors;
  private int weight = 1;
  private double sentiment = 0;

  private Relation(NamedEntity entity1, NamedEntity entity2)
  {
    this.entity1 = entity1;
    this.entity2 = entity2;
    this.descriptors = new ArrayList<>();
  }
  
  public static Relation createRelation(NamedEntity entity1, NamedEntity entity2, double sentiment)
  {
    Relation relation = new Relation(entity1, entity2);
    relation.sentiment = sentiment;
    entity1.addRelation(entity2, relation);
    entity2.addRelation(entity1, relation);
    return relation;
  }

  public void addWeight(double sentiment)
  {
	  this.sentiment+=sentiment;
	  weight++;
  }
  
  public double getSentiment() {
	return sentiment;
}

public ArrayList<String> getDescriptors() {
	return descriptors;
}

public void setWeight(int weight) {
	this.weight = weight;
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
  
  public void increase(int weight, double sentiment){
	  this.weight+=weight;
	  this.sentiment+=sentiment;
  }
  public void addDescriptors(List<String> descriptors){
	  this.descriptors.addAll(descriptors);
  }
  
  
}
