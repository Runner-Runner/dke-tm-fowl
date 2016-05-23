package textminingtest;

import edu.stanford.nlp.hcoref.CorefCoreAnnotations.CorefClusterIdAnnotation;
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
//-Xms2048m
public class TextMiner {
	private String directory;

	private EntityManager entityManager;

	private HashMap<Integer, String> corefIdMapping;

	private static final String ANNOTATORS = "tokenize, ssplit, pos, lemma, ner, regexner, parse, dcoref, sentiment";

	private static final String ENTITY_TAG = "PERSON";

	public TextMiner() {
		entityManager = new EntityManager();
		corefIdMapping = new HashMap<>();
	}

	public void setDirectory(String directory) {
		this.directory = directory;
	}

	public void mineText(boolean writeInFile, String directText) {
		try {
			PrintWriter out;
			if (writeInFile) {
				out = new PrintWriter("output1.txt");
			} else {
				out = new PrintWriter(System.out);
			}

			Properties props = new Properties();
			props.setProperty("annotators", ANNOTATORS);
			props.setProperty("regexner.mapping", "data/regexner.txt");
			//props.setProperty("ner.model","edu/stanford/nlp/models/ner/english.all.3class.caseless.distsim.crf.ser.gz");
			//props.setProperty("sentiment.model","edu/stanford/nlp/models/sentiment/sentiment.binary.ser.gz");

			StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

			List<Annotation> fileAnnotations = new ArrayList<>();
			if (directText != null) {
				fileAnnotations.add(new Annotation(directText));
			} else {
				File directoryFile = new File(directory);
				File[] listFiles = directoryFile.listFiles();
				for (File file : listFiles) {
					String fileText = IOUtils.slurpFileNoExceptions(file);
					fileAnnotations.add(new Annotation(fileText));
				}
			}
			int relationRange = 3; //(sentences)
			int c = 0;
			for (Annotation annotation : fileAnnotations) {
				c++;
				System.out.println("Processing annotation: " + c);
				PrintWriter xmlOut = new PrintWriter("xmlOutputAnno" + c + ".xml");

				// ids might change for different texts -> reset
				corefIdMapping.clear();

				// run all the selected Annotators on this text
				pipeline.annotate(annotation);

				List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
				if (sentences != null && !sentences.isEmpty()) {
					LinkedList<HashSet<String>> namesBefore = new LinkedList<HashSet<String>>();
					LinkedList<Double> sentimentScores = new LinkedList<Double>();
					double allSentimentScore = 0;
					// iterate sentences
					for (CoreMap sentence : sentences) {
						HashSet<String> personNamesInSentence = new HashSet<String>();
						// find name entities
						for (CoreMap token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
							Integer corefClusterId = token.get(CorefClusterIdAnnotation.class);
							String name = corefIdMapping.get(corefClusterId);

							String namedEntityTag = token.get(NamedEntityTagAnnotation.class);
							String word = null;
							if (ENTITY_TAG.equals(namedEntityTag)) {
								word = token.get(CoreAnnotations.TextAnnotation.class);

								if (corefClusterId != null && name == null) {
									corefIdMapping.put(corefClusterId, word);
								}
							} else {
								word = name;
							}

							if (word != null) {
								personNamesInSentence.add(word);
							}

						}
						// get sentiment
						Tree tree = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
						double sentenceScore = RNNCoreAnnotations.getPredictedClass(tree);
						
						namesBefore.addFirst(personNamesInSentence);
						sentimentScores.addFirst(sentenceScore);
						allSentimentScore+=sentenceScore;
						
						if(namesBefore.size()>relationRange){
							namesBefore.removeLast();
							allSentimentScore-=sentimentScores.removeLast();
						}
						HashSet<String> alreadyChecked = new HashSet<String>();
						for(String personOne:personNamesInSentence){
							alreadyChecked.add(personOne);
							for(HashSet<String> sentenceBefore: namesBefore){
								for(String personTwo: sentenceBefore){
									if(!alreadyChecked.contains(personTwo)){
										entityManager.increaseRelation(personOne, personTwo, allSentimentScore/sentences.size());
									}
								}
									
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
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println(ex.getMessage());
		}

	}
}
