package tagging;

import java.util.ArrayList;
import java.util.List;

public class Statistic {
  public int tp = 0;
  public int tn = 0;
  public int fp = 0;
  public int fn = 0;
  public List<String> fpWords;
  public List<String> fnWords;

  public Statistic()
  {
    fpWords = new ArrayList<>();
    fnWords = new ArrayList<>();
  }
  
  public double getPrecision()
  {
    return ((double)tp)/((double)tp+fp);
  }
  
  public double getRecall()
  {
    return ((double)tp)/((double)tp+fn);
  }
}
