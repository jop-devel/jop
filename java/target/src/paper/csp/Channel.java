package csp;

public class Channel {
		OutPort Src;
		InPort Dst;
		
		public Channel(OutPort Src, InPort Dst) {
			this.Src = Src;
			this.Dst = Dst;
		}
		
		// these are the true CSP operations
		// with ACK on the message, real rendezvous
		public int[] receive() {
			int r[] = Dst.noAck_receive();
			System.out.println("ch received something. sending ack.");
			// in true CSP semantics, this receive must be acknowledged
			// back to the sender
			Src.noAck_send(r, 0);
			return r;
		}
		
		
		// send does await for ack from the InChannel however!
		public void send(int buffer[], int cnt) {
			// do the real send
			Src.noAck_send(buffer,cnt);
			System.out.println("ch sent. waiting for ack.");
			// wait for ack
			Dst.noAck_receive();
		}

}
