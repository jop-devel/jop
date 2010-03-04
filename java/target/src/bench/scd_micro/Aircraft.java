package scd_micro;

/**
 * @author Filip Pizlo
 */

class Aircraft implements Comparable {
	/** The callsign. Currently, the only data we hold. */
	private final byte[] callsign;

	/** Construct with a callsign. */
	public Aircraft(final byte[] _callsign) {
		callsign = _callsign;
	}

	/** Construct a copy of an aircraft. */
	public Aircraft(final Aircraft _aircraft) {
		this(_aircraft.getCallsign());
	}

	/** Gives you the callsign. */
	public byte[] getCallsign() {
		return callsign;
	}

	/** Returns a valid hash code for this object. */
	public int hashCode() {
		int h = 0;

		for(int i=0; i<callsign.length; i++) {
			h += callsign[i];
		}

		return h;
	}

	/** Performs a comparison between this object and another. */
	public boolean equals(final Object other) {
		if (other == this) return true;
		else if (other instanceof Aircraft) {
			final byte[] cs = ((Aircraft) other).callsign;
			if (cs.length != callsign.length) return false;
			for (int i = 0; i < cs.length; i++)
				if (cs[i] != callsign[i]) return false;
			return true;
		} else return false;
	}

	/** Performs comparison with ordering taken into account. */
	public int compareTo(final Object _other) throws ClassCastException {
		final byte[] cs = ((Aircraft) _other).callsign;
		if (cs.length < callsign.length) return -1;
		if (cs.length > callsign.length) return +1;
		for (int i = 0; i < cs.length; i++)
			if (cs[i] < callsign[i]) return -1;
			else if (cs[i] > callsign[i]) return +1;
		return 0;
	}

	/** Returns a helpful description of this object. */
	public String toString() {
		return new String(callsign, 0, callsign.length);
	}
}
