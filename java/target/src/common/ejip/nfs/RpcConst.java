/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2009, Daniel Reichhard (daniel.reichhard@gmail.com)

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ejip.nfs;

public class RpcConst {
	//portmapper specific
	protected static final int PMAP_PORT = 111;
	protected static final int PMAP_PROG = 100000;
	protected static final int PMAP_VERS = 2;
	
    protected static final int IPPROTO_TCP = 6;      /* protocol number for TCP/IP */
    protected static final int IPPROTO_UDP = 17;     /* protocol number for UDP/IP */
	
	public static final byte RPC_RPCVERS = 2;
	
	public static final byte AUTH_NULL 	= 0;
	public static final byte AUTH_SYS 	= 1;
	public static final byte AUTH_SHORT = 2;
	public static final byte AUTH_DES 	= 3;
	
	public static final byte TYPE_CALL 	= 0;
	public static final byte TYPE_REPLY = 1;
	
	public static final byte RPC_MSG_RPLY_STAT_ACCEPTED = 0;
	public static final byte RPC_MSG_RPLY_STAT_DENIED 	= 1;
	
	public static final byte RPC_MSG_ACCEPT_STAT_SUCCESS 		= 0;
	public static final byte RPC_MSG_ACCEPT_STAT_PROG_UNAVAIL 	= 1;
	public static final byte RPC_MSG_ACCEPT_STAT_PROG_MISMATCH 	= 2;
	public static final byte RPC_MSG_ACCEPT_STAT_PROC_UNAVAIL 	= 3;
	public static final byte RPC_MSG_ACCEPT_STAT_GARBAGE_ARGS 	= 4;
	public static final byte RPC_MSG_REJECT_STAT_RPC_MISMATCH	= 0;
	public static final byte RPC_MSG_REJECT_STAT_AUTH_ERROR		= 1;
	
	public static final byte RPC_MSG_AUTH_STAT_AUTH_BADCRED			= 1;
	public static final byte RPC_MSG_AUTH_STAT_AUTH_REJECTEDCRED	= 2;
	public static final byte RPC_MSG_AUTH_STAT_AUTH_BADVERF			= 3;
	public static final byte RPC_MSG_AUTH_STAT_AUTH_REJECTEDVERF	= 4;
	public static final byte RPC_MSG_AUTH_STAT_AUTH_TOOWEAK			= 5;
	
}
