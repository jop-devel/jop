/*
 * This file is a part of the Jop-UI 
 * Copyright (C) 2009, 	Stefan Resch (e0425306@student.tuwien.ac.at)
 * 						Stefan Rottensteiner (e0425058@student.tuwien.ac.at)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package  com.jopdesign.jopui.core;

import java.io.InputStream; 
import java.io.IOException;

/**
 * Image with an identifier and accepts converted images as bytestream   
 */
public class JPFResource extends Image {

	private String identifier = "";

	/**
	 * Creates a new JPFResource
	 * @param width width of the image
	 * @param height height of the image
	 * @param identifier id of the image
	 */
	public JPFResource(int width, int height, String identifier) {
		super(width, height);
		this.identifier = identifier;
	}

	/**
	 * Returns the id
	 * @return id
	 */
	public String getIdentifier() {
		return this.identifier;
	}

	/**
	 * Set the id
	 * @param identifier id of image
	 */
	public void setIdentifer(String identifier) {
		this.identifier = identifier;
	}

	/**
	 * Generate image out of stream
	 * @param in Input stream
	 * @return resource object
	 */
	public static JPFResource createJPF(InputStream in) {
		byte inbyte;
		byte[] imgbytes = new byte[4];
		boolean abort = false;
		JPFResource jpf = null;
		int action = 0;
		int width = 0;
		int height = 0;
		int length = 0;
		String identifier = "";
		
		int adr;
		int absolute_adr;
		int offset;
	
		while(!abort) {
			
			try {
				inbyte = (byte)in.read();
				System.out.write(inbyte);
				
				switch(action) {
					case 0:	// read header
						if(inbyte != 'J')
							return null;
						break;

					case 1: if(inbyte != 'P')
							return null;
						break;

					case 2: if(inbyte != 'F')
							return null;
						break;

					case 3: if(inbyte != 0) // we currently only support version 0
							return null;
							
						break;

					case 4:	length = inbyte; // length of identifier
						break;

					case 5:	identifier += (char) inbyte; // identifier
						if(--length > 0)
							action--;
						else
							length = 4;
						break;

					case 6: imgbytes[4-length] = inbyte;
						if(--length > 0)
							action--;
						break;

					case 7:	width =  ( 0xff00 & (imgbytes[0] << 8)) |( 0xff & imgbytes[1]);
						height = ( 0xff00 & (imgbytes[2] << 8)) |( 0xff & imgbytes[3]);
						length = width*height;
						
						
						height = ( 0xff00 & (imgbytes[2] << 8)) |( 0xff & imgbytes[3]);
						jpf = new JPFResource(width, height, identifier);
						
						adr = action-7;
						absolute_adr = adr>>2; // write first pixel
						offset = 8*(adr & 0x03);
						jpf.data[absolute_adr] &= ~(0xFF << offset);
						jpf.data[absolute_adr] |= ((int)inbyte) << offset;
						length--;
						break;

					default:
						adr = action-7; // write all other pixels
						absolute_adr = adr>>2;
						offset = 8*(adr & 0x03);
						jpf.data[absolute_adr] &= ~(0xFF << offset);
						jpf.data[absolute_adr] |= ((int)inbyte) << offset;
						if(--length <= 0)
							abort = true;
				}
			
			} catch(IOException e) {
				e.printStackTrace();
        		}
			action++;
		}

		return jpf;
	
	}

}

