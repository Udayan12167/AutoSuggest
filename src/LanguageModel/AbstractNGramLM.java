package LanguageModel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AbstractFileFilter;
import org.apache.commons.lang.math.RandomUtils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;
import com.google.common.collect.TreeMultiset;
import com.google.common.math.DoubleMath;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import NGram.NGram;
import Tokenizer.JavaTokenizer;
import Trie.LongTrie;
import Trie.Trie.Node;


public abstract class AbstractNGramLM {
	public static final String UNK_Symbol = "UNK_Symbol";
	
	private static final Logger LOGGER = Logger.getLogger(AbstractNGramLM.class
			.getName());
	
	int nGramSize;
	
	JavaTokenizer tokenizer;
	
	LongTrie<String> trie;
	
	public AbstractNGramLM() {
		
	}
	
	AbstractNGramLM(AbstractNGramLM original){
		nGramSize = original.nGramSize;
		trie = original.trie;
		tokenizer = original.tokenizer;
	}
	
	public AbstractNGramLM(int size, JavaTokenizer tokenizerModule) {
		nGramSize = size;
		trie = new LongTrie<String>(UNK_Symbol);
		tokenizer = tokenizerModule;
	}
	
	public abstract void addFromSentence(ArrayList<String> sentence, boolean addNewVoc);
	
	protected abstract void addNgram(final NGram<String> ngram, boolean addNewVoc);
	
	public abstract void addSentences(
			final Collection<ArrayList<String>> sentenceSet,
			final boolean addNewVocabulary);
	
	public abstract void cutoffRare(int threshold);
	
	public double getAbsoluteEntropy(final File file) throws IOException {
		return getAbsoluteEntropy(FileUtils.readFileToString(file));
	}
	
	public double getAbsoluteEntropy(final String fileContent) {
		char[] code = fileContent.toCharArray();
		if (code.length == 0) {
			return 0;
		}
		ArrayList<String> tokens = getTokenizer().tokenList(code);
		if (tokens.isEmpty()) {
			return 0;
		}
		double sentenceProb = getLogProbOfSentence(tokens);

		return sentenceProb;
	}
	
	public Multiset<String> getAlternativeNamings(Multiset<NGram<String>> ngrams, String tokenToSubstitute) {
		final Multiset<String> namings = TreeMultiset.create();
		final LongTrie<String> globalTrie = getTrie();

		for (final Multiset.Entry<NGram<String>> ngramEntry : ngrams.entrySet()) {
			final NGram<String> ngram = ngramEntry.getElement();

			final Set<String> alternatives = checkNotNull(getAlternativesForNGram(
					globalTrie, ngram, tokenToSubstitute));
			namings.addAll(alternatives);
		}

		return namings;
	}
	
	public Set<String> getAlternativesForNGram(LongTrie<String> globalTrie, NGram<String> ngram, String tokenToSubstitute) {
		// First get the n-gram up to the wildcard
		NGram<String> prefix = ngram;
		while (!prefix.get(prefix.size() - 1).contains(tokenToSubstitute)) {
			prefix = prefix.prefix();
		}
		prefix = prefix.prefix(); // Remove the substitute token

		// Then get the node that has as children all possible names
		Node<Long> sNode = globalTrie.getNGramNodeForInput(prefix, false);
		if (sNode == null) {
			return Collections.emptySet();
		}

		// Then for each child construct one ngram replacing the wildcard
		// unless this is the "last" N
		Set<String> renamings = Sets.newTreeSet();
		if (prefix.size() != getN() - 1) {

			int prefixSize = prefix.size();
			int ngramSize = ngram.size();
			NGram<String> suffix = new NGram<String>(ngram, prefixSize + 1, ngramSize);

			for (Map.Entry<Long, Node<Long>> entry : sNode.children.entrySet()) {
				String token = globalTrie
						.getSymbolFromKey(entry.getKey());
				NGram<String> replacedNgramSuffix = NGram
						.substituteTokenWith(suffix, tokenToSubstitute, token);
				Node<Long> fNode = globalTrie.getNGramNodeForInput(replacedNgramSuffix, false, entry.getValue());
				if (fNode != null) {
					renamings.add(token);
				}
			}
		} else {
			for (Long key : sNode.children.keySet()) {
				String token = globalTrie.getSymbolFromKey(key);
				renamings.add(token);
			}
		}
		return renamings;
	}
	
	public double getExtrinsticEntropy(final File file) throws IOException {
		return getExtrinsticEntropy(FileUtils.readFileToString(file));
	}
	
	public double getExtrinsticEntropy(final String fileContent) {
		char[] code = fileContent.toCharArray();
		if (code.length == 0) {
			return 0;
		}
		ArrayList<String> tokens = getTokenizer().tokenList(code);
		if (tokens.isEmpty()) {
			return 0;
		}
		double sentenceProb = getLogProbOfSentence(tokens);

		return sentenceProb / (tokens.size() - 1.);
	}
	
	public ArrayList<Double> getLogProbDistOfSentence(String fileContent) {
		ArrayList<Double> logProbDist = new ArrayList<Double>();

		char[] code = fileContent.toCharArray();
		if (code.length == 0) {
			return logProbDist;
		}
		ArrayList<String> tokens = getTokenizer().tokenList(code);
		if (tokens.isEmpty()) {
			return logProbDist;
		}

		for (int i = 0; i < tokens.size(); ++i) {
			NGram<String> ngram = new NGram<String>(i, tokens, nGramSize);
			if (ngram.size() > 1) {
				double prob = getProbabilityFor(ngram);
				checkArgument(prob > 0);
				checkArgument(!Double.isInfinite(prob));
				logProbDist.add(DoubleMath.log2(prob));
			}
		}
		return logProbDist;
	}
	
	public double getLogProbOfSentence(ArrayList<String> sentence){
		double logProb = 0;
		for (int i = 0; i < sentence.size(); ++i){
			NGram<String> ngram = new NGram<String>(i, sentence, nGramSize);
			if (ngram.size() > 1) {
				final double prob = getProbabilityFor(ngram);
				checkArgument(prob > 0);
				checkArgument(!Double.isInfinite(prob));
				logProb += DoubleMath.log2(prob);
			}
		}
		return logProb;
	}
	
	public double getMLProbabilityFor(NGram<String> ngram, boolean useUNKs) {
		checkNotNull(ngram);
		if (ngram.size() == 1) {
			ngram = trie.substituteWordsToUNK(ngram);
		}
		final long thisNgramCount = trie.getCount(ngram, ngram.size() == 1,
				true);

		if (thisNgramCount > 0) {
			final long productionCount = trie.getCount(ngram.prefix(),
					ngram.size() == 1, false);
			checkArgument(productionCount >= thisNgramCount);

			final double mlProb = ((double) thisNgramCount)
					/ ((double) productionCount);
			checkArgument(!Double.isInfinite(mlProb));
			return mlProb;
		} else {
			checkArgument(ngram.size() > 1);
			return 0.4 * getProbabilityFor(ngram.suffix());

		}
	}
	
	public final int getN() {
		return nGramSize;
	}
	
	public abstract double getProbabilityFor(final NGram<String> ngram);
	
	public JavaTokenizer getTokenizer(){
		return tokenizer;
	}
	
	public LongTrie<String> getTrie(){
		return trie;
	}
	
	public AbstractFileFilter modelledFilesFilter() {
		return getTokenizer().getFileFilter();
	}
	
	public String pickRandom(NGram<String> prefix) {
		Map<String, Long> productions = trie.getPossibleProductionsWithCounts(prefix);
		
		if (productions.size() == 0) {
			return pickRandom(prefix.suffix());
		}
		double sum = 0;
		for (Entry<String, Long> entry : productions.entrySet()) {
			sum += entry.getValue();
		}
		
		double randomPoint = RandomUtils.nextDouble() * sum;
		long currentSum = 0;
		for (Entry<String, Long> entry : productions.entrySet()) {
			currentSum += entry.getValue();
			if (currentSum >= randomPoint) {
				return entry.getKey();
			}
		}

		throw new IllegalStateException(
				"Should never reach this point. Picking random production failed.");
	}
	
	public abstract void removeNgram(final NGram<String> ngram);
	
	public String toString() {
		return trie.toString();
	}
	

}
