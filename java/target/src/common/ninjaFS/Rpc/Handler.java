/* *************************************************************************** *

	Copyright 2009 Georg Merzdovnik, Gerald Wodni

	This file is part of ninjaFS.

	ninjaFS is free software; you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation; either version 3 of the License, or
	(at your option) any later version.

	ninjaFS is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program.  If not, see <http://www.gnu.org/licenses/>.

* *************************************************************************** */

package ninjaFS.Rpc;

public class Handler
{
	protected Call call;
	protected byte[] data;
	protected int readOffset;
	
	
	public Handler( Call call )
	{
		this.call = call;
		this.data = call.getData();
		this.readOffset = call.getReadOffset();
	}
	
	public static Handler create( Call call )
	{
		/* check if version matches */
		if( call.getRpcVersion() == Message.Constants.Rpc.currentVersion )
		{
			/* check for matching program */
			if( call.getProgram() == Message.ProgramNumbers.Portmapper )
				return new PortmapperHandler( call );
			else if( call.getProgram() == Message.ProgramNumbers.Nfs )
				return new NfsHandler( call );
			else if( call.getProgram() == Message.ProgramNumbers.Mount )
				return new MountHandler( call);
		}
		
		/* nothing found, return default handler */
		return new Handler( call );
	}
	
	protected long readUInt32()
	{
		long retter = 0;
		for( int i = 0; i < 4; i++ )
		{
			retter <<= 8;
			retter |= ((long) this.data[this.readOffset++]) & 0xFF;
		}
		
		return retter;
	}
	
	/* return rpc-version error or programunavailable */
	public Reply read() 
	{
		Reply reply;
		
		if( this.call.getRpcVersion() != Message.Constants.Rpc.currentVersion )
			reply = new Reply( this.call, Message.Constants.Rpc.replyStatus.Denied, Message.Constants.Rpc.rejectedStatus.RpcMissmatch );
		else
			reply = new Reply( this.call, Message.Constants.Rpc.replyStatus.Accepted, Message.Constants.Rpc.acceptedStatus.ProgramUnavailable );
		
		reply.write();
			
		return reply;
	}
	
	public String toString()
	{
		return "Handler";
	}
}
