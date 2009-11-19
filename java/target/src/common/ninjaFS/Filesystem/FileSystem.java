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

import ninjaFS.Rpc.TimeValue;

public  class FileSystem {
	
	public static File root;
	public static File file1;
	public static File file2;
	
	public FileSystem()
	{		TimeValue time = new TimeValue( 1240954100, 843911);
			FileAttributes fileattr = new FileAttributes(Constants.fileType.NFDIR, 040755, 36, 1000, 1000, 4096, 4096, 0xffffffff, 8, 0x00000900 , 1, time, time, time);
		
			byte[] fhandler = new byte[32];
								
			root = new File(fileattr, "root", fhandler);
			FileAttributes fileattr1 = new FileAttributes(Constants.fileType.NFREG, 040755, 36, 1000, 1000, 4096, 4096, 0xffffffff, 8, 0x00000900 , 1, time, time, time);
			fileattr1.fileid = 2;
			byte[] fhandler1 = new byte[32];
			fhandler1[31] = 2;
			file1 = new File(fileattr1, "file1", fhandler1);
			FileAttributes fileattr2 = new FileAttributes(Constants.fileType.NFREG, 040755, 36, 1000, 1000, 4096, 4096, 0xffffffff, 8, 0x00000900 , 1, time, time, time);
			fileattr2.fileid = 3;
			byte[] fhandler2 = new byte[32];
			fhandler2[31] = 3;
			file2 = new File(fileattr2, "file2", fhandler2);
	}
	
	public static File findFile( byte[] fhandler ){
		
		if( 0 == fhandler[31] )
		{
			return root;	
		}
		if( 2 == fhandler[31] )
		{
			return file1;			
		}
		if( 3 == fhandler[31] )
		{
			return file2;			
		}
		
		return null;
	}
	
	public static File findFilebyString( String  Filename ){
		
		if( Filename.equals("root") )
		{
			return root;	
		}
		if( Filename.equals("file1") )
		{
			return file1;			
		}
		if( Filename.equals("file2") )
		{
			return file2;			
		}
		
		return null;
	}

}
