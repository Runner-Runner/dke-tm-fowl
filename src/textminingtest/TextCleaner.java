package textminingtest;

import edu.stanford.nlp.io.IOUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import sun.nio.ch.IOUtil;

public class TextCleaner
{
  public static void main(String[] args)
  {
    try
    {
//      cleanTxt("data/small/1-the-book.txt", "data/small/cleanTest.txt");
      cleanTxtInDir("data/ArtemisFowl3/chapters/original");
    }
    catch (IOException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public static void cleanTxtInDir(String directory) throws IOException
  {
    File dirFile = new File(directory);
    String prefix = directory+"_clean";
    File outputDirFile = new File(prefix);
    IOUtils.ensureDir(outputDirFile);
    for(File file : dirFile.listFiles())
    {
      cleanTxt(file, prefix + "/" + file.getName());
    }
  }

  public static void cleanTxt(String input, String output) throws IOException
  {
    cleanTxt(new File(input), output);
  }
          
public static void cleanTxt(File inputFile, String output) throws IOException
  {
    BufferedReader br = new BufferedReader(new FileReader(inputFile));
    PrintWriter writer = new PrintWriter(output, "UTF-8");
    try
    {
      String line = br.readLine();

      while (line != null)
      {
        line = line.replaceAll("\\d", "");
        if (line.isEmpty() || line.equals("Artemis Fowl") || line.startsWith("CHAPTER") || line.startsWith("Chapter :"))
        {
          line = br.readLine();
          continue;
        }
        writer.println(line);
        line = br.readLine();
      }

    }
    finally
    {
      br.close();
    }
    writer.close();
    System.out.println("clean");
  }
}
