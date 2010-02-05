package scd_micro;

/** CallSign (name) of the plane. Constructor runs and instance lives in the persistent 
 * detector scope, so that call signs can be linked in the (persistent) state - StateTable.
 */
public class CallSign {

	final private byte[] val;

	public CallSign(final byte[] v) {
		val = v;
	}

	/** Returns a valid hash code for this object. */
	public int hashCode() {
		int h = 0;
		for(int i=0; i<val.length; i++) {
			h += val[i];
		}
		return h;
	}

	/** Performs a comparison between this object and another. */
	public boolean equals(final Object other) {
		if (other == this) return true;
		else if (other instanceof CallSign) {
			final byte[] cs = ((CallSign) other).val;
			if (cs.length != val.length) return false;
			for (int i = 0; i < cs.length; i++)
				if (cs[i] != val[i]) return false;
			return true;
		} else return false;
	}

	/** Performs comparison with ordering taken into account. */
	public int compareTo(final Object _other) throws ClassCastException {
		final byte[] cs = ((CallSign) _other).val;
		if (cs.length < val.length) return -1;
		if (cs.length > val.length) return +1;
		for (int i = 0; i < cs.length; i++)
			if (cs[i] < val[i]) return -1;
			else if (cs[i] > val[i]) return +1;
		return 0;
	}
}
