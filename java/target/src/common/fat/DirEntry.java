/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2008-2009, Rainhard Raschbauer

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package fat;

//Directory Entry Struct

public final class DirEntry {

	int[] DIR_Name = new int[11];     //8 chars filename
	int	DIR_Attr;         //file attributes RSHA, Longname, Drive Label, Directory
	int	DIR_NTRes;        //set to zero
	int	DIR_CrtTimeTenth; //creation time part in milliseconds
	int	DIR_CrtTime;      //creation time
	int	DIR_CrtDate;      //creation date
	int	DIR_LastAccDate;  //last access date
	int	DIR_FstClusHI;    //first cluster high word
	int	DIR_WrtTime;      //last write time
	int	DIR_WrtDate;      //last write date
	int	DIR_FstClusLO;    //first cluster low word
	int	DIR_FileSize;

	private char[] name = new char[13]; // NUL-terminated file name
	
	void initialize(int[] buffer, int offset)	{
		int i, k;

		for (i = 0; i < 11; i++) {
			DIR_Name[i] = buffer[offset+i];
		}

		// get name as displayed
		k = 0;
		for (i = 0; i < 8; i++) {
			if (DIR_Name[i] != ' ') {
				name[k++] = Character.toUpperCase((char)DIR_Name[i]);
			}
		}
		name[k++] = '.';
		for (i = 8; i < 11; i++) {
			if (DIR_Name[i] != ' ') {
				name[k++] = Character.toUpperCase((char)DIR_Name[i]);
			}
		}
		name[k++] = '\0';

		DIR_Attr = buffer[offset + 11];
		DIR_NTRes = buffer[offset + 12];
		DIR_CrtTimeTenth = buffer[offset + 13];
		DIR_CrtTime = ((buffer[offset + 14] & 0x000000FF))
				| ((buffer[offset + 15] & 0x000000FF) << 8);
		DIR_CrtDate = ((buffer[offset + 16] & 0x000000FF))
				| ((buffer[offset + 17] & 0x000000FF) << 8);
		DIR_LastAccDate = ((buffer[offset + 18] & 0x000000FF))
				| ((buffer[offset + 19] & 0x000000FF) << 8);
		DIR_FstClusHI = ((buffer[offset + 20] & 0x000000FF))
				| ((buffer[offset + 21] & 0x000000FF) << 8);
		DIR_WrtTime = ((buffer[offset + 22] & 0x000000FF))
				| ((buffer[offset + 23] & 0x000000FF) << 8);
		DIR_WrtDate = ((buffer[offset + 24] & 0x000000FF))
				| ((buffer[offset + 25] & 0x000000FF) << 8);
		DIR_FstClusLO = ((buffer[offset + 26] & 0x000000FF))
				| ((buffer[offset + 27] & 0x000000FF) << 8);
		DIR_FileSize = (((buffer[offset + 28] & 0x000000FF))
				| ((buffer[offset + 29] & 0x000000FF) << 8)
				| ((buffer[offset + 30] & 0x000000FF) << 16)
				| ((buffer[offset + 31] & 0x000000FF) << 24));
	}

	void dump(int[] buffer, int offset)	{
		int i;

		for (i = 0; i < 11; i++) {
			buffer[offset+i] = DIR_Name[i];
		}

		buffer[offset + 11] = DIR_Attr;
		buffer[offset + 12] = DIR_NTRes;
		buffer[offset + 13] = DIR_CrtTimeTenth;

		buffer[offset + 14] = DIR_CrtTime & 0x000000FF;
		buffer[offset + 15] = (DIR_CrtTime >> 8) & 0x000000FF;
		buffer[offset + 16] = DIR_CrtDate & 0x000000FF;
		buffer[offset + 17] = (DIR_CrtDate >> 8) & 0x000000FF;

		buffer[offset + 18] = DIR_LastAccDate & 0x000000FF;
		buffer[offset + 19] = (DIR_LastAccDate >> 8) & 0x000000FF;
		buffer[offset + 20] = DIR_FstClusHI & 0x000000FF;
		buffer[offset + 21] = (DIR_FstClusHI >> 8) & 0x000000FF;
		buffer[offset + 22] = DIR_WrtTime & 0x000000FF;
		buffer[offset + 23] = (DIR_WrtTime >> 8) & 0x000000FF;
		buffer[offset + 24] = DIR_WrtDate & 0x000000FF;
		buffer[offset + 25] = (DIR_WrtDate >> 8) & 0x000000FF;
		buffer[offset + 26] = DIR_FstClusLO & 0x000000FF;
		buffer[offset + 27] = (DIR_FstClusLO >> 8) & 0x000000FF;

		buffer[offset + 28] = DIR_FileSize & 0x000000FF;
		buffer[offset + 29] = (DIR_FileSize >> 8) & 0x000000FF;
		buffer[offset + 30] = (DIR_FileSize >> 16) & 0x000000FF;
		buffer[offset + 31] = (DIR_FileSize >> 24) & 0x000000FF;
	}

	public int getCluster() {
		return DIR_FstClusLO;
	}

	public int getSize() {
		return DIR_FileSize;
	}
	
	public int getAttribs() {
		return DIR_Attr;
	}
	
	public void setName(char [] filename) {

		int dot = filename.length;

		for (int i = 0; i < 9 && i < filename.length; i++) {
			if (filename[i] == '.') {
				dot = i;
				break;
			}
		}

		for (int i = 0; i < DIR_Name.length; i++) {
			DIR_Name[i] = ' ';
		}
		for (int i = 0; i < 8 && i < dot; i++) {
			DIR_Name[i] = (int) filename[i];
		}
		for (int i = 0; i < 3 && dot+i+1 < filename.length; i++) {
			DIR_Name[8+i] = filename[dot+i+1];
			System.out.print((char)DIR_Name[8+i]);
		}

		for (int i = 0; i < name.length && i < filename.length; i++) {
			name[i] = filename[i];
		}
	}
	
	public boolean matchName(char [] filename) {

		for (int i = 0; i < name.length && i < filename.length; i++) {

			if (name[i] != Character.toUpperCase((char) filename[i])) {
				return false;
			}

			if ((name[i] == 0) && (filename[i] == 0)) {
				return true;
			}

			if ((name[i] == 0) || (filename[i] == 0)) {
				return false;
			}

		}

		return true;
	}
	
}