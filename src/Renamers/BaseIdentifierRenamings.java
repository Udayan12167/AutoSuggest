package Renamers;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.logging.Logger;

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

}
