package ejip123;

/** An entry in the routing table. */
public class Route{
private int net;
private int mask;
private LinkLayer linkLayer;

public Route(int net, int mask, LinkLayer linkLayer){
	this.net = net;
	this.mask = mask;
	this.linkLayer = linkLayer;
}

public int getNet(){
	return net;
}

public void setNet(int net){
	this.net = net;
}

public int getMask(){
	return mask;
}

public void setMask(int mask){
	this.mask = mask;
}

public LinkLayer getLinkLayer(){
	return linkLayer;
}

public void setLinkLayer(LinkLayer linkLayer){
	this.linkLayer = linkLayer;
}
}
