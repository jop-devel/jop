/*
 * Created on 12.07.2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package tal;

/**
 * @author martin
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class TalIo {
	
	boolean[] in;
	boolean[] out;
	int[] analog;
	boolean[] led;
	
	TalIo() {
		in = new boolean[10];
		out = new boolean[4];
		analog = new int[3];
		led = new boolean[14];
		for (int i=0; i<10; ++i) in[i]	= false;	
		for (int i=0; i<4; ++i) out[i]	= false;	
		for (int i=0; i<3; ++i) analog[i]	= 0;	
		for (int i=0; i<14; ++i) led[i]	= false;	
	}

}
