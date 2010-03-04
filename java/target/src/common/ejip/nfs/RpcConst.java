/*
 * Copyright (c) Daniel Reichhard, daniel.reichhard@gmail.com
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
 *	This product includes software developed by Daniel Reichhard
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
