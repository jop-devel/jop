package ejip123;

import ejip123.util.Dbg;

/** Implementation of a routing table. Stores net-interface-pairs to provide routing information for IP. */
public class Router{
private static int CNT;
private static final Object mutex = new Object();
private static Route[] routes;
private static LinkLayer defaultInterface = null;
//private static int minMTU = Integer.MAX_VALUE;

private Router(){
}

/**
 initializes the routing table.

 @param routeCnt Number of possible entries in the routing table. Defaults to 0, if < 0. */
public static void init(int routeCnt){
	CNT = routeCnt < 0 ? 0 : routeCnt;
	routes = new Route[CNT];
}

/**
 Returns the interface to be used to send an IP packet.

 @param dstIp Destination IP address.
 @return The interface to be used or null if there is no corresponding route and no default interface is set. */
public static LinkLayer getIf(int dstIp){
	synchronized(mutex){
		for(int i = 0; i < CNT; i++){
			Route cur;
			if((cur = routes[i]) != null && (dstIp&cur.getMask()) == cur.getNet()){
/*
					Dbg.wr("found a route to ");
					Dbg.ip(dstIp);
					Dbg.wr("through ");
					Dbg.ip(cur.getNet());
					Dbg.lf();
*/
				return cur.getLinkLayer();
			}
		}
	}
	if(defaultInterface == null)
		Dbg.wr("no route!\n");
	return defaultInterface;
}

public static boolean addRoute(Route r){
//	int mtu = r.getLinkLayer().getMtu();
	synchronized(mutex){
//		if(mtu < minMTU)
//			minMTU = mtu;

		for(int i = 0; i < routes.length; i++){
			if(routes[i] == null){
				routes[i] = r;
				return true;
			}
		}
	}
	return false;
}

public static LinkLayer getDefaultInterface(){
	return defaultInterface;
}

public static void setDefaultInterface(LinkLayer newInterface){
	synchronized(mutex){
		if(defaultInterface == null){
//			int mtu = newInterface.getMtu();
//			if(mtu < minMTU)
//				minMTU = mtu;

			defaultInterface = newInterface;
		}
	}
}

public static void print(){
	Dbg.wr("destination      network mask     interface        mtu\r\n");
	for(int i = 0; i < CNT; i++){
		Route r = routes[i];
		if(r != null){
			for(int j = Util.wrIp(r.getNet()); j <= 15; j++)
				Dbg.wr(' ');
			for(int j = Util.wrIp(r.getMask()); j <= 15; j++)
				Dbg.wr(' ');
			for(int j = Util.wrIp(r.getLinkLayer().getIp()); j <= 15; j++)
				Dbg.wr(' ');
			Dbg.intVal(r.getLinkLayer().getMtu());
			Dbg.lf();
		}
	}
	if(defaultInterface != null){
		Dbg.wr("Default interface: ");
		Util.wrIp(defaultInterface.getIp());
		Dbg.lf();
	}
	Dbg.lf();
}
}
