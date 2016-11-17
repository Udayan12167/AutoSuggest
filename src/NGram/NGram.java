package NGram;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

public class NGram<T> implements Iterable<T> {
	
	class NGramIterable implements Iterator<T>{
		int currentPos = 0;
		
		@Override
		public boolean hasNext() {
			return currentPos < end - start;
		}
		
		@Override
		public T next() {
			if (currentPos >= end - start) {
				throw (new NoSuchElementException());
			}
			final T obj = get(currentPos);
			currentPos++;
			return obj;
		}
		
		@Override
		public void remove() {
			throw (new UnsupportedOperationException());
		}
	}

	ArrayList<T> containingSentence;
	
	int start;
	int end;
	
	public NGram(ArrayList<T> sentence){
		containingSentence = sentence;
		start=0;
		end= sentence.size();
	}
	
	public NGram(ArrayList<T> sentence, int from, int to){
		checkArgument(from >= 0);
		checkArgument(from <= to);
		checkArgument(sentence.size() >= to);
		
		containingSentence = sentence;
		start = from;
		end = to;
	}
	
	public NGram(NGram<T> ngram, int from, int to) {
		containingSentence = ngram.containingSentence;
		start = ngram.start + from;
		end = ngram.end + to;
	}
	
	public NGram(int position, ArrayList<T> sentence, int size){
		checkArgument(position >= 0);
		checkArgument(position < sentence.size(), sentence);
		
		containingSentence = sentence;
		end = position + 1;
		int start = position - size + 1;
		if (start < 0)
			start = 0;
	}
	
	public T get(int index){
		checkArgument((start + index) < end);
		return containingSentence.get(start+index);
	}
	
	public NGram<T> prefix(){
		return new NGram<T>(containingSentence, start, end - 1);
	}
	
	public NGram<T> suffix() {
		return new NGram<T>(containingSentence, start + 1, end);
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(start, end, containingSentence);
	}
	
	@Override
	public Iterator<T> iterator() {
		return new NGramIterable();
	}
	
	public int size() {
		return end - start;
	}
	
	public ArrayList<T> toList() {
		return Lists.newArrayList(containingSentence.subList(start, end));
	}
	
	@Override
	public String toString() {
		final StringBuffer buffer = new StringBuffer();
		buffer.append('{');
		for (int i = start; i < end; i++) {
			buffer.append(containingSentence.get(i));
			buffer.append(", ");
		}
		buffer.append('}');
		return buffer.toString();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof NGram<?>)) {
			return false;
		}
		final NGram<T> other = (NGram<T>) obj;
		final int thisNGramSize = size();

		if (other.size() != thisNGramSize) {
			return false;
		}
		for (int i = 0; i < thisNGramSize; i++) {
			if (!other.get(i).equals(get(i))) {
				return false;
			}
		}
		return true;
	}
	
	public static NGram<String> substituteTokenWith(final NGram<String> ngram, final String from, final String substitute) {
		ArrayList<String> ngramCopy = ngram.toList();
		int nSize = ngram.size();
		for (int i = 0; i < nSize; i++) {
			String token = ngramCopy.get(i);
			if (token.contains(from)) {
				ngramCopy.set(i, token.replace(from, substitute));
			}
		}
		return new NGram<String>(ngramCopy);
	}
}
