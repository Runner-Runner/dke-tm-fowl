package textminingtest;

import edu.stanford.nlp.hcoref.CorefCoreAnnotations.CorefClusterIdAnnotation;
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

public class TextMiner
{
  private String directory;

  private EntityManager entityManager;

  private HashMap<Integer, String> corefIdMapping;

  private static final String ANNOTATORS
          = "tokenize, ssplit, pos, lemma, ner, parse, dcoref, sentiment";
  
  private static final String ENTITY_TAG = "PERSON";

  public TextMiner()
  {
    entityManager = new EntityManager();
    corefIdMapping = new HashMap<>();
  }

  public void setDirectory(String directory)
  {
    this.directory = directory;
  }

  public void mineText(boolean writeInFile, String directText)
  {
    try
    {
      PrintWriter out;
      if (writeInFile)
      {
        out = new PrintWriter("output1.txt");
      }
      else
      {
        out = new PrintWriter(System.out);
      }

      Properties props = new Properties();
      props.setProperty("annotators", ANNOTATORS);
//      props.setProperty("regexner.mapping", "regexner.txt");

      StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

      List<Annotation> fileAnnotations = new ArrayList<>();
      if (directText != null)
      {
        fileAnnotations.add(new Annotation(directText));
      }
      else
      {
        File directoryFile = new File(directory);
        File[] listFiles = directoryFile.listFiles();
        for (File file : listFiles)
        {
          String fileText = IOUtils.slurpFileNoExceptions(file);
          fileAnnotations.add(new Annotation(fileText));
        }
      }

      int c = 0;
      for (Annotation annotation : fileAnnotations)
      {
        c++;
        System.out.println("Processing annotation: "+c);
        PrintWriter xmlOut = new PrintWriter("xmlOutputAnno"+c+".xml");
        
        //ids might change for different texts -> reset
        corefIdMapping.clear();
        
        // run all the selected Annotators on this text
        pipeline.annotate(annotation);

        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
        if (sentences != null && !sentences.isEmpty())
        {
          for (CoreMap sentence : sentences)
          {
            List<String> personNamesInSentence = new ArrayList<>();
            for (CoreMap token : sentence.get(CoreAnnotations.TokensAnnotation.class))
            {
              Integer corefClusterId = token.get(CorefClusterIdAnnotation.class);
              String name = corefIdMapping.get(corefClusterId);

              String namedEntityTag = token.get(NamedEntityTagAnnotation.class);
              String word = null;
              if (ENTITY_TAG.equals(namedEntityTag))
              {
                word = token.get(CoreAnnotations.TextAnnotation.class);

                if (corefClusterId != null && name == null)
                {
                  corefIdMapping.put(corefClusterId, word);
                }
              }
              else
              {
                word = name;
              }

              if (word != null && !personNamesInSentence.contains(word))
              {
                personNamesInSentence.add(word);
              }

            }
            for (int i = 0; i < personNamesInSentence.size(); i++)
            {
              String personName1 = personNamesInSentence.get(i);
              for (int j = i + 1; j < personNamesInSentence.size(); j++)
              {
                String personName2 = personNamesInSentence.get(j);

                entityManager.increaseRelation(personName1, personName2);
              }
            }
          }
        }
        
        pipeline.xmlPrint(annotation, xmlOut);
        IOUtils.closeIgnoringExceptions(xmlOut);
      }

      IOUtils.closeIgnoringExceptions(out);
      
      GephiExporter.exportCSV(entityManager.getEntities().values(), "test");
      WekaParser.entitiesToWeka(entityManager.getEntities().values(), "test");
    }
    catch (Exception ex)
    {
      System.out.println(ex.getMessage());
    }

  }
}
