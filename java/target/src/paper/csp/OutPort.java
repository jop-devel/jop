package csp;

public interface OutPort {
	
	// all sends are non blocking in the sense that
	// they do not wait for the destination to receive the message
	public void noAck_send(int buffer[], int cnt);

}
