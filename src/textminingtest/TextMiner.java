package textminingtest;

import edu.stanford.nlp.hcoref.CorefCoreAnnotations.CorefClusterIdAnnotation;
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

public class TextMiner
{
  private String[] fileNames;

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

  public void setFileNames(String... fileNames)
  {
    this.fileNames = fileNames;
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

      // Create a CoreNLP pipeline. To build the default pipeline, you can just use:
      //   StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
      // Here's a more complex setup example:
      //   Properties props = new Properties();
      //   props.put("annotators", "tokenize, ssplit, pos, lemma, ner, depparse");
      //   props.put("ner.model", "edu/stanford/nlp/models/ner/english.all.3class.distsim.crf.ser.gz");
      //   props.put("ner.applyNumericClassifiers", "false");
      //   StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
      // Add in sentiment
      Properties props = new Properties();
      props.setProperty("annotators", ANNOTATORS);

      StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

      List<Annotation> fileAnnotations = new ArrayList<>();
      if (directText != null)
      {
        fileAnnotations.add(new Annotation(directText));
      }
      else
      {
        for (String fileName : fileNames)
        {
          String fileText = IOUtils.slurpFileNoExceptions(fileName);
          fileAnnotations.add(new Annotation(fileText));
        }
      }

      for (Annotation annotation : fileAnnotations)
      {
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
      }

      IOUtils.closeIgnoringExceptions(out);

      GephiExporter.exportCSV(entityManager.getEntities().values(), "SmallTest");
    }
    catch (IOException ex)
    {
      System.out.println(ex.getMessage());
    }

  }
}
