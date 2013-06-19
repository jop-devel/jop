package javax.realtime.precedence;

public class DepWord {
	public final int predJob;
	public final int succJob;
	public DepWord(int p, int s) {
		predJob = p;
		succJob = s;
	}
}

