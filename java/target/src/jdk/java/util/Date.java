
package java.util;

/**
*	java.util.Date (only for CoffeinMarkEmbedded)
*
*/
// public class Date implements Cloneable, Comparable, java.io.Serializable
public class Date {
	/**
	 * The time in milliseconds since the epoch.
	 */
	private transient int time;

	/**
	 * Creates a new Date Object representing the current time.
	 */
	public Date() {
		time = (int) System.currentTimeMillis();
	}

	public long getTime() {
		return (long) time;
	}
}
