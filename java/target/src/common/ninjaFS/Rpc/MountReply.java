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

public class MountReply extends Reply
{
	private static final int maxReceivedPacketSize = 1024;
	protected long  mountState; 
	byte[] fileHandle;
	
	public MountReply( Call call, long replyState, long acceptRejectState , long mountState, byte[] fileHandle)
	{	super(call, replyState, acceptRejectState);
		this.xid = call.getXid();
		this.replyState = replyState;
		/* verification auth */
		this.acceptRejectState = acceptRejectState;
		this.mountState = mountState;
		this.fileHandle = fileHandle;
	}
	
	public void writeNull()
	{
		this.data = new byte[maxReceivedPacketSize];
		this.writeOffset = 0;
		
		this.writeUInt32(this.xid);
		this.writeUInt32(Message.msg_type.REPLY);
		this.writeUInt32(this.replyState);
		
		/* verification */
		this.writeUInt32(0);
		this.writeUInt32(0);
		
		this.writeUInt32(this.acceptRejectState);
	}
	
	public void write()
	{
		this.data = new byte[maxReceivedPacketSize];
		this.writeOffset = 0;
		
		this.writeUInt32(this.xid);
		this.writeUInt32(Message.msg_type.REPLY);
		this.writeUInt32(this.replyState);
		
		/* verification */
		this.writeUInt32(0);
		this.writeUInt32(0);
		
		this.writeUInt32(this.acceptRejectState);
	
		/* send mount status if there was no error */
		if(0 == this.acceptRejectState)
		{
			this.writeUInt32( this.mountState );
			
			/* return file handle if everithing was ok */
			if( Message.Constants.Nfs.Stat.Ok == this.mountState )
			{
				this.writeFHandle(this.fileHandle );
			}
			
		}
	}
	
}
