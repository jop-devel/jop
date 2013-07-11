package csp;

public interface IOInterface {
	
	public IODevice getIODevice();
	public void write(Buffer buffer);
	public Buffer read();
	
}
