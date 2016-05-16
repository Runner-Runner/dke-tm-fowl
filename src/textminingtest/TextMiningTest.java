package textminingtest;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.util.CoreMap;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author daniel
 */
public class TextMiningTest
{

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args)
  {
    test4();
  }

  public static void test4()
  {
    TextMiner textMiner = new TextMiner();
    textMiner.setDirectory("data/small");
    textMiner.mineText(false, null);
//    textMiner.mineText(true, "Because John is tall. He talks to David.");
  }
  
  
  
  public static void test3()
  {
    try
    {
      String[] args = {"SmallTest.txt", "testOutput/Output1.txt", "testOutput/XmlOutput2.xml"};
//      String[] args = {};
      StanfordCoreNlpDemoModified.execute(args);
    }
    catch (IOException ex)
    {
      Logger.getLogger(TextMiningTest.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
  
  public static void test2()
  {
    try
    {
      PrintWriter xmlOut = new PrintWriter("xmlOutput.xml");
      Properties props = new Properties();
      props.setProperty("annotators",
              "tokenize, ssplit, pos, lemma, ner, parse");
      StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
      
      String text = new Scanner(new File("ArtemisFowl1.txt")).useDelimiter("\\Z").next();

      Annotation annotation = new Annotation(text);
      pipeline.annotate(annotation);
      pipeline.xmlPrint(annotation, xmlOut);
      // An Annotation is a Map and you can get and use the
// various analyses individually. For instance, this
// gets the parse tree of the 1st sentence in the text.
      List<CoreMap> sentences = annotation.get(
              CoreAnnotations.SentencesAnnotation.class);
      if (sentences != null && sentences.size() > 0)
      {
        String output = "";
        for (CoreMap sentence : sentences)
        {
          for (CoreLabel token : sentence.get(TokensAnnotation.class))
          {
            String namedEntity = token.get(NamedEntityTagAnnotation.class);
            String word = token.get(TextAnnotation.class);
            
            output += "ent: "+namedEntity+", word:"+word+"\n";
            
          }
        }
        System.out.println(output);

//        CoreMap sentence = sentences.get(0);
//
//        Tree tree = sentence.get(TreeAnnotation.class);
//        PrintWriter out = new PrintWriter(System.out);
//        out.println("The first sentence parsed is:");
//        tree.pennPrint(out);
      }
    }
    catch (IOException ex)
    {
      System.out.println(ex.getMessage());
    }
  }

  public static void test1()
  {
    String a = "How does one describe Artemis Fowl? Various psychiatrists have tried and failed. The main problem is Artemis's own intelligence. He bamboozles every test thrown at him. He has puzzled the greatest medical minds and sent many of them gibbering to their own hospitals.";
    MaxentTagger tagger = new MaxentTagger("english-bidirectional-distsim.tagger");
    String tagged = tagger.tagString(a);
    System.out.println(tagged);
  }

}
