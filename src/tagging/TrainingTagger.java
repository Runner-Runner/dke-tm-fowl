package tagging;

import edu.stanford.nlp.io.IOUtils;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JFrame;

//TODO Needed?
public class TrainingTagger
{
  private ActionListener buttonListener;

  private String[] words;
  private TaggingPanel taggingPanel;

  private int wordIndex;

  private PrintWriter writer;

  private List<String> taggingLines;

  private Set<String> entityNames;
  
  public TrainingTagger()
  {
    entityNames = new HashSet<>();
    taggingPanel = new TaggingPanel();
    buttonListener = new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        String entityClass = e.getActionCommand();
        buttonPressed(entityClass);
      }
    };
    taggingPanel.addActionListener(buttonListener);
  }

  public static void main(String args[])
  {
    ManualEntityTagger manualEntityTagger = new ManualEntityTagger();
    manualEntityTagger.start("data/ArtemisFowl1.txt");
  }

  public void start(String fileName)
  {
    try
    {
      File file = new File("data/EntityTagging.txt");

      if (file.exists())
      {
        String taggingText = IOUtils.slurpFileNoExceptions(file);
        taggingLines = new ArrayList<>();
        taggingLines.addAll(Arrays.asList(taggingText.split("\n")));
        
        for(int i=1; i<taggingLines.size(); i++)
        {
          String line = taggingLines.get(i);
          int index = line.indexOf("=");
          if(index == -1)
            continue;
          String name = line.substring(0, index);
          entityNames.add(name);
        }
        
        wordIndex = Integer.parseInt(taggingLines.get(0));
      }
      else
      {
        wordIndex = 0;
        taggingLines = new ArrayList<>();
        taggingLines.add("0\n");
      }

      writer = new PrintWriter("data/EntityTagging.txt", "UTF-8");
      JFrame taggingFrame = new JFrame("Manual Entity Tagger");
      taggingFrame.setLayout(new GridLayout());
      taggingFrame.setSize(265, 375);
      taggingFrame.setResizable(false);
      taggingFrame.add(taggingPanel);
      taggingFrame.addWindowListener(new WindowAdapter()
      {
        @Override
        public void windowClosing(WindowEvent e)
        {
          String taggingText = "";
          taggingText = ""+wordIndex;
          for (int i = 1; i < taggingLines.size(); i++)
          {
            taggingText += taggingLines.get(i);
          }
          writer.print(taggingText);
          writer.close();
        }
      });
      taggingFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      java.awt.EventQueue.invokeLater(new Runnable()
      {
        @Override
        public void run()
        {
          taggingFrame.setVisible(true);
        }
      });

      String fileText = IOUtils.slurpFileNoExceptions(fileName);
      words = fileText.split(" ");

      showNextEntity();
    }
    catch (IOException ex)
    {
      ex.printStackTrace();
    }
  }

  private void buttonPressed(String entityClass)
  {
    taggingLines.add("\n" + words[wordIndex] + "=" + entityClass);
    showNextEntity();
  }

  private void showNextEntity()
  {
    for (int i = wordIndex; i < words.length; i++)
    {
      String word = words[i].trim();
      if (!word.isEmpty() && Character.isUpperCase(word.charAt(0)) && 
              !entityNames.contains(word))
      {
        entityNames.add(word);
        
        taggingPanel.setEntity(word);

        int begin = Math.max(i - 8, 0);
        int end = Math.min(i + 8, words.length - 1);
        String context = "";
        for (int j = begin; j <= end; j++)
        {
          context += words[j] + " ";
        }
        taggingPanel.setContext(context);

        wordIndex = i+1;
        return;
      }
    }
    wordIndex = -1;
  }

}
