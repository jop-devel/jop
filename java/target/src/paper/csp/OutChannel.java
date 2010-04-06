package csp;

abstract public class OutChannel {
	InChannel inEnd;
	
	
	public OutChannel(InChannel inEnd) {
		// the other end
		this.inEnd = inEnd;
	}
	// all sends are non blocking in the sense that
	// they do not wait for the destination to receive the message
	protected abstract void nb_send(int buffer[], int cnt);

	// send does await for ack from the InChannel however!
	public void send(int buffer[], int cnt) {
		// do the real send
		nb_send(buffer,cnt);
		// wait for ack
		inEnd.nb_receive();
	}
}
