package javax.realtime.precedence;

public class Precedence {
	public final DepWord [] prefix;
	public final DepWord [] pattern;
	public Precedence(DepWord [] prefix, DepWord [] pattern) {
		this.prefix = prefix;
		this.pattern = pattern;
	}
}
