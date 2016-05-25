package tagging;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Statistics
{
  private HashMap<String, Statistic> statistics;

  public Statistics()
  {
    statistics = new HashMap<>();
    statistics.put("PERSON", new Statistic());
    statistics.put("LOCATION", new Statistic());
    statistics.put("ORGANIZATION", new Statistic());
    statistics.put("O", new Statistic());
  }

  public Statistic getStatistic(String entityType)
  {
    return statistics.get(entityType);
  }

  public void addData(String proposedType, String actualType, String word)
  {
    Statistic actualStatistic = getStatistic(actualType);
    List<Statistic> otherStatistics = new ArrayList<>();
    otherStatistics.addAll(statistics.values());
    otherStatistics.remove(actualStatistic);

    if (proposedType.equals(actualType) || 
            (actualType.equals("O") && !proposedType.equals("PERSON") && 
            !proposedType.equals("LOCATION") && !proposedType.equals("ORGANIZATION")))
    {
      actualStatistic.tp++;
      for (Statistic s : otherStatistics)
      {
        s.tn++;
      }
    }
    else
    {
      actualStatistic.fn++;
      actualStatistic.fnWords.add(word);
      
      Statistic proposedStatistic = getStatistic(proposedType);
      if(proposedStatistic != null)
      {
        proposedStatistic.fp++;
        proposedStatistic.fpWords.add(word);
        otherStatistics.remove(proposedStatistic);
      }
      
      for (Statistic s : otherStatistics)
      {
        s.tn++;
      }
    }
  }

  public void writeToFile()
  {
    File outFile = new File("data/statistics.txt");
    try
    {
      PrintWriter writer = new PrintWriter(outFile);
      writer.println();

      Iterator<Map.Entry<String, Statistic>> iterator = statistics.entrySet().iterator();
      while (iterator.hasNext())
      {
        Map.Entry<String, Statistic> next = iterator.next();
        writer.println(next.getKey() + ":");
        Statistic statistic = next.getValue();
        writer.println("Precision: " + statistic.getPrecision());
        writer.println("Recall: " + statistic.getRecall());
        writer.println("TP: " + statistic.tp);
        writer.println("TN: " + statistic.tn);
        writer.println("FP: " + statistic.fp);
        writer.println("FP Words: " + statistic.fpWords);
        writer.println("FN: " + statistic.fn);
        writer.println("FN Words: " + statistic.fnWords);
        
      }

      writer.close();
    }
    catch (IOException ex)
    {

    }
  }

}
