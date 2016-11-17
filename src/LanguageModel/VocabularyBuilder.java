package LanguageModel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import com.google.common.collect.HashMultiset_CustomFieldSerializer;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multiset.Entry;
import com.google.common.collect.HashMultiset;

import Tokenizer.JavaTokenizer;



public class VocabularyBuilder {
	private static Logger LOGGER = Logger.getLogger(VocabularyBuilder.class.getName());
	
	public static Set<String> buildVocabulary(Collection<File> files, JavaTokenizer tokenizer, int threshold) throws IOException{
		Multiset<String> vocabulary = HashMultiset.create();
		
		for(File f: files){
			vocabulary.addAll(tokenizer.tokenListFromFile(f));
		}
		
		pruneElementsFromMultiset(threshold, vocabulary);
		
		LOGGER.info("Vocabulary built, with " + vocabulary.elementSet().size()
				+ " words");

		return vocabulary.elementSet();
	}
	
	public static void pruneElementsFromMultiset(final int threshold, Multiset<String> vocabulary) {
		final ArrayDeque<Entry<String>> toBeRemoved = new ArrayDeque<Entry<String>>();

		for (final Entry<String> ent : vocabulary.entrySet()) {
			if (ent.getCount() <= threshold) {
				toBeRemoved.add(ent);
			}
		}

		for (Entry<String> ent : toBeRemoved) {
			vocabulary.remove(ent.getElement(), ent.getCount());
		}
	}

}
