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

import ninjaFS.Filesystem.Constants;
import ninjaFS.Filesystem.File;
import ninjaFS.Filesystem.FileSystem;

public class NfsHandler extends Handler
{
	public NfsHandler( Call call )
	{
		super( call );
	}
	
	public Reply read()
	{
		if( this.call.getProcedure() == Message.ProcedureNumbers.Nfs.Nul )
			return this.Nul();
		else if( this.call.getProcedure() == Message.ProcedureNumbers.Nfs.Statfs )
			return this.Statfs();
		else if( this.call.getProcedure() == Message.ProcedureNumbers.Nfs.GetAttr )
			return this.GetAttr();
		else if( this.call.getProcedure() == Message.ProcedureNumbers.Nfs.Readdir )
			return this.ReadDir();
		else if( this.call.getProcedure() == Message.ProcedureNumbers.Nfs.Lookup )
			return this.Lookup();
		else
		{
			Reply reply = new Reply( this.call, Message.Constants.Rpc.replyStatus.Accepted, Message.Constants.Rpc.acceptedStatus.ProcedureUnavail );
			reply.write();
			
			return reply;
		}
	}	
	
	public Reply Nul()
	{
		Reply reply = new Reply( this.call, Message.Constants.Rpc.replyStatus.Accepted, Message.Constants.Rpc.acceptedStatus.Success );
		reply.write();
		
		return reply;
	}
	
	public Reply Statfs()
	{
		Reply reply = new Reply( this.call, Message.Constants.Rpc.replyStatus.Accepted, Message.Constants.Rpc.acceptedStatus.Success );
		// Werte aus eigenem Dateisystem, müssen dann aus dem darunterliegenden Dateisystem gelesen werden
		reply.write(32768, 4096, 120179748, 27494998, 21390199);
		
		return reply;
	}
	
	public Reply GetAttr()
	{
		File file = FileSystem.findFile( this.call.readFHandle( this.call.readOffset) );
		Reply reply;
		// Werte aus eigenem Dateisystem, müssen dann aus dem darunterliegenden Dateisystem gelesen werden
		if( null != file)
		{
			reply = new Reply( this.call, Message.Constants.Rpc.replyStatus.Accepted, Message.Constants.Rpc.acceptedStatus.Success );
			reply.write( file.fattr );
		}
		else
		{
			reply = new Reply( this.call, Message.Constants.Rpc.replyStatus.Accepted, Message.Constants.Rpc.acceptedStatus.Success );
			reply.write( Message.Constants.Nfs.Stat.ErrorExists );
		}
		return reply;
	}
	
	public Reply ReadDir()
	{
		ReadDirArgs args = this.call.readDirArgs();
		File file = FileSystem.findFile( args.fhandle );
		Reply reply;
		// Werte aus eigenem Dateisystem, müssen dann aus dem darunterliegenden Dateisystem gelesen werden
		if( null != file)
		{
			if( Constants.fileType.NFDIR != file.fattr.filetype )
			{
				
				reply = new Reply( this.call, Message.Constants.Rpc.replyStatus.Accepted, Message.Constants.Rpc.acceptedStatus.Success );
				reply.write( Message.Constants.Nfs.Stat.ErrorNotDirectory );
			}
			else
			{
				reply = new Reply( this.call, Message.Constants.Rpc.replyStatus.Accepted, Message.Constants.Rpc.acceptedStatus.Success );
				reply.write( Message.Constants.Nfs.Stat.Ok );
				// Write the files starting from cookie to the output
				// value_follows = 1
				reply.writeUInt32( 1 );
				reply.writeEntry( FileSystem.file1.readEntry() );
				reply.writeUInt32( 1 );
				reply.writeEntry( FileSystem.file2.readEntry() );
				reply.writeUInt32( 0 );
				
				// EOF
				reply.writeUInt32( 1 );
			}
			
		}
		else
		{
			reply = new Reply( this.call, Message.Constants.Rpc.replyStatus.Accepted, Message.Constants.Rpc.acceptedStatus.Success );
			reply.write( Message.Constants.Nfs.Stat.ErrorExists );
		}
		return reply;
	}
	
	public Reply Lookup()
	{
		DirOpArgs args = this.call.readDirOpArgs();
		File dir = FileSystem.findFile( args.directory );
		Reply reply;
		// Werte aus eigenem Dateisystem, müssen dann aus dem darunterliegenden Dateisystem gelesen werden
		if( null != dir)
		{
			if( Constants.fileType.NFDIR != dir.fattr.filetype )
			{
				
				reply = new Reply( this.call, Message.Constants.Rpc.replyStatus.Accepted, Message.Constants.Rpc.acceptedStatus.Success );
				reply.write( Message.Constants.Nfs.Stat.ErrorNotDirectory );
			}
			else
			{
				reply = new Reply( this.call, Message.Constants.Rpc.replyStatus.Accepted, Message.Constants.Rpc.acceptedStatus.Success );
				
				File file = FileSystem.findFilebyString( args.filename );
				if( file == null)
				{
					reply.write( Message.Constants.Nfs.Stat.ErrorExists );
					return reply;
				}

				reply.write( Message.Constants.Nfs.Stat.Ok );
				reply.writeFHandle( file.fileHandle );
				reply.writeAttributes(file.fattr);
				

			}
			
		}
		else
		{
			reply = new Reply( this.call, Message.Constants.Rpc.replyStatus.Accepted, Message.Constants.Rpc.acceptedStatus.Success );
			reply.write( Message.Constants.Nfs.Stat.ErrorExists );
		}
		return reply;
	}
	
	
}
