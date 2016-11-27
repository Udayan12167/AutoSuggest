package Renamers;

import com.google.common.base.Objects;
import com.google.common.collect.ComparisonChain;


public class Renaming implements Comparable<Renaming> {
	public String name;

	public double score;

	public int nContexts;

	public String scope;

	public Renaming(String id, double xEntropy, int contexts, String renamingScope) {
		name = id;
		score = xEntropy;
		nContexts = contexts;
		scope = renamingScope;
	}

	@Override
	public int compareTo(final Renaming other) {
		return ComparisonChain.start().compare(score, other.score)
				.compare(name, other.name).result();
	}

	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof Renaming)) {
			return false;
		}
		final Renaming r = (Renaming) other;
		return Objects.equal(name, r.name) && Objects.equal(score, r.score);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(name, score);

	}

	@Override
	public String toString() {
		return name + ":" + String.format("%.2f", score);
	}

}
