package textminingtest;

import edu.stanford.nlp.hcoref.CorefCoreAnnotations.CorefClusterIdAnnotation;
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.LastTaggedAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Pair;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
//-Xms2048m
public class TextMiner {
	private String directory;

	private EntityManager entityManager;

	private HashMap<Integer, String> corefIdMapping;
	
	private HashSet<String> stopwords;

	private static final String ANNOTATORS = "tokenize, ssplit, pos, lemma, ner, regexner, parse, dcoref, sentiment";

	private static final String ENTITY_TAG = "PERSON";
	
	private HashSet<String> doubleNames;
	private HashMap<String, String> aliases;
	
	
	public TextMiner() {
		entityManager = new EntityManager();
		corefIdMapping = new HashMap<>();
		stopwords = readStopwords("data/stopwords.txt");
		doubleNames = new HashSet<>();
		doubleNames.add("Artemis Fowl");
		doubleNames.add("Holly Short");
		doubleNames.add("Nguyen Xuan");
		doubleNames.add("Xuan Nguyen");
		doubleNames.add("James Bond");
		aliases = new HashMap<>();
		//aliases.put(key, value)
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
			int relationRange = 5; //(sentences)
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
						CoreMap lastPerson = null;
						// find name entities
						for (CoreMap token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
							Integer corefClusterId = token.get(CorefClusterIdAnnotation.class);
							String name = corefIdMapping.get(corefClusterId);
							if(name!=null){
								token.set(CoreAnnotations.TextAnnotation.class, name);
								token.set(CoreAnnotations.LemmaAnnotation.class, name);
							}
							String namedEntityTag = token.get(NamedEntityTagAnnotation.class);
							String word = null;
							if (ENTITY_TAG.equals(namedEntityTag)) {
								word = token.get(CoreAnnotations.TextAnnotation.class);
								//check for double name
								if(lastPerson!=null){
									String doubleName = lastPerson.get(CoreAnnotations.TextAnnotation.class)+" "+word;
									if(doubleNames.contains(doubleName)){
										token.set(CoreAnnotations.TextAnnotation.class, lastPerson.get(CoreAnnotations.TextAnnotation.class));
										token.set(CoreAnnotations.LemmaAnnotation.class, lastPerson.get(CoreAnnotations.TextAnnotation.class));
									}
								}
								if (corefClusterId != null && name == null) {
									corefIdMapping.put(corefClusterId, word);
								}
								lastPerson = token;
							} else {
								lastPerson = null;
								word = name;
							}

							if (word != null) {
								personNamesInSentence.add(word);
							}
							
						}
						// get sentiment
						Tree tree = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
						//tree.pennPrint();
						double sentenceScore = RNNCoreAnnotations.getPredictedClass(tree);
						
						
						entityManager.addEntities(personNamesInSentence);
						HashSet<String> alreadyChecked = new HashSet<String>();
						double gamma = 1;
						
						//update names before
						namesBefore.addFirst(personNamesInSentence);
						sentimentScores.addFirst(sentenceScore);
						allSentimentScore+=sentenceScore;
						if(namesBefore.size()>relationRange){
							namesBefore.removeLast();
							allSentimentScore-=sentimentScores.removeLast();
						}
						
						for(String personOne:personNamesInSentence){
							alreadyChecked.add(personOne);
							// sentences before
							for(HashSet<String> sentenceBefore: namesBefore){
								gamma*=0.8;
								for(String personTwo: sentenceBefore){
									if(!personOne.equals(personTwo)){
										entityManager.increaseRelation(personOne, personTwo, (allSentimentScore/sentences.size())*gamma);
									}
								}
							}
						}
						checkDependencies(sentence);
					}
				}

				pipeline.xmlPrint(annotation, xmlOut);
				IOUtils.closeIgnoringExceptions(xmlOut);
			}

			IOUtils.closeIgnoringExceptions(out);

			GephiExporter.exportCSV(entityManager.getEntities().values(), "test");
			WekaParser.entitiesToWeka(entityManager.getEntities().values(), "test");
			PrintWriter pw = new PrintWriter("test-descriptors.txt", "UTF-8");
			// print person descriptors
			for(NamedEntity ne : entityManager.getEntities().values()){
				pw.println(ne.getName());
				for(String s:ne.getDescriptors()){
					pw.println("\t"+s);
				}
			}
			// print relation descriptors
			for(Relation r:entityManager.getRelations()){
				pw.println(r.getEntity1().getName()+" <-> "+r.getEntity2().getName());
				for(String s:r.getDescriptors()){
					pw.println("\t"+s);
				}
			}
			pw.close();
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println(ex.getMessage());
		}

	}
	private void checkDependencies(CoreMap sentence) {
		Tree tree = sentence.get(TreeAnnotation.class);
		// Get dependency tree
		TreebankLanguagePack tlp = new PennTreebankLanguagePack();
		GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
		GrammaticalStructure gs = gsf.newGrammaticalStructure(tree);
		Collection<TypedDependency> td = gs.typedDependenciesCollapsed();
		System.out.println(td);

		Object[] list = td.toArray();
		TypedDependency typedDependency;
		HashMap<String,List<NamedEntity>> relationMapping = new HashMap<>();
		HashMap<String, NamedEntity> adjectives = new HashMap<>();
		HashSet<String> neg = new HashSet<String>();
		for (Object object : list) {
			typedDependency = (TypedDependency) object;
			String firstDep = typedDependency.dep().lemma();
			String firstTag = typedDependency.dep().tag();
			String secondTag = typedDependency.gov().tag();
			String secondDep = typedDependency.gov().lemma();
			String rel = typedDependency.reln().getShortName();
			if(rel.equals("neg")){
				neg.add(firstDep);
				neg.add(secondDep);
			}
			NamedEntity one = entityManager.getEntity(firstDep);
			NamedEntity two = entityManager.getEntity(secondDep);
			if(one == null && two!=null && firstDep!=null /*&& !stopwords.contains(firstDep)*/){
				if(firstTag.startsWith("J"))
					adjectives.put(firstDep, two);
				else if(firstTag.startsWith("VB")){
					List<NamedEntity> mapped = relationMapping.get(firstDep);
					if(mapped == null){
						mapped = new ArrayList<NamedEntity>();
						relationMapping.put(firstDep, mapped);
					}
					if(!mapped.contains(two))
						mapped.add(two);
				}
			}
			else if(one != null && two==null && secondDep!=null/* && !stopwords.contains(secondDep)*/){
				if(secondTag.startsWith("J"))
					adjectives.put(secondDep, one);
				else if(secondTag.startsWith("VB")){
					List<NamedEntity> mapped = relationMapping.get(secondDep);
					if(mapped == null){
						mapped = new ArrayList<NamedEntity>();
						relationMapping.put(secondDep, mapped);
					}
					if(!mapped.contains(one))
						mapped.add(one);
				}
			}
		}
		for(Entry<String,NamedEntity> entry:adjectives.entrySet()){
			if(!neg.contains(entry.getKey()))
				entry.getValue().addDescriptor(entry.getKey());
		}
		for(Entry<String, List<NamedEntity>> entry: relationMapping.entrySet()){
			if(!neg.contains(entry.getKey())&&entry.getValue().size()>1){
				System.out.println(entry.getValue().get(0).getName());
				System.out.println(entry.getValue().get(1).getName());
				System.out.println(entry.getKey());
				entry.getValue().get(0).getRelation(entry.getValue().get(1)).addDescriptor(entry.getKey());
			}
		}
	}

	private HashSet<String> mapPersons(List<String> entityNames){
		HashSet<String> persons = new HashSet<>();
		String before = null;
		for(String name: entityNames){
			if(doubleNames.contains(before+" "+name)){
				continue;
			}
			else{
				if(aliases.get(name)!=null)
					name = aliases.get(name);
				persons.add(name);
				before = name;
			}
		}
		return persons;
	}
	
	public static HashSet<String> readStopwords(String path)
	  {
	    HashSet<String> stopwords = new HashSet<String>();
	    BufferedReader br = null;
	    try
	    {
	      br = new BufferedReader(new FileReader(path));
	      String line = br.readLine();
	      while (line != null)
	      {
	        stopwords.add(line.trim());
	        line = br.readLine();
	      }
	      br.close();
	    }
	    catch (Exception e)
	    {
	      System.out.println("Stopwords could not be read.");
	    }
	    return stopwords;
	  }
}
