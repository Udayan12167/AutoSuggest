package LanguageModel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Logger;

import com.google.common.collect.Lists;

import org.apache.commons.lang.exception.ExceptionUtils;

import NGram.NGram;
import Tokenizer.JavaTokenizer;
import Tokenizer.Token;

public class IdentifierNGramLM extends AbstractNGramLM{
	
	public static final int CLEAN_NGRAM_THRESHOLD = 1;
	public static final int CLEAN_VOCABULARY_THRESHOLD = 1;
	
	private static final Logger LOGGER = Logger.getLogger(IdentifierNGramLM.class.getName());
	
	public IdentifierNGramLM(int size, JavaTokenizer tokenizerModule) {
		super(size, tokenizerModule);
	}

	@Override
	public void addFromSentence(ArrayList<String> sentence, boolean addNewToks) {
		for (int i = getN() - 1; i < sentence.size(); ++i) {
			NGram<String> ngram = new NGram<String>(i, sentence, getN());
			if (ngram.size() > 1) {
				addNgram(ngram, addNewToks);
			}
		}

		for (int i = getN() - 1; i > 0; i--) {
			NGram<String> ngram = new NGram<String>(sentence.size() - 1, sentence, i);
			addNgram(ngram, addNewToks);
		}
		
	}

	@Override
	protected void addNgram(NGram<String> ngram, boolean addNewVoc) {
		trie.add(ngram, addNewVoc);
	}

	@Override
	public void addSentences(Collection<ArrayList<String>> sentenceSet,
			boolean addNewVocabulary) {
		for (ArrayList<String> sent : sentenceSet) {
			addFromSentence(sent, addNewVocabulary);
		}	
	}

	@Override
	public void cutoffRare(int threshold) {
		trie.cutoffRare(threshold);		
	}

	@Override
	public double getProbabilityFor(final NGram<String> ngram) {
		return getMLProbabilityFor(ngram, false);
	}

	@Override
	public void removeNgram(final NGram<String> ngram) {
		trie.remove(ngram);
	}
	
	public void addRelevantNGrams(ArrayList<Token> lst) {

		SortedSet<Integer> identifierPositions = new TreeSet<Integer>();
		ArrayList<String> sentence = Lists.newArrayList();

		for (int i = 0; i < lst.size(); i++) {
			final Token fullToken = lst.get(i);
			sentence.add(fullToken.token);
			if (fullToken.tokenType.equals(tokenizer.getIdentifierType())) {
				identifierPositions.add(i);
			}
		}

		// Construct the rest
		for (int i = 0; i < sentence.size(); i++) {
			// Filter n-grams with no identifiers
			if (identifierPositions.subSet(i - getN() + 1, i + 1).isEmpty()) {
				continue;
			}
			NGram<String> ngram = new NGram<String>(i, sentence, getN());
			if (ngram.size() > 1) {
				addNgram(ngram, false);
			}
		}

	}
	
	public void trainModel(Collection<File> files) throws IOException {
		trie.buildVocabularySymbols(VocabularyBuilder.buildVocabulary(
				files, getTokenizer(), CLEAN_VOCABULARY_THRESHOLD));
		int i=1;
		for (File fi : files) {
			LOGGER.finer("Reading file " +i+ " :"+ fi.getAbsolutePath());
			try {
				ArrayList<Token> tokens = tokenizer.getTokenListFromFile(fi);

				addRelevantNGrams(tokens);
			} catch (final IOException e) {
				LOGGER.warning(ExceptionUtils.getFullStackTrace(e));
			}
			i+=1;
		}
		
	}

}
