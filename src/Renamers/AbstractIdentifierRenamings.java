package Renamers;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.SortedSet;
import java.util.logging.Logger;

import org.apache.commons.lang.exception.ExceptionUtils;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multiset.Entry;
import com.google.common.collect.Multisets;
import com.google.common.collect.Sets;
import com.google.common.collect.TreeMultiset;
import com.google.common.math.DoubleMath;

import LanguageModel.AbstractNGramLM;
import NGram.NGram;

public abstract class AbstractIdentifierRenamings {
	
	public static final String WILDCARD_TOKEN = "%WC%";
	
	private static final Logger LOGGER = Logger
			.getLogger(AbstractIdentifierRenamings.class.getName());
	
	protected AbstractNGramLM ngramLM;
	
	public abstract void buildRenamingModel(Collection<File> training);
	
	public SortedSet<Renaming> calculateScores(Multiset<NGram<String>> ngrams, Set<String> alternatives, String scope) {
		SortedSet<Renaming> scoreMap = Sets.newTreeSet();

		for (final String identifierName : alternatives) {
			double score = 0;
			for (final Entry<NGram<String>> ngram : ngrams.entrySet()) {
				try {
					final NGram<String> identNGram = NGram.substituteTokenWith(
							ngram.getElement(), WILDCARD_TOKEN, identifierName);
					final double ngramScore = scoreNgram(identNGram);
					score += DoubleMath.log2(ngramScore) * ngram.getCount();
				} catch (final Throwable e) {
					LOGGER.warning(ExceptionUtils.getFullStackTrace(e));
				}
			}
			scoreMap.add(new Renaming(identifierName, 
									  (0 - score) / ngrams.size(), 
									  ngrams.size() / ngramLM.getN(), 
									  scope));
		}

		return scoreMap;
	}
	
	public Multiset<String> getAlternativeNames(
			Multiset<NGram<String>> relevantNgrams,
			String currentName) {
		// Get all alternative namings
		Multiset<String> nameAlternatives = ngramLM.getAlternativeNamings(relevantNgrams, WILDCARD_TOKEN);
		nameAlternatives.add(currentName); // Give the current identifier a
											// chance...

		// Prune naming alternatives
		Multiset<String> toKeep = TreeMultiset.create();

		int seen = 0;
		for (Entry<String> ent : Multisets.copyHighestCountFirst(
				nameAlternatives).entrySet()) {
			if (seen > 1000) {
				break;
			}
			toKeep.add(ent.getElement(), ent.getCount());
			seen++;
		}
		toKeep.add(AbstractNGramLM.UNK_Symbol);
		return toKeep;
	}

	public AbstractNGramLM getLM() {
		return ngramLM;
	}
	
	public Multiset<NGram<String>> getNgramsAtPosition(SortedSet<Integer> targetPositions, ArrayList<String> tokens) {
		Multiset<NGram<String>> ngrams = HashMultiset.create();
		for (int ngramPos : targetPositions) {
			for (int i = 0; i < ngramLM.getN(); i++) {
				int nGramPosition = ngramPos + i;
				if (nGramPosition >= tokens.size()) {
					break;
				}
				NGram<String> ngram = new NGram<String>(nGramPosition, tokens, ngramLM.getN());
				ngrams.add(ngram);

				if (ngram.size() <= 1) {
					continue;
				}
			}
		}

		return ngrams;
	}
	
	public SortedSet<Renaming> getRenamings(String scope,
			final String targetIdentifier) {
		// Get the snippet n-grams
		final Multiset<NGram<String>> relevantNgrams = getSnippetNGrams(
				scope, targetIdentifier);

		final Multiset<String> toKeep = getAlternativeNames(relevantNgrams,
				targetIdentifier);

		// calculate scores for each naming
		return calculateScores(relevantNgrams, toKeep.elementSet(), scope);
	}
	
	public Multiset<NGram<String>> getSnippetNGrams(String snippet, String targetIdentifier) {
		ArrayList<String> lst = checkNotNull(ngramLM).getTokenizer().tokenList(snippet.toCharArray());

		SortedSet<Integer> identifierPositions = Sets.newTreeSet();
		ArrayList<String> sentence = Lists.newArrayList();

		for (int i = 0; i < lst.size(); i++) {
			final String token = lst.get(i);
			sentence.add(token);
			if (token.equals(targetIdentifier)
					|| token.contains("%" + targetIdentifier + "%")) {
				identifierPositions.add(i);
				sentence.set(i, token.replace(targetIdentifier, WILDCARD_TOKEN));
			}
		}
		return getNgramsAtPosition(identifierPositions, sentence);
	}
	
	public boolean isTrueUNK(final String token) {
		return checkNotNull(ngramLM).getTrie().isUNK(token);
	}
	
	public abstract double scoreNgram(final NGram<String> ngram);
}
