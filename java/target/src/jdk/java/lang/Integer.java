
package java.lang;

/**
*	java.lang.Integer (only for CoffeinMarkEmbedded)
*
*/
// public final class Integer extends Number implements Comparable
public final class Integer {

	private final int value;

	public Integer(int value) {
		this.value = value;
	}

	public int intValue() {
		return value;
	}
}
