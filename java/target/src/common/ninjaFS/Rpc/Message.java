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


/* names try to match RFC1831 */
public class Message
{	
	protected long xid; /*!< transaction identifier */
	protected byte[] data;
	protected int readOffset, writeOffset;
	
	public String toHexString( long i )
	{
		return "XYXYXYXY";
	}
		
	public void read( byte[] data ) 
	{
		this.readOffset = 0;
		this.data = data;
		this.xid = this.readUInt32();
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
	
	public String readFilename()
	{

		int length = (int) readUInt32();
		String filename;
		try
		{
			filename = new String( this.data, this.readOffset, length);
		}
		catch( Exception e )
		{
			filename = "null";
		}
		return filename;
	}
	
	public void writeFHandle( byte[] fhandle )
	{
		int FHSIZE = 32;
		for( int i = 0; i < FHSIZE; i++ )
		{
			this.data[ this.writeOffset++ ] = fhandle[i];
		}
	}
	
	public void writeStringAligned( String string)
	{
		byte[] byteString = string.getBytes();
		
		this.writeUInt32( byteString.length );
		System.out.println("Stringsize" + byteString.length );
		
		int i = 0;
		for( i = 0; i < byteString.length ; i++)
		{
			this.data[ this.writeOffset++ ] = byteString[i];
			System.out.println(this.writeOffset + " " + i);
		}
		
		while( (i % 4) != 0 )
		{
			i++;
			this.data[ this.writeOffset++ ] = 0; 
			System.out.println(this.writeOffset);
		}
		
		
		
	}
	
	public byte[] readFHandle( long readOffset)
	{
		if( 0 == readOffset )
			readOffset = data.length - 32;
		
		byte[] fhandle = new byte[32];
		
		for( int i = 0; i < 32 ; i++ )
		{
			fhandle[0]  = data[(int) readOffset];
			readOffset++;
		}
		
		return fhandle;
	}
	
	
	public ReadDirArgs readDirArgs()
	{
		ReadDirArgs args = new ReadDirArgs();
		args.fhandle = readFHandle( this.readOffset );
		this.readOffset += 32;
		args.nfscookie = this.readUInt32();
		args.count = this.readUInt32();
		return args;
	}
	
	public DirOpArgs readDirOpArgs()
	{
		DirOpArgs args = new DirOpArgs();
		args.directory = readFHandle( this.readOffset );
		this.readOffset += 32;
		args.filename = this.readFilename();
		return args;
	}
	
	public void writeEntry( Entry entry )
	{
		this.writeUInt32( entry.fileid );
		this.writeStringAligned( entry.filename );
		this.writeUInt32( entry.nfscookie );
		
	}
		
	public void writeUInt32( long number )
	{
		for( int i = 0; i < 4; i++ )
		{
			long part = number >> ( 8 * 3 );
			number <<= 8;
			
			part &= 0xFF;
			this.data[ this.writeOffset++ ] = (byte) part;
		}
	}
	
	public String toString()
	{
		return "xid:\t0x" + toHexString( this.xid ) + "\n";
	}
	
	protected void readAuth()
	{
		this.readAuthPart();
		this.readAuthPart();
	}
	
	protected void readAuthPart()
	{
		long flavor = this.readUInt32();
		long length = this.readUInt32();
		
		for( int i = 0; i < length; i++ )
			this.readOffset++;
	}
	
	/*public class RPCReply extends RPCMessage
	{
		protected class reply_stat
		{
			public static final long MSG_ACCEPTED = 0;
			public static final long MSG_DENIED = 1;
		}
		
		public RPCMessage read() throws DataFormatException
		{
			return this;
		}
		
		reply_stat stat;
	}
	
	/*private class accepted_reply extends RPCReply
	{
		opaque_auth verifier;
		
	}*/
	protected class msg_type
	{
		public static final long CALL = 0;
		public static final long REPLY = 1;
	}
	
	protected class opaque_auth 
	{
		protected class auth_flavor
		{
			public static final long AUTH_NONE = 0;
			public static final long AUTH_SYS = 1;
			public static final long AUTH_SHORT = 2;
			
			public long value;
			
			public auth_flavor( long value )
			{
				this.value = value;
			}
			
			public String toString()
			{
				if( this.value == AUTH_NONE )
					return "AUTH_NONE";
				else if( this.value == AUTH_SYS )
					return "AUTH_SYS";
				else if( this.value == AUTH_SHORT )
					return "AUTH_SHORT";
				
				return "UNKNOWN";
			}
		}
		
		long opaque_length;
		byte[] opaque_body;
		
		
		/* returns new readOffset */
		int read( byte[] data, int offset )
		{
			return 0;
		}
		
		public String toString()
		{
			return "";
		}
	}
	
	public long getXid()
	{
		return xid;
	}

	public byte[] getData()
	{
		return data;
	}

	public int getReadOffset()
	{
		return readOffset;
	}
	
	public int getWriteOffset()
	{
		return writeOffset;
	}
	
	public class ProgramNumbers
	{
		public final static long Portmapper = 100000;
		public final static long Nfs = 100003;
		public final static long Mount = 100005;
	}
	
	public class ProcedureNumbers
	{
		public class Portmapper
		{
			public final static long	GetPort = 3;
		}
		public class Nfs
		{
			public final static long	Nul			= 0,
               							GetAttr		= 1,
               							SetAttr		= 2,
               							Root		= 3,
               							Lookup		= 4,
               							ReadLink	= 5,
               							Read		= 6,
               							WriteCache	= 7,
               							Write		= 8,
               							Create		= 9,
               							Remove		= 10,
               							Rename		= 11,
               							Link		= 12,
               							Symlink		= 13,
               							Mkdir		= 14,
               							Rmdir		= 15,
               							Readdir		= 16,
               							Statfs		= 17;

		}
		public class Mount
		{
			public final static long	NoOp		= 0,
										Mount		= 1,
										Dump		= 2,
										Unmount		= 3,
										UnmountAll	= 4,
										Export		= 5;
		}
	}
	
	public class Constants
	{
		public class Rpc
		{
			public final static long		currentVersion = 2;
			
			public class replyStatus
			{
				public final static long	Accepted	= 0,
											Denied		= 1;
			}
			
			public class acceptedStatus
			{
				public final static long	Success				= 0,
											ProgramUnavailable	= 1,
											ProgramMismatch		= 2,
											ProcedureUnavail	= 3,
											GarbageArguments	= 4;
			}
			
			public class rejectedStatus
			{
				public final static long	RpcMissmatch		= 0,
											AuthenticationError	= 1;
			}
		}
		public class Nfs
		{
			public class Stat
			{
				public final static long	Ok					= 0,
											ErrorPermission		= 1,
											ErrorNotExists		= 2,
											ErrorIO				= 5,
											ErrorNoSuchIO		= 6,
											ErrorAccess			= 13,
											ErrorExists			= 17,
											ErrorNoSuchDevice	= 19,
											ErrorNotDirectory	= 20,
											ErrorIsDirectory	= 21,
											ErrorFileTooLong	= 27,
											ErrorNoSpace		= 28,
											ErrorReadOnlyFS		= 30,
											ErrorNameTooLong	= 63,
											ErrorNotEmpty		= 66,
											ErrorDiscQuota		= 69,
											ErrorFileHandle		= 70,
											ErrorWriteFlush		= 99;
			}
		}
	}
}


