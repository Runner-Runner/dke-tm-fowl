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
import java.util.List;
import javax.swing.JFrame;

public class TrainingTagger
{
  private ActionListener buttonListener;

  private String[] words;
  private TaggingPanel taggingPanel;

  private int wordIndex;

  private PrintWriter writer;

  private List<String> taggingLines;

  public TrainingTagger()
  {
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
    TrainingTagger trainingTagger = new TrainingTagger();
    trainingTagger.start("data/ArtemisFowl1.txt", 0);
  }

  public void start(String fileName, int startIndex)
  {
    try
    {
      File tsvFile = new File(fileName + "_tagged.tsv");

      wordIndex = startIndex;
      taggingLines = new ArrayList<>();
      taggingLines.add("map = word=0,answer=1");

      writer = new PrintWriter(tsvFile.getPath(), "UTF-8");
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
          for (String line : taggingLines)
          {
            writer.println(line);
          }
          writer.close();
          System.out.println("Stopped at word index " + wordIndex);
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
      words = fileText.split("\\s+");

      showNextEntity();
    }
    catch (IOException ex)
    {
      ex.printStackTrace();
    }
  }

  private void buttonPressed(String entityClass)
  {
    taggingLines.add(words[wordIndex] + "\t" + entityClass);
    showNextEntity();
  }

  private void showNextEntity()
  {
    for (int i = wordIndex; i < words.length; i++)
    {
      String word = words[i].trim();
      
      taggingPanel.setEntity(word);

      int begin = Math.max(i - 8, 0);
      int end = Math.min(i + 8, words.length - 1);
      String context = "";
      for (int j = begin; j <= end; j++)
      {
        context += words[j] + " ";
      }
      taggingPanel.setContext(context);

      wordIndex = i + 1;
      return;
    }
    wordIndex = -1;
  }

}
