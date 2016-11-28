package Renamers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;

import LanguageModel.IdentifierNGramLM;
import NGram.NGram;
import Tokenizer.JavaTokenizer;
import static com.google.common.base.Preconditions.checkArgument;

public class BaseIdentifierRenamings extends AbstractIdentifierRenamings {
	
	public static final int NGRAM_SIZE = 5;
	
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
	
	public static void main(String[] args) throws IOException {
		File folder = new File("/Users/udayantandon/Downloads/data/train/retrofit");
		File[] listOfFiles = folder.listFiles();
		List<File> files = new ArrayList<File>();
		for (int i = 0; i < listOfFiles.length; i++) {
		      if (listOfFiles[i].isFile()) {
		        files.add(listOfFiles[i]);
		      }
		}
		
		Collections.sort(files);
		
		for(File fi: files){
			Collection<File> trainingFiles = new HashSet<File>();
			for (int i = 0; i < listOfFiles.length; i++) {
			      if (listOfFiles[i].isFile() && !fi.getName().equals(listOfFiles[i].getName())) {
			        trainingFiles.add(listOfFiles[i]);
			      }
			}
			Scanner scanner = new Scanner(System.in);
			System.out.println(fi.getName());
			System.out.print("Do you want to train model for this file: ");
		    String answer = scanner.next();
		    if(answer.equals("y")){
				JavaTokenizer tokenizer = new JavaTokenizer();
				BaseIdentifierRenamings lm = new BaseIdentifierRenamings(tokenizer);
				lm.buildRenamingModel(trainingFiles);
				System.out.println(lm.ngramLM);
				while(true){
					System.out.print("Enter Identifier for suggestions or \"end\": ");
					String identifier = scanner.next();
					if(identifier.equals("end")){
						break;
					}
					System.out.println(lm.getRenamings(FileUtils.readFileToString(fi), identifier));
				}
		    }
		}
	}

}
