package tcpip;

/**
*	LinkLayer.java
*
*	communicate with jopbb via serial line.
*/


import javax.joprt.*;
import util.*;

/**
*	LinkLayer driver.
*/

public abstract class LinkLayer extends RtThread {

/**
*	
*/
	protected LinkLayer(int period) {
		super(period);
	}
}
