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

public class MountHandler extends Handler
{
	private byte[] fileHandle;
	
	public MountHandler( Call call )
	{
		super( call );
		this.fileHandle = new byte[32];
	}
	
	public MountReply read()
	{
		if( this.call.getProcedure() == Message.ProcedureNumbers.Mount.NoOp )
			return this.NoOp();
		else if ( this.call.getProcedure() == Message.ProcedureNumbers.Mount.Mount )
		{	
			return this.Mount( this.call.readFilename() );
		}
		else
		{
			MountReply reply = new MountReply( this.call, Message.Constants.Rpc.replyStatus.Accepted, Message.Constants.Rpc.acceptedStatus.ProcedureUnavail , 0, fileHandle);
			reply.write();
			
			return reply;
		}
	}	
	
	public MountReply NoOp()
	{
		MountReply reply = new MountReply( this.call, Message.Constants.Rpc.replyStatus.Accepted, Message.Constants.Rpc.acceptedStatus.Success , 0, fileHandle );
		reply.writeNull();
		
		return reply;
	}
	
	public MountReply Mount( String path)
	{
		// search for file, check permission  and return file handle	
		MountReply reply = new MountReply( this.call, Message.Constants.Rpc.replyStatus.Accepted, Message.Constants.Rpc.acceptedStatus.Success , Message.Constants.Nfs.Stat.Ok, fileHandle );
		reply.write();
		
		return reply;
	}
	
	
	
}
