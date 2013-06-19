package prelude;

public class ComPattern {
	public final boolean[] prefix;
	public final boolean[] pattern;

	public ComPattern(boolean[] prefix, boolean[] pattern) {
		this.prefix = prefix;
		this.pattern = pattern;
	}

	public boolean mustUpdate(int n) {
		int preflen = prefix == null ? 0 : prefix.length;
		if (n < preflen)
			return prefix[n];
		else
			return pattern[(n-preflen)%pattern.length];
	}

}
