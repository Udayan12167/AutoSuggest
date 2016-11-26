package NGram;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.Serializable;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import com.google.common.collect.Lists;

import java.util.Map.Entry;


public class Trie<T extends Comparable<?>> implements Serializable {

	private static final long serialVersionUID = 12L;
	
	public static class Node<T> implements Serializable {

		private static final long serialVersionUID = 1L;
		// children of this node
		public SortedMap<T, Node<T>> children = new TreeMap<T, Node<T>>();
		public long count = 0;
		public long isLast = 0;
	}
	
	// Token for unique (rare) tokens
	protected T unkSymbolId;
	
	public Trie(T unk) {
		unkSymbolId = unk;
	}
	
	// root
	private Node<T> root = new Node<T>();

	public void add(List<T> elementSequence) {
			
			root.count++;
			Node<T> currentNode = root;

			for (T token : elementSequence) {
				
				if (!currentNode.children.containsKey(token)) {
					currentNode.children.put(token, new Node<T>());
				}
				
				final Node<T> next = currentNode.children.get(token);
				next.count++;

				currentNode = next;
			}
			
			currentNode.isLast += 1;
			
	}
	
	private void checkCount(Node<T> node) {
		if (node.count >= 0) {
			return;
		}
		node.count = 0;
		throw new IllegalStateException("Removed a non-existent sequence.");
	}
	
	// Counts the number of distinct nodes with 'prefix' as prefix 
	public long countDistinctStartingWith(List<T> prefix, boolean useUNKs) {
		
		checkArgument(prefix.size() > 0);
		
		final Node<T> currentNode = getTrieNodeForInput(prefix, useUNKs);

		if (currentNode == null) {
			return 0;
		}

		if (!useUNKs && currentNode.children.containsKey(getUnkSymbolId()) ) {
			return currentNode.children.size() - 1;
		} else {
			return currentNode.children.size();
		}
	}
	
	
	// counts nodes in sub-trie of given ngramSymbols
	public long getCount(List<T> ngramSymbols, boolean useUNKs, boolean useTerminals) {
		
		final Node<T> current = getTrieNodeForInput(ngramSymbols, useUNKs);
		
		if (current == null) {
			return 0;
		}

		final long unkDiscountCount;
		if (!useUNKs) {
			final Node<T> unkUnit = current.children.get(getUnkSymbolId());

			if (unkUnit != null) {
				unkDiscountCount = unkUnit.count;
			} else {
				unkDiscountCount = 0;
			}
		} else {
			unkDiscountCount = 0;
		}

		final long totalCount;
		if (useTerminals) {
			totalCount = current.count - unkDiscountCount;
		} else {
			totalCount = current.count - current.isLast - unkDiscountCount;
		}

		checkArgument(totalCount >= 0);
		return totalCount;
	}
	
	public final Node<T> getRoot() {
		return root;
	}
	
	public final Node<T> getTrieNodeForInput(final List<T> ngramSymbols,
			final boolean useUNKs) {
		return getTrieNodeForInput(ngramSymbols, useUNKs, root);
	}
	
	
	public Node<T> getTrieNodeForInput(List<T> ngramSymbols, boolean useUNKs, Node<T> startNode) {
		
		Node<T> fromNode = startNode;
		
		for (T symbol : ngramSymbols) {
			
			if (symbol != null && fromNode.children.containsKey(symbol)) {
				fromNode = fromNode.children.get(symbol);
			} 
			else if (fromNode.children.containsKey(getUnkSymbolId()) && useUNKs) {
				fromNode = fromNode.children.get(getUnkSymbolId());
			} 
			else {
				fromNode = null;
				break;
			}
			
		}
		
		return fromNode;
	}
	
	public T getUnkSymbolId() {
		return unkSymbolId;
	}
	
	private void mergeTrieNodes(Node<T> from, Node<T> to) {
		
		checkNotNull(to).count += checkNotNull(from).count;
		
		to.isLast += from.isLast;

		for (final Entry<T, Node<T>> fromChild : from.children.entrySet()) {
			
			if (to.children.containsKey(fromChild.getKey())) {
				mergeTrieNodes(fromChild.getValue(), to.children.get(fromChild.getKey()));
			}
			
			else {
				to.children.put(fromChild.getKey(), fromChild.getValue());
			}
		}
	}
	
	public final void remove(final List<T> elementSequence) {
		root.count--;
		checkCount(root);
			
		Node<T> currentUnit = root;
		
		for (final T token : elementSequence) {
			
			if (!currentUnit.children.containsKey(token)) {
				currentUnit.children.put(token, new Node<T>());
			}
			
			final Node<T> next = currentUnit.children.get(token);
			next.count--;
			
			checkCount(next);
			currentUnit = next;
		}			
		
		currentUnit.isLast--;
		
	}
	
	
	public void cutoffRare(final int threshold) {
		cutoffRare(root, threshold);
	}

	// prune rare symbols given threshold
	private void cutoffRare(Node<T> node, int threshold) {
		
		final List<T> toBeRemoved = Lists.newArrayList();

		// Create or retrieve the UNK
		final Node<T> unkUnit;
		
		if (node.children.containsKey(getUnkSymbolId())) {
			unkUnit = node.children.get(getUnkSymbolId());
		} 
		else {
			unkUnit = new Node<T>();
		}

		// For every production that is below the threshold, merge and
		// recursively cut
		
		for (final Entry<T, Node<T>> prodEntry : node.children.entrySet()) {
		
			final T production = prodEntry.getKey();
			final Node<T> currentPos = prodEntry.getValue();
			
			if ( currentPos.count <= threshold && (!production.equals(getUnkSymbolId())) ) {
				toBeRemoved.add(production);
				mergeTrieNodes(currentPos, unkUnit);
			} 
			else {
				cutoffRare(currentPos, threshold);
			}
		}

		if (unkUnit.count > 0) {
			node.children.put(getUnkSymbolId(), unkUnit);
			cutoffRare(unkUnit, threshold);
		}

		// Remove all, from current node.
		for (final T child : toBeRemoved) {
			node.children.remove(child);
		}

	}
	
	public long sumStartingWith(List<T> prefix, boolean useUNKs) {
		
		checkArgument(prefix.size() > 0);
		
		final Node<T> unit = getTrieNodeForInput(prefix, useUNKs);

		if (unit == null) {
			return 0;
		}

		long count = unit.count - unit.isLast;

		return count;
	}
}
