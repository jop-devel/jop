
package embsys;

/**
* Interrupt handle must extend
* <code>Interrupt</code> and register itself on an iterrupt.
* <p>
* For simple blocking, all interrupts can be enabled or disabled.
* 
* 
*/

abstract public class Interrupt {

	private Interrupt() {}

	/**
	* Is a general enable of interrupts.
	* Individual masking of interrupts is device specific and is handled via {@link IOPort}.
	*/
	public static void enable() {
	}

	/**
	* Disables all interrupts (like cli on x86).
	*/
	public static void disable() {
	}
	/**
	* An object registers itself with this method.
	* The interrupt number is device dependent.
	* @return A previous installed handler or <code>null</code>.
	*/
	public static Interrupt register(int number) {
		return null;
	}
	/**
	* An interrupt causes the object's handle method to be called.
	*/
	public abstract void handle();
}
