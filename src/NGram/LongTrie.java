package NGram;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;


public class LongTrie<K> implements Serializable {

	private final Trie<Long> baseTrie = new Trie<Long>(null);

	private static final long serialVersionUID = -7194495381473625925L;

	private final BiMap<K, Long> alphabet;
	private long nextId;
	private final K unkSymbol;

	public LongTrie(final K unk) {
		nextId = Long.MIN_VALUE;
		alphabet = HashBiMap.create();
		baseTrie.unkSymbolId = nextId++;
		alphabet.put(unk, baseTrie.unkSymbolId);
		unkSymbol = unk;
	}

	// add given ngram to trie
	public void add(NGram<K> ngram, boolean introduceVoc) {
		
		List<Long> keys = getSymbolIds(ngram, introduceVoc);
		
		if (!introduceVoc) {
			// replace with unks
			for (int i = 0; i < keys.size(); i++) {
				if (keys.get(i) == null) {
					keys.set(i, baseTrie.getUnkSymbolId());
				}
			}
		}
		
		baseTrie.add(keys);
	}

	private long addSymbolId(final K element) {
		alphabet.put(element, nextId);
		nextId++;
		return nextId - 1;
	}

	// add given words to alphabet by assigning symbols
	public void buildVocabularySymbols(Set<K> words) {
		for (K elem : words) {
			addSymbolId(elem);
		}
	}

	
	public long countDistinctStartingWith(final NGram<K> ngram,
			final boolean useUNKs) {
		return baseTrie.countDistinctStartingWith(getSymbolIds(ngram, false),
				useUNKs);
	}

	public void cutoffRare(final int threshold) {
		baseTrie.cutoffRare(threshold);

		// Now scan everything and remove unwanted symbols from vocabulary.
		final Set<Long> usedSymbols = Sets.newTreeSet();
		final ArrayDeque<Trie.Node<Long>> stack = new ArrayDeque<Trie.Node<Long>>();
		stack.push(baseTrie.getRoot());

		while (!stack.isEmpty()) {
			final Trie.Node<Long> node = stack.pop();
			usedSymbols.addAll(node.children.keySet());
			for (final Trie.Node<Long> childNode : node.children.values()) {
				stack.push(childNode);
			}
		}

		final List<Long> difference = Lists.newArrayList(Sets.difference(
				alphabet.values(), usedSymbols));
		for (final long keyToRemove : difference) {
			if (keyToRemove == getUnkSymbolId()) {
				continue;
			}
			checkNotNull(alphabet.inverse().remove(keyToRemove));
		}
	}

	/**
	 * Returns the count of the n-gram in the dictionary. If a token does not
	 * exist in the dictionary then it is replaced with UNK. If UNKs do not
	 * exist at the current point then 0 is returned.
	 *
	 * @param ngram
	 * @return
	 */
	public long getCount(final NGram<K> ngram, final boolean useUNKs,
			final boolean useTerminals) {
		return baseTrie.getCount(getSymbolIds(ngram, false), useUNKs,
				useTerminals);
	}

	public Trie.Node<Long> getNGramNodeForInput(final NGram<K> ngram,
			final boolean useUNKs) {
		return baseTrie
				.getTrieNodeForInput(getSymbolIds(ngram, false), useUNKs);
	}

	public Trie.Node<Long> getNGramNodeForInput(final NGram<K> ngram,
			final boolean useUNKs, final Trie.Node<Long> fromNode) {
		return baseTrie.getTrieNodeForInput(getSymbolIds(ngram, false),
				useUNKs, fromNode);
	}

	/**
	 * Return all the possible productions from a specific prefix.
	 *
	 * @param prefix
	 * @return
	 */
	public Map<K, Long> getPossibleProductionsWithCounts(final NGram<K> prefix) {
		final Trie.Node<Long> node = baseTrie.getTrieNodeForInput(
				getSymbolIds(prefix, false), false);

		final Map<K, Long> productions = new TreeMap<K, Long>();

		if (node == null) {
			return productions;
		}

		for (final Entry<Long, Trie.Node<Long>> prodEntry : node.children
				.entrySet()) {
			final K key = alphabet.inverse().get(prodEntry.getKey());
			final long count = prodEntry.getValue().count;
			if (key != null) {
				productions.put(key, count);
			} else {
				productions.put(unkSymbol, count);
			}
		}

		return productions;
	}

	public Trie.Node<Long> getRoot() {
		return baseTrie.getRoot();
	}

	public Set<K> getRootSymbols() {
		final Set<K> rootProductions = Sets.newHashSet();
		final Trie.Node<Long> rootNode = baseTrie.getRoot();
		for (final long symbolId : rootNode.children.keySet()) {
			rootProductions.add(getSymbolFromKey(symbolId));
		}

		return rootProductions;
	}

	/**
	 * Return the symbol from the key.
	 *
	 * @param key
	 * @return
	 */
	public K getSymbolFromKey(final Long key) {
		if (key.equals(baseTrie.unkSymbolId)) {
			return unkSymbol;
		}
		return alphabet.inverse().get(key);
	}

	/**
	 * Helper function to create symbol IDs from list.
	 *
	 * @param objectList
	 * @param createIfNotFoundK
	 * @return
	 */
	// converts tokenlist (ngram) to symbollist
	public List<Long> getSymbolIds(final Iterable<K> objectList, final boolean createIfNotFound) {
		
		final List<Long> symbols = Lists.newArrayList();
		
		for (final K element : objectList) {
			
			final Long key = alphabet.get(element);
			
			if (key == null && createIfNotFound) {
				symbols.add(addSymbolId(element));
			} 
			else if (key == null) {
				symbols.add(null);
			} 
			else {
				symbols.add(key);
			}
		}
		
		return symbols;
	}

	public Long getUnkSymbolId() {
		return baseTrie.getUnkSymbolId();
	}

	public Set<K> getVocabulary() {
		return alphabet.keySet();
	}

	public boolean isUNK(final K token) {
		return !alphabet.containsKey(token);
	}

	/**
	 * Remove an n-gram from the trie. The n-gram must exist.
	 *
	 * @param ngram
	 */
	public void remove(final NGram<K> ngram) {
		final List<Long> keys = getSymbolIds(ngram, false);

		// replace nulls with unks
		for (int i = 0; i < keys.size(); i++) {
			if (keys.get(i) == null) {
				keys.set(i, baseTrie.getUnkSymbolId());
			}
		}

		baseTrie.remove(keys);
	}

	/**
	 * Substitute all the tokens in the current ngram with UNK when they do not
	 * exist in the dictionary.
	 *
	 * @param ngram
	 */
	public NGram<K> substituteWordsToUNK(NGram<K> ngram) {
		
		ArrayList<K> ngramCopy = new ArrayList<K>();
		
		for (K gram : ngram) {
			
			Long key = alphabet.get(gram);
			if (key == null) {
				ngramCopy.add(alphabet.inverse().get(baseTrie.unkSymbolId));
			} 
			else {
				ngramCopy.add(gram);
			}
		}
		return new NGram<K>(ngramCopy);
	}

	/**
	 * Return c_(ngram,*)
	 *
	 * @param ngram
	 * @param useUNKs
	 * @return
	 */
	public long sumStartingWith(final NGram<K> ngram, final boolean useUNKs) {
		return baseTrie.sumStartingWith(getSymbolIds(ngram, false), useUNKs);
	}

	@Override
	public String toString() {
		
		final StringBuffer buf = new StringBuffer();
		buf.append('[');
		
		for (Entry<Long, Trie.Node<Long>> ngramEntry : baseTrie.getRoot().children.entrySet()) {
			
			Long ngram = ngramEntry.getKey();
			List<String> prods = Lists.newArrayList();
			
			toStringHelper(alphabet.inverse().get(ngram).toString(),
					ngramEntry.getValue(), prods);
			for (final String prod : prods) {
				buf.append(prod + System.lineSeparator());
			}
		}
		buf.append(']');
		return buf.toString();
	}

	/**
	 * Helper function to convert dictionary to string.
	 *
	 * @param currentString
	 * @param currentUnit
	 * @param productions
	 */
	private void toStringHelper(String currentString,
			Trie.Node<Long> currentUnit, List<String> productions) {
		if (currentUnit.children.size() == 0) {
			productions.add(currentString + " count:" + currentUnit.count);
		} else {
			for (Entry<Long, Trie.Node<Long>> prodEntry : currentUnit.children
					.entrySet()) {
				Long prod = prodEntry.getKey();
				toStringHelper(
						currentString + ", " + alphabet.inverse().get(prod),
						prodEntry.getValue(), productions);
			}
		}
	}

}