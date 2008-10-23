package dsvmfp.util;

import dsvmfp.util.DsvmPacket;

// Call back interface
public interface ILink {
	public void receive(DsvmPacket p);
}
