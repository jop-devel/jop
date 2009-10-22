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

package ninjaFS.Filesystem;

import ninjaFS.Rpc.Entry;



public class File {

	public FileAttributes fattr;
	public String name;
	public byte[] fileHandle;
	
	public File( FileAttributes fattr, String name, byte[] fHandle){
		
		this.fattr = fattr;
		this.name = name;
		this.fileHandle = fHandle; 
	}
	
	public Entry readEntry()
	{
		Entry entry = new Entry();
		entry.filename = this.name;
		entry.fileid = this.fattr.fileid;
		entry.nfscookie = this.fattr.fileid;
		
		return entry;
	}
		
	
}
