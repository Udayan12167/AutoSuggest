package Renamers;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.SortedSet;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;

import com.google.common.collect.Sets;

import LanguageModel.IdentifierNGramLM;
import NGram.NGram;
import Tokenizer.JavaTokenizer;
import Tokenizer.Token;
import static com.google.common.base.Preconditions.checkArgument;

public class BaseIdentifierRenamings extends AbstractIdentifierRenamings {
	
	public static final int NGRAM_SIZE = 4;
	
	JavaTokenizer tokenizer;
	
	private static final Logger LOGGER = Logger
			.getLogger(BaseIdentifierRenamings.class.getName());
	
	public BaseIdentifierRenamings(JavaTokenizer tokenizer) {
		super();
		this.tokenizer = tokenizer;
	}

	@Override
	public void buildRenamingModel(Collection<File> trainingFiles) {
		checkArgument(trainingFiles.size() > 0);
		IdentifierNGramLM dict = new IdentifierNGramLM(NGRAM_SIZE, tokenizer);
		try {
			dict.trainModel(trainingFiles);
			this.ngramLM = dict;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	public double scoreNgram(NGram<String> ngram) {
		double ngramScore = ngramLM.getProbabilityFor(ngram);
		return ngramScore;
	}
	
	public static <E> E get(Collection<E> collection, int index)
	{
	    Iterator<E> i = collection.iterator();
	    E element = null;
	    while (i.hasNext() && index-- > 0)
	    {
	        element = i.next();
	    }
	    return element;
	}
	
	public static void main(String[] args) throws IOException {
		File folder = new File("/Users/nitikasaran/Downloads/data/train/retrofit");
		File[] listOfFiles = folder.listFiles();
		List<File> files = new ArrayList<File>();
		for (int i = 0; i < listOfFiles.length; i++) {
		      if (listOfFiles[i].isFile()) {
		        files.add(listOfFiles[i]);
		      }
		}
		
		Collections.sort(files);
		PrintWriter writer = new PrintWriter("results_retrofit_4_gram.txt", "UTF-8");
		
		for(File fi: files){
			Collection<File> trainingFiles = new HashSet<File>();
			for (int i = 0; i < listOfFiles.length; i++) {
			      if (listOfFiles[i].isFile() && !fi.getName().equals(listOfFiles[i].getName())) {
			        trainingFiles.add(listOfFiles[i]);
			      }
			}
			writer.println(fi.getName());
			System.out.println(fi.getName());
			Scanner sc = new Scanner(System.in);
			JavaTokenizer tokenizer = new JavaTokenizer();
			BaseIdentifierRenamings lm = new BaseIdentifierRenamings(tokenizer);
			lm.buildRenamingModel(trainingFiles);
			ArrayList<Token> tokens = tokenizer.getTokenListFromFile(fi);
			double totalIdentifiers = 0.0;
			int k_1 = 0, k_2 = 0, k_3 = 0, k_4 = 0, k_5 = 0;
			for(Token token : tokens){
				if (token.tokenType.equals(tokenizer.getIdentifierType())){
					totalIdentifiers++;
					SortedSet<Renaming> scoreMap = lm.getRenamings(FileUtils.readFileToString(fi), token.token);
					if(token.token.equals(get(scoreMap, 1).name)){
						k_1++;
					}
					else if(token.token.equals(get(scoreMap, 2).name)){
						k_2++;
					}
					else if(token.token.equals(get(scoreMap, 3).name)){
						k_3++;
					}
					else if(token.token.equals(get(scoreMap, 4).name)){
						k_4++;
					}
					else if(token.token.equals(get(scoreMap, 5).name)){
						k_5++;
					}
					
				}
			}
			writer.println(totalIdentifiers);
			writer.println("The chance of finding the variable at the first location is: " + (k_1/totalIdentifiers));
			writer.println("The chance of finding the variable at the first location is: " + ((k_1+k_2)/totalIdentifiers));
			writer.println("The chance of finding the variable at the first location is: " + ((k_1+k_2+k_3)/totalIdentifiers));
			writer.println("The chance of finding the variable at the first location is: " + ((k_1+k_2+k_3+k_4)/totalIdentifiers));
			writer.println("The chance of finding the variable at the first location is: " + ((k_1+k_2+k_3+k_4+k_5)/totalIdentifiers));
		}
		writer.close();
	}

}
