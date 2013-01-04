package csp;

/**
 * The IO device should have at least a control and status register.
 * 
 * @author jrri
 *
 */
public interface IODevice {
	
	public void setControl(int i);
	
	public int readControl();
	
	public int readStatus();

}
