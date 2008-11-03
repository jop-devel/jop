package wcet.dsvmfp.util;

import wcet.dsvmfp.util.DsvmPacket;

// Call back interface
public interface ILink {
	public void receive(DsvmPacket p);
}
