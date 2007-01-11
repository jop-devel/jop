/*
 * Copyright (c) Martin Schoeberl, martin@jopdesign.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *	This product includes software developed by Martin Schoeberl
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 */

package ejip2;

public abstract class LinkLayer {
	/** Own IP address */
	public int ip; // own ip address

	/** */
	public int gateway;

	/** Subnet mask */
	public int netmask;

	/**
	 * The one and only reference to this object. (Singleton)
	 */
	static LinkLayer single;

	/**
	 * @return int
	 */
	public abstract int getIpAddress();

	/**
	 * @param dialstr
	 * @param connect
	 * @param user
	 * @param passwd
	 */
	public abstract void startConnection(StringBuffer dialstr,
			StringBuffer connect, StringBuffer user, StringBuffer passwd);

	/** */
	public abstract void reconnect();

	/**
	 * @return int
	 */
	public abstract int getConnCount();

	/** */
	public void disconnect() {
		// only usefull for PPP
	}

	/** */
	public abstract void loop();

	/**
	 * @param remoteAddr
	 * @return true if the remote address is in the same subnet or if no gateway
	 *         is given
	 */
	public static boolean isSameSubnet(int remoteAddr) {
		if (single.gateway == 0)
			return true;

		int test1 = single.ip & single.netmask;
		int test2 = remoteAddr & single.netmask;

		return (test1 ^ test2) == 0;
	}

}
