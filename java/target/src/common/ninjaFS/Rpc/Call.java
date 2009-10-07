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

import ninjaFS.Rpc.Message.msg_type;
import ninjaFS.Rpc.Message.opaque_auth;

public class Call extends Message
{
	protected long	rpcVersion,
					program,
					version,
					procedure;
	
	opaque_auth credentials;
	opaque_auth verifier;
	


	public void read( byte[] data )
	{
		super.read( data );
		
		if( this.readUInt32() != msg_type.CALL )
		{
		}
		
		this.rpcVersion = readUInt32();
		this.program = readUInt32();
		this.version = readUInt32();
		this.procedure = readUInt32();
		
		this.readAuth();
	}
	
	public String toString()
	{
		return super.toString() + "rpcVersion:\t0x" + toHexString(this.rpcVersion) + "\n" 
								+ "program:\t" + this.program + "\n"
								+ "version:\t0x" + toHexString(this.version) + "\n"
								+ "procedure:\t0x" + toHexString(this.procedure) + "\n";
	}

	public long getProgram()
	{
		return program;
	}
	
	public long getProcedure()
	{
		return procedure;
	}

	public long getRpcVersion()
	{
		return rpcVersion;
	}

	public long getVersion()
	{
		return version;
	}
}
