package prelude;

import javax.realtime.precedence.Precedence;

public class PreludePrecedence {
	public final String pred;
	public final String succ;
	public final Precedence prec;
	
	public PreludePrecedence(String pred, String succ, Precedence prec) {
		this.pred = pred;
		this.succ = succ;
		this.prec = prec;
	}
}
