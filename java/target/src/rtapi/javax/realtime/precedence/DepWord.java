package javax.realtime.precedence;

import javax.safetycritical.annotate.SCJAllowed;

@SCJAllowed
public class DepWord {
	public final int predJob;
	public final int succJob;
	public DepWord(int p, int s) {
		predJob = p;
		succJob = s;
	}
}

