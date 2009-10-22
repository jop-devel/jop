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

public class PortmapperHandler extends Handler
{
	public PortmapperHandler( Call call )
	{
		super( call );
		
		System.err.println( "Offset: " + this.readOffset );
	}
	
	public Reply read() 
	{
		if( this.call.getProcedure() == Message.ProcedureNumbers.Portmapper.GetPort )
			return this.GetPort(this.readUInt32(), this.readUInt32(), this.readUInt32(), this.readUInt32());
		else
			return null;
	}
	
	/* request for ports of mound and nfs */
	public Reply GetPort( long program, long version, long protocol, long port )
	{
		Reply reply = new Reply( this.call, Message.Constants.Rpc.replyStatus.Accepted, Message.Constants.Rpc.acceptedStatus.Success );
		reply.write();
		
		/* send port number */
		reply.writeUInt32(111);
		
		System.out.println( "program:" + program + " vers:" + version + " proto:" + protocol + " port:"  + port );
		
		return reply;
	}
}
