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

import ninjaFS.Filesystem.FileAttributes;

public class Reply extends Message
{
	private static final int maxReceivedPacketSize = 1024;
	protected long replyState, acceptRejectState;
	
	public Reply( Call call, long replyState, long acceptRejectState )
	{
		this.xid = call.getXid();
		this.replyState = replyState;
		/* verification auth */
		this.acceptRejectState = acceptRejectState;
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
	}
	
	public void write(long nfsError)
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
		this.writeUInt32( nfsError );
		
	}
	
	public void write(long transfersize, long blocksize, long totalblocks, long freeblocks, long availableblocks)
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
		
		// NFS Command Accepted Message
		this.writeUInt32( Message.Constants.Nfs.Stat.Ok );
		this.writeUInt32(transfersize);
		this.writeUInt32(blocksize);
		this.writeUInt32(totalblocks);
		this.writeUInt32(freeblocks);
		this.writeUInt32(availableblocks);
	}
	
	public void write( FileAttributes fattr)
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
		
		// NFS Command Accepted Message
		this.writeUInt32( Message.Constants.Nfs.Stat.Ok );
		this.writeAttributes(fattr);
	}
	
	public void writeAttributes(FileAttributes fattr)
	{
		this.writeUInt32( fattr.filetype );
		this.writeUInt32( fattr.mode );
		this.writeUInt32( fattr.nlink );
		this.writeUInt32( fattr.uid );
		this.writeUInt32( fattr.gid );
		this.writeUInt32( fattr.size );
		this.writeUInt32( fattr.blocksize );
		this.writeUInt32( fattr.rdev );
		this.writeUInt32( fattr.blocks );
		this.writeUInt32( fattr.fsid );
		this.writeUInt32( fattr.fileid );
		this.writeUInt32( fattr.atime.seconds );
		this.writeUInt32( fattr.atime.useconds );
		this.writeUInt32( fattr.mtime.seconds );
		this.writeUInt32( fattr.mtime.useconds );
		this.writeUInt32( fattr.ctime.seconds );
		this.writeUInt32( fattr.ctime.useconds );
		
	}
	
	
}
