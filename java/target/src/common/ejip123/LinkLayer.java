package ejip123;

/** Template class for linklayers. Every linklayer needs to inherit from this class. */
public abstract class LinkLayer{

/** IP address of this interface. */
private int ip;

/** IP address of the gateway to non local nets. */
private int gateway;

/** Subnet mask of this interface. */
private int netmask;

/**
 Minimum transfer unit. Number of bytes of <b>payload</b> the local link can handle in one frame. Defaults to 576B
 because that's the minimum IP packet size every host needs to be able to process, hence it's reasonable to assume the
 underlying layer can transport frames of that size without fragmentation.
 */
private int mtu = 576;

public int getIp(){
	return ip;
}

protected abstract void loop();
//	public abstract void loop();

public boolean isLocalBroadcast(int dstAddr){
	Util.wrIp(dstAddr);
	if(dstAddr == 0xffffffff){
//		Dbg.wr("limited BC ");
		return true;
	}

	int bcAdr = ip|~netmask;
//	if(bcAdr == dstAddr)
//		Dbg.wr("net BC ");
//	else
//		Dbg.wr("!BC ");
	return bcAdr == dstAddr;

}
/*
	 * @param remoteAddr
	 * @return true if the remote address is in the same subnet or if no gateway
	 *         is given
	 */
/*
	public boolean isSameSubnet(int remoteAddr) {
		if (gateway == 0)
			return true;

//		int test1 = ip & netmask;
		int test1 = netmask;

		int test2 = remoteAddr & netmask;

		return (test1 ^ test2) == 0;
	}
*/

protected void setIp(int ip){
	this.ip = ip;
}

public int getGateway(){
	return gateway;
}

protected void setGateway(int gateway){
	this.gateway = gateway;
}

public int getNetmask(){
	return netmask;
}

protected void setNetmask(int netmask){
	this.netmask = netmask;
}

public int getMtu(){
	return mtu;
}

protected void setMtu(int mtu){
	this.mtu = mtu;
}
}
