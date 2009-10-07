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

package com.jopdesign.jopui.core;

import com.jopdesign.jopui.helper.Color8Bit;

/**
 * For drawing to images. Can draw images, fonts, primitives.
 */
public class Graphics {
	/**
	 * State value for enable/disable color key. <p>
	 * Constant has the value 0x1
	 */
	public static final int COLOR_KEY = 0x1;
	
	/**
	 * Status of a state <p>
	 * Constant has the value 0x0
	 */
	public static final int DISABLE = 0x0;
	
	/**
	 * Status of a state <p>
	 * Constant has the value 0x1
	 */
	public static final int ENABLE = 0x1;
	
	/**
	 * Informs, that an unknown state is enabled/disabled <p>
	 * Constant has the value 0x2
	 */
	public static final int UNKNOWN_STATE = 0x2;

	private Image destImage;
	private int curColor;
	private Bounds curClip;
	private Font curFont;
	private int colorKey;
	
	private static int stateColorKey;
	
	Graphics(Image image) {
		destImage = image;
		curClip = new Bounds(0,0,image.getWidth(),image.getHeight());
		curFont = JopFont.getInstance();
		curColor = Color8Bit.convert(0xFF);
		colorKey = image.colorKey;
	}
	
	/**
	 * <p>Draws an ellipse or circle </p>
	 * <p>If xRadius equals yRadius a circle will be drawn. Only pixel inside the clipping window will be drawn </p>
	 * <pre> 
	 * Used Algorithm:
	 * A Fast Bresenham Type Algorithm. For Drawing Circles
	 * John Kennedy. Mathematics Department. Santa Monica College. 1900 Pico Blvd. Santa Monica, CA 90405
	 * </pre>
	 * @param cx x-coordinate of the center 
	 * @param cy y-coordinate of the center
	 * @param xRadius pixel width in x direction 
	 * @param yRadius pixel width in y direction
	 */
	public void drawArc(int cx, int cy, int xRadius, int yRadius) {
		int x, y;
		int xChange, yChange;
		int ellipseError;
		int twoASquare, twoBSquare;
		int xStopping, yStopping;
		
		int xSquare = xRadius*xRadius;
		int ySquare = yRadius*yRadius;
		
		twoASquare = 2*xSquare;
		twoBSquare = 2*ySquare;
		x = xRadius;
		y = 0;
		xChange = ySquare*(1-2*xRadius);
		yChange = xSquare;
		ellipseError = 0;
		xStopping = twoBSquare * xRadius;
		yStopping = 0;
		
		while(xStopping >= yStopping) {
			ellipseSetPixel(cx,cy,x,y);
			
			y++;
			yStopping += twoASquare;
			ellipseError += yChange;
			yChange += twoASquare;
			if((2*ellipseError + xChange) > 0) {
				x--;
				xStopping -= twoBSquare;
				ellipseError += xChange;
				xChange += twoBSquare;
			}
		}
		
		x = 0;
		y = yRadius;
		xChange = ySquare;
		yChange = xSquare*(1-2*yRadius);
		ellipseError = 0;
		xStopping = 0;
		yStopping = twoASquare*yRadius;
		
		while(xStopping <= yStopping) {
			ellipseSetPixel(cx,cy,x,y);
			
			x++;
			xStopping += twoBSquare;
			ellipseError += xChange;
			xChange += twoBSquare;
			if((2*ellipseError + yChange) > 0) {
				y--;
				yStopping -= twoASquare;
				ellipseError += yChange;
				yChange += twoASquare;
			}
		}
	}
	
	private void ellipseSetPixel(int cx, int cy, int x, int y) {
		int adr;
		int absolute_adr;
		int adroffset;
		
		int xpos, ypos;
		int clipXmin = curClip.x;
		int clipXmax = curClip.x + curClip.width - 1;
		int clipYmin = curClip.y;
		int clipYmax = curClip.y + curClip.height - 1;
			
		for(int i=0; i<4; ++i) {
			switch(i) {
				case 0: 
					xpos = cx + x;
					ypos = cy + y;
					break;
				case 1:
					xpos = cx + x;
					ypos = cy - y;
					break;
				case 2:
					xpos = cx - x;
					ypos = cy + y;
					break;
				case 3:
					xpos = cx - x;
					ypos = cy - y;
					break;
				default: continue;
			}
			
			if(xpos >= clipXmin && xpos <= clipXmax && ypos >= clipYmin && ypos <= clipYmax) {
				adr = xpos + ypos*destImage.width;
				absolute_adr = adr>>2;
				adroffset = 8*(adr & 0x03);
				destImage.data[absolute_adr] &= ~(0xFF << adroffset);
				destImage.data[absolute_adr] |= curColor << adroffset;
			}
		}
	}
	
	/**
	 * <p>Reads the colorvalue of a given pixel </p>
	 * <pre>
	 * Conditions:
	 * clipping window x &#60;= x &#60; clipping window x + clipping window width
	 * clipping window y &#60;= y &#60; clipping window y + clipping window height
	 * </pre>
	 * @param x x-coordinate of the pixel
	 * @param y y-coordinate of the pixel
	 * @return if P(x,y) is inside the clipping window an 8 bit colorvalue will be returned otherwise 0x100 
	 */
	public int getPixel(int x, int y) {
		int clipXmin = curClip.x;
		int clipXmax = curClip.x + curClip.width - 1;
		int clipYmin = curClip.y;
		int clipYmax = curClip.y + curClip.height - 1;
		
		if(x >= clipXmin && x <= clipXmax && y >= clipYmin && y <= clipYmax) {
			int adr = (x+y*destImage.width);
			int absolute_adr = adr>>2;
			int adroffset = 8*(adr & 0x03);
			return (0xFF & (destImage.data[absolute_adr] >> adroffset));
		}
		return (0x100);
	}
	
	/**
	 * <p>Set the given pixel to the current color </p>
	 * <pre>
	 * Conditions:
	 * clipping window x &#60;= x &#60; clipping window x + clipping window width
	 * clipping window y &#60;= y &#60; clipping window y + clipping window height
	 * If pixel is outside the clipping window, no pixel will be drawn
	 * </pre>
	 * @param x x-coordinate of the pixel
	 * @param y y-coordinate of the pixel
	 * @param x x-coordinate of the pixel
	 * @param y y-coordinate of the pixel
	 */
	public void setPixel(int x, int y) {
		int clipXmin = curClip.x;
		int clipXmax = curClip.x + curClip.width - 1;
		int clipYmin = curClip.y;
		int clipYmax = curClip.y + curClip.height - 1;
		
		if(x >= clipXmin && x <= clipXmax && y >= clipYmin && y <= clipYmax) {
			int adr = (x+y*destImage.width); //position in the one-dimensional pixelarray
			int absolute_adr = adr>>2; //because 4 pixel are stored per int
			int adroffset = 8*(adr & 0x03); //which of the 4 pixel
			destImage.data[absolute_adr] &= ~(0xFF << adroffset); //read int
			destImage.data[absolute_adr] |= curColor << adroffset; //write color to the right pos
		}
	}
	
	/**
	 * <p>Draws a ascii character at the specific location </p>
	 * <p>Every character is drawn on a 8*10 pixel grid.</p>
	 * <pre>
	 * Conditions:
	 * if character is unknown, no character is drawn
	 * Only pixel inside the clipping area will be drawn
	 * </pre>
	 * @param character character to be drawn. 
	 * @param x x-coordinate of the upper left corner of the 8*10 grid
	 * @param y y-coordinate of the upper left corner of the 8*10 grid
	 */
	public void drawChar(char character, int x, int y) {
		if((stateColorKey == ENABLE) && (curColor == colorKey)) {
			return;
		}
		
		int [] ch = curFont.getChar(character);
		
		if(ch == null)
			return;
		
		int c1 = 0x0;
		int c2 = 0x0;
		int c3 = 0x0;
		
		/*
		 * characters are stored in int arrays with length 3.
		 * for some, length of 2 or 1 is enough.
		 */
		switch(ch.length) {
			case 1: c1 = ch[0]; break;
			case 2: c1 = ch[0];  c2 = ch[1]; break;
			case 3: c1 = ch[0];  c2 = ch[1]; c3 = ch[2]; break;
			default: return;
		}
		
		int w = curFont.getWidth(); //8
		int h = curFont.getHeight(); //10
		int clipXmin = curClip.x;
		int clipXmax = curClip.x + curClip.width - 1;
		int clipYmin = curClip.y;
		int clipYmax = curClip.y + curClip.height -1;
		int cx=0, cy = 0;
		
		for(int addr = 0; addr < w*h; addr++) {
			int buf;
			if(cx == w) {
				cx = 0;
				cy++;
			}
			
			switch(addr >> 5) {
				case 0:
					buf = c1;
					break;
				case 1:
					buf = c2;
					break;
				case 2:
					buf = c3;
					break;
				default:
					buf = 0;
			}
			
			if((buf & (1<<(addr & 63))) != 0) {
				int nx = cx + x;
				int ny = cy + y;
				if(nx >= clipXmin && nx <= clipXmax && ny >= clipYmin && ny <= clipYmax) {
					int adr = (nx+ny*destImage.width);
					int absolute_adr = adr>>2;
					int adroffset = 8*(adr & 0x03);
					destImage.data[absolute_adr] &= ~(0xFF << adroffset);
					destImage.data[absolute_adr] |= curColor << adroffset;
				}
			}
			cx++;
		}
	}
	
	/**
	 * <p>Draws a sequence of ascii characters</p>
	 * <pre>
	 * Conditions:
	 * if data == null no character will be drawn
	 * if offset &#60; 0 or length &#60; 0 no character will be drawn
	 * if offset + length &#62; the length of the data array no character will be drawn 
	 * Only pixel inside the clipping area will be drawn
	 * </pre>
	 * @param data array holding the string
	 * @param offset start index for the array 
	 * @param length number of chars to be drawn
	 * @param x x-coordinate of the upper left corner of the 8*10 grid
	 * @param y y-coordinate of the upper left corner of the 8*10 grid
	 */
	public void drawChars(char[] data, int offset, int length, int x, int y) {
		if((stateColorKey == ENABLE) && (curColor == colorKey)) {
			return;
		}
		if(data == null)
			return;
		if(offset < 0 || length < 0)
			return;
		if((offset + length) > data.length)
			return;
		
		int w = curFont.getWidth();
		
		for(int i=0; i<length; ++i) {
			char c = data[offset + i];
			int nx = x + i*w;
			drawChar(c, nx, y);
		}
	}
	
	/**
	 * <p>Draws an image to the specific location</p>
	 * <p>Image will be clipped with the current clipping window</p>
	 * <pre>
	 * Conditions:
	 * if img is null no image will be drawn
	 * Only pixel inside the clipping window will be drawn
	 * </pre>
	 * @param img image to be drawn
	 * @param x x-coordinate of the upper left corner of the image
	 * @param y x-coordinate of the upper left corner of the image
	 */
	public void drawImage(Image img, int x, int y) {
		if(img == null)
			return;
		
		Bounds srcRect = new Bounds(0,0,img.getWidth(),img.getHeight());
		int srcWidth = img.getWidth();
		int srcHeight = img.getHeight();
		
		/* calculate drawable area, so no clipping check is needed per pixelset later */
		if(srcRect.x < 0) {
			srcRect.width += srcRect.x;
			srcRect.x = 0;
		}
		if(srcRect.y < 0) {
			srcRect.height += srcRect.y;
			srcRect.y = 0;
		}
		if(srcRect.width > srcWidth) {
			srcRect.width = srcWidth;
		}
		if(srcRect.height > srcHeight) {
			srcRect.height = srcWidth;
		}
		
		Bounds drawable = new Bounds(x, y, srcRect.width, srcRect.height);
		
		int x1 = 0;
		int y1 = 0;
		int w = 0;
		int h = 0;
		
		if(		(drawable.x > curClip.x+curClip.width-1) || (drawable.x+drawable.width-1 < curClip.x) || 
				(drawable.y > curClip.y+curClip.height-1) || (drawable.y+drawable.height-1 < curClip.y))
		{
			return;
		}
		
		if(drawable.x < curClip.x)
			x1 = curClip.x;
		else
			x1 = drawable.x;
		
		if(drawable.y < curClip.y)
			y1 = curClip.y;
		else
			y1 = drawable.y;
		
		if(drawable.x+drawable.width < curClip.x+curClip.width)
			w = drawable.x+drawable.width - x1;
		else
			w = curClip.x+curClip.width - x1;
		
		if(drawable.y+drawable.height < curClip.y+curClip.height)
			h = drawable.y+drawable.height - y1;
		else
			h = curClip.y+curClip.height - y1;
		
		drawable.set(x1, y1, w, h);
		
		int xn, yn, wn, hn;
		
		xn = srcRect.x + srcRect.width - drawable.width;
		yn = srcRect.y + srcRect.height - drawable.height;
		wn = drawable.width;
		hn = drawable.height;
		
		// read pixel for pixel
		for(int i=yn; i<hn+yn; ++i) {
			for(int j=xn; j<wn+xn; ++j) {
				// read pixel from source image
				int adr = (j+i*img.width);
				int absolute_adr = adr>>2;
				int adroffset = 8*(adr & 0x03);
				int c = 0xFF & (img.data[absolute_adr] >> adroffset);
				
				// only draw visible pixel
				if((stateColorKey == ENABLE) && (c == colorKey)) {
					continue;
				}
				
				// write pixel to destination image 
				adr = ((drawable.x+j)+(drawable.y+i)*destImage.width);
				absolute_adr = adr>>2;
				adroffset = 8*(adr & 0x03);
				destImage.data[absolute_adr] &= ~(0xFF << adroffset);
				destImage.data[absolute_adr] |= c << adroffset;
			}
		}
	}
	
	/**
	 * <p>Draws a line between to points</p>
	 * <pre>
	 * Conditions:
	 * Only pixel inside the clipping window will be drawn
	 * </pre>
	 * @param x1 x-coordinate of the first point
	 * @param y1 y-coordinate of the first point
	 * @param x2 x-coordinate of the second point
	 * @param y2 y-coordinate of the second point
	 */
	public void drawLine(int x1, int y1, int x2, int y2) {
		if((stateColorKey == ENABLE) && (curColor == colorKey)) {
			return;
		}
			int x,y,xEnd,yEnd;
			int xInc=1,yInc=1;
			int dx,dy;
			int clipXmin = curClip.x;
			int clipXmax = curClip.x + curClip.width - 1;
			int clipYmin = curClip.y;
			int clipYmax = curClip.y + curClip.height -1;
			int twody,TwoDymTwoDx,p;
			
			/*
			 * |dx|<|dy|...|m|>1...bCheck=true
			 * |dx|>|dy|...|m|<1...bCheck=false
			 * |dx|=|dy|...|m|=1...bCheck=false 
			 */
			boolean bCheck=Math.abs(x2-x1)<Math.abs(y2-y1);
			if(bCheck) {
				x=y1;
				y=x1;
				xEnd=y2;
				yEnd=x2;
			}
			else {
				x=x1;
				y=y1;
				xEnd=x2;
				yEnd=y2;
			}
			
			// always draw from left endpoint to right endpoint 
			if(x>xEnd) {				
				//swap x,xEnd
				x = x ^ xEnd;
				xEnd = xEnd ^x;
				x = x ^ xEnd;
				
				//swap y,yEnd
				y = y ^ yEnd;
				yEnd = yEnd ^ y;
				y = y ^ yEnd;
			}
			
			// decrease the y-coordinate for a neg. slope       
			if(yEnd<y)
				yInc=-1;
						
			dx = Math.abs(xEnd-x);
			dy = Math.abs(yEnd-y);
			
			twody = 2*dy;
			TwoDymTwoDx = 2*(dy-dx);
			p = twody-dx;
						
			// if |m|>1 change roles of x and y calculations
			// clip & draw current Pixel and calculate next one
			if(bCheck) {
				while(x<=xEnd) {
					if(y >= clipXmin && y <= clipXmax && x >= clipYmin && x <= clipYmax) {
						int adr = (y+x*destImage.width);
						int absolute_adr = adr>>2;
						int offset = 8*(adr & 0x03);
						destImage.data[absolute_adr] &= ~(0xFF << offset);
						destImage.data[absolute_adr] |= curColor << offset;
					}
					if(p<0) {
						p+=twody;
					}
					else {
						p+=TwoDymTwoDx;
						y+=yInc;
					}
					x+=xInc;
				}
			}
			else {
				while(x<=xEnd) {
					if(x >= clipXmin && x <= clipXmax && y >= clipYmin && y <= clipYmax) {
						int adr = (x+y*destImage.width);
						int absolute_adr = adr>>2;
						int offset = 8*(adr & 0x03);
						destImage.data[absolute_adr] &= ~(0xFF << offset);
						destImage.data[absolute_adr] |= curColor << offset;
					}
					if(p<0) {
						p+=twody;
					}
					else {
						p+=TwoDymTwoDx;
						y+=yInc;
					}
					x+=xInc;
				}
			}
	}
	
	/**
	 * <p>Draws a rectangle</p>
	 * <pre>
	 * Conditions:
	 * if width or height &#60;= 0 no rectangle will be drawn
	 * Only pixel inside the clipping window will be drawn
	 * </pre>
	 * @param x x-coordinate of the upper left corner
	 * @param y y-coordinate of the upper left corner
	 * @param width width of the rectangle
	 * @param height height of the rectangle
	 */
	public void drawRect(int x, int y, int width, int height) {
		if((stateColorKey == ENABLE) && (curColor == colorKey)) {
			return;
		}
		if(width <= 0 || height <= 0)
			return;
		drawLine(x,y,x+width-1,y);
		drawLine(x+width-1,y,x+width-1,y+height-1);
		drawLine(x+width-1,y+height-1,x,y+height-1);
		drawLine(x,y+height-1,x,y);
	}
	
	/**
	 * <p>Draws a region of a source image to the destination image</p>
	 * <pre>
	 * Conditions:
	 * if src == null no action is performed
	 * if width or height &#60;= 0 no action is performed
	 * if width &#62; destination image no action is performed
	 * if height &#62; destination image no action is performed
	 * Only pixel inside the clipping window will be drawn
	 * </pre>
	 * @param src source image
	 * @param x_src x-coordinate of the upper left corner of the region
	 * @param y_src y-coordinate of the upper left corner of the region
	 * @param width width of the region
	 * @param height height of the region
	 * @param transform transformation to be used for the region
	 * @param x_dest x-coordinate for the destination upper left corner
	 * @param y_dest y-coordinate for the destination upper left corner
	 */
	public void drawRegion(Image src, int x_src, int y_src, int width, int height, int transform, int x_dest, int y_dest) {
		if(src == null)
			return;
		if(width <= 0 || height <= 0)
			return;
		if(width > src.getWidth() || height > src.getHeight())
			return;
		Image tmp = null;
		tmp = Image.createImage(src,x_src,y_src,width,height,transform);
		if(tmp == null)
			return;
		
		Bounds srcRect = new Bounds(0,0,tmp.getWidth(),tmp.getHeight());
		
		int srcWidth = tmp.getWidth();
		int srcHeight = tmp.getHeight();
		
		if(srcRect.x < 0) {
			srcRect.width += srcRect.x;
			srcRect.x = 0;
		}
		if(srcRect.y < 0) {
			srcRect.height += srcRect.y;
			srcRect.y = 0;
		}
		if(srcRect.width > srcWidth) {
			srcRect.width = srcWidth;
		}
		if(srcRect.height > srcHeight) {
			srcRect.height = srcWidth;
		}
		
		Bounds drawable = new Bounds(x_dest, y_dest, srcRect.width, srcRect.height);
		
		int x1 = 0;
		int y1 = 0;
		int w = 0;
		int h = 0;
		
		if(		(drawable.x > curClip.x+curClip.width-1) || (drawable.x+drawable.width-1 < curClip.x) || 
				(drawable.y > curClip.y+curClip.height-1) || (drawable.y+drawable.height-1 < curClip.y))
		{
			return;
		}
		
		if(drawable.x < curClip.x)
			x1 = curClip.x;
		else
			x1 = drawable.x;
		
		if(drawable.y < curClip.y)
			y1 = curClip.y;
		else
			y1 = drawable.y;
		
		if(drawable.x+drawable.width < curClip.x+curClip.width)
			w = drawable.x+drawable.width - x1;
		else
			w = curClip.x+curClip.width - x1;
		
		if(drawable.y+drawable.height < curClip.y+curClip.height)
			h = drawable.y+drawable.height - y1;
		else
			h = curClip.y+curClip.height - y1;
		
		drawable.set(x1, y1, w, h);
		
		int xn, yn, wn, hn;
		
		xn = srcRect.x + srcRect.width - drawable.width;
		yn = srcRect.y + srcRect.height - drawable.height;
		wn = drawable.width;
		hn = drawable.height;
		
		for(int i=yn; i<hn+yn; ++i) {
			for(int j=xn; j<wn+xn; ++j) {
				int adr = (j+i*tmp.width);
				int absolute_adr = adr>>2;
				int adroffset = 8*(adr & 0x03);
				int c = 0xFF & (tmp.data[absolute_adr] >> adroffset);
				
				if((stateColorKey == ENABLE) && (c == colorKey)) {
					continue;
				}
				
				adr = ((drawable.x+j)+(drawable.y+i)*destImage.width);
				absolute_adr = adr>>2;
				adroffset = 8*(adr & 0x03);
				destImage.data[absolute_adr] &= ~(0xFF << adroffset);
				destImage.data[absolute_adr] |= c << adroffset;
			}
		}
	}
	
	/**
	 * <p>Draws an image with the given rgb data</p>
	 * <pre>
	 * Conditions:
	 * if rgbData == null no action is performed
	 * if x of y or offset &#60; 0 no action is performed
	 * if width or height &#60;= 0 no action is performed
	 * if scanlength &#60; width no action is performed
	 * if x + width &#62; destination image width no action is performed
	 * if y + height &#62; destination image height no action is performed
	 * if width * height + offset &#62; the length of rgbData no action is performed
	 * Only pixel inside the clipping window will be drawn
	 * </pre>
	 * @param rgbData image data
	 * @param offset offset in the image data array
	 * @param scanlength length of one scanline in the image data array
	 * @param x x-coordinate of the upper left corner of the destination
	 * @param y y-coordinate of the upper left corner of the destination
	 * @param width width of the image
	 * @param height height of the image
	 */
	public void drawRGB(int[] rgbData, int offset, int scanlength, int x, int y, int width, int height) {
		if(rgbData == null)
			return;
		if(x < 0 || y < 0 || width <= 0 || height <= 0 || offset < 0 || scanlength < width)
			return;
		int imgWidth = destImage.getWidth();
		if((x + width > imgWidth) || (y + height > destImage.getHeight()))
			return;
		if((width*height + offset) > rgbData.length)
			return;
		
		int [] data = destImage.getData();
		int adr;
		int absolute_adr;
		int adroffset;
		
		for(int j=y; j<height; ++j) {
			for(int i=x; i<width; ++i) {
				adr = (i+j*imgWidth);
				absolute_adr = adr>>2;
				adroffset = 8*(adr & 0x03);
				data[absolute_adr] &= ~(0xFF << adroffset);
				data[absolute_adr] |= rgbData[offset + (i-x) + (j-y)*scanlength] << adroffset;
			}
		}
	}
	
	/**
	 * <p>Draws a string to the specific location</p>
	 * <pre>
	 * Conditions:
	 * if str == null no action is performed
	 * if str is an empty string no action is performed 
	 * Only pixel inside the clipping window will be drawn
	 * </pre>
	 * @param str string to be drawn
	 * @param x x-coordinate of the upper left corner of the character grid
	 * @param y x-coordinate of the upper left corner of the character grid
	 */
	public void drawString(String str, int x, int y) {
		if((stateColorKey == ENABLE) && (curColor == colorKey)) {
			return;
		}
		if(str == null)
			return;
		int length = str.length();
		if(length == 0)
			return;
		char [] dest = new char[length];
		str.getChars(0, length, dest, 0);
		drawChars(dest,0,length,x,y);
	}

	/**
	 * <p>Draws a string to the specific location</p>
	 * <pre>
	 * Conditions:
	 * if str == null no action is performed
	 * if str is an empty string no action is performed
	 * if offset + len &#62; length no action is performed
	 * Only pixel inside the clipping window will be drawn
	 * </pre>
	 * @param str string to be drawn
	 * @param offset start index in the string
	 * @param len number of chars to be drawn
	 * @param x x-coordinate of the upper left corner of the character grid
	 * @param y y-coordinate of the upper left corner of the character grid
	 */
	public void drawSubstring(String str, int offset, int len, int x, int y) {
		if((stateColorKey == ENABLE) && (curColor == colorKey)) {
			return;
		}
		if(str == null)
			return;
		int length = str.length();
		if(length == 0)
			return;
		if((offset + len)> length)
			return;
		String sub = str.substring(offset, offset+len);
		drawString(sub,x,y);
	}
	
	/**
	 * <p>Fill a circle/ellipse with the current color</p>
	 * <pre>
	 * Used Algorithm:
	 * A Fast Bresenham Type Algorithm. For Drawing Circles
	 * John Kennedy. Mathematics Department. Santa Monica College. 1900 Pico Blvd. Santa Monica, CA 90405
	 * </pre>
	 * @param cx x-coordinate of the center
	 * @param cy y-coordinate of the center
	 * @param xRadius pixel width in x diBoundson
	 * @param yRadius pixel width in y diBoundson
	 */
	public void fillArc(int cx, int cy, int xRadius, int yRadius) {
		int x, y;
		int xChange, yChange;
		int ellipseError;
		int twoASquare, twoBSquare;
		int xStopping, yStopping;
		
		int xSquare = xRadius*xRadius;
		int ySquare = yRadius*yRadius;
		
		twoASquare = 2*xSquare;
		twoBSquare = 2*ySquare;
		x = xRadius;
		y = 0;
		xChange = ySquare*(1-2*xRadius);
		yChange = xSquare;
		ellipseError = 0;
		xStopping = twoBSquare * xRadius;
		yStopping = 0;
		
		while(xStopping >= yStopping) {
			drawLine(cx-x,cy+y,cx+x,cy+y);
			drawLine(cx-x,cy-y,cx+x,cy-y);
			
			y++;
			yStopping += twoASquare;
			ellipseError += yChange;
			yChange += twoASquare;
			if((2*ellipseError + xChange) > 0) {
				x--;
				xStopping -= twoBSquare;
				ellipseError += xChange;
				xChange += twoBSquare;
			}
		}
		
		x = 0;
		y = yRadius;
		xChange = ySquare;
		yChange = xSquare*(1-2*yRadius);
		ellipseError = 0;
		xStopping = 0;
		yStopping = twoASquare*yRadius;
		
		while(xStopping <= yStopping) {
			drawLine(cx-x,cy+y,cx+x,cy+y);
			drawLine(cx-x,cy-y,cx+x,cy-y);
			
			x++;
			xStopping += twoBSquare;
			ellipseError += xChange;
			xChange += twoBSquare;
			if((2*ellipseError + yChange) > 0) {
				y--;
				yStopping -= twoASquare;
				ellipseError += yChange;
				yChange += twoASquare;
			}
		}
	}
	
	/**
	 * <p>Fills a recangle with the current color</p>
	 * <p>Only pixel inside the clipping window will be drawn</p>
	 * 
	 * @param x x-coordinate for the upper left corner
	 * @param y y-coordinate for the upper left corner
	 * @param width width of the rectangle
	 * @param height height of the rectangle
	 */
	public void fillRect(int x, int y, int width, int height) {
		if((stateColorKey == ENABLE) && (curColor == colorKey)) {
			return;
		}
		
		int clipXmin = curClip.x;
		int clipXmax = curClip.x + curClip.width - 1;
		int clipYmin = curClip.y;
		int clipYmax = curClip.y + curClip.height -1;
		
		if(x == 0 && y == 0 && width == destImage.width && height == destImage.height) {
			int c = (curColor<<24) | (curColor<<16) | (curColor<<8) | curColor;
			for(int i=0; i<destImage.data.length; ++i) {
				destImage.data[i] = c;
			}
			return;
		}
		
		int xStart = x;
		int xStop = x + width - 1;
		int yStart = y;
		int yStop = y + height - 1;
		int adr;
		int absolute_adr;
		int adroffset;
		
		if(x > clipXmax || y > clipYmax)
			return;
		if((x + width) < clipXmin || (y + height) < clipYmin)
			return;
		
		if(x < clipXmin)
			xStart = clipXmin;
		if(y < clipYmin)
			yStart = clipYmin;
		if(x+width > clipXmax)
			xStop = clipXmax;
		if(y+height > clipYmax)
			yStop = clipYmax;

		// fill rectangle pixel per pixel 
		for(int i=yStart; i<=yStop; ++i) {
			for(int j=xStart; j<=xStop; ++j) {
				adr = (j+i*destImage.width);
				absolute_adr = adr>>2;
				adroffset = 8*(adr & 0x03);
				destImage.data[absolute_adr] &= ~(0xFF << adroffset);
				destImage.data[absolute_adr] |= curColor << adroffset;
			}
		}
	}
	
	/**
	 * <p>Set the clipping window</p>
	 * <pre>
	 * Conditions:
	 * if x or y &#60; 0 no action is performed
	 * if width or height &#62;= 0 no action is performed
	 * if x+width &#62; destination image width no action is performed
	 * if y+height &#62; destination image height no action is performed
	 * </pre>
	 * @param x x-coordinate of the upper left corner of the clipping window in pixel
	 * @param y y-coordinate of the upper left corner of the clipping window in pixel
	 * @param width width of the clipping window in pixel
	 * @param height height of the clipping window in pixel
	 */
	public void setClip(int x, int y, int width, int height) {
		if(x < 0 || y < 0 || width <= 0 || height <= 0)
			return;
		if(x+width > destImage.getWidth() || y+height > destImage.getHeight())
			return;
		curClip.set(x, y, width, height);
	}
	
	/**
	 * Set the current color
	 * 
	 * @param RGB an 8 bit value
	 */
	public void setColor(int RGB) {
		curColor = Color8Bit.convert(RGB);
	}
	
	/**
	 * <p>Set the current color</p>
	 * Greater values as given bits will be truncated
	 * @param red 3 bit value
	 * @param green 2 bit value
	 * @param blue 2 bit value
	 */
	public void setColor(int red, int green, int blue) {
		curColor = Color8Bit.convert(red, green, blue);
	}
	
	/**
	 * Set current font
	 * 
	 * @param font font to be used
	 */
	public void setFont(Font font) {
		curFont = font;
	}
	
	/**
	 * Get the red value of the current color 
	 * @return red color value
	 */
	public int getRedComponent() {
		return Color8Bit.getRed(curColor);
	}
	
	/**
	 * Get the green value of the current color 
	 * @return green color value
	 */
	public int getGreenComponent() {
		return Color8Bit.getGreen(curColor);
	}
	
	/**
	 * Get the blue value of the current color 
	 * @return blue color value
	 */
	public int getBlueComponent() {
		return Color8Bit.getBlue(curColor);
	}
	
	/**
	 * Get the current color
	 * @return current color value
	 */
	public int getColor() {
		return curColor;
	}
	
	/**
	 * Get the current font
	 * @return current font
	 */
	public Font getFont() {
		return curFont;
	}
	
	/**
	 * Get the x-coordinate of the clipping window
	 * 
	 * @return x-coordinate of clipping window
	 */
	public int getClipX() {
		return curClip.x;
	}
	
	/**
	 * Get the y-coordinate of the clipping window
	 * 
	 * @return y-coordinate of clipping window
	 */
	public int getClipY() {
		return curClip.y;
	}
	
	/**
	 * Get the width of the clipping window
	 * 
	 * @return width of clipping window
	 */
	public int getClipWidth() {
		return curClip.width;
	}
	
	/**
	 * Get the height of the clipping window
	 * 
	 * @return height of clipping window
	 */
	public int getClipHeight() {
		return curClip.height;
	}
	
	/**
	 * <p>Enable states for drawing</p>
	 * <pre>
	 * State:
	 * COLOR_KEY
	 * </pre>
	 * @param state state to be enabled
	 */
	public static void enable(int state) {
		switch(state) {
			case COLOR_KEY: stateColorKey = ENABLE; break;
			default:
		}
	}
	
	/**
	 * <p>Disable states for drawing</p>
	 * <pre>
	 * State:
	 * COLOR_KEY
	 * </pre>
	 * @param state state to be disabled
	 */
	public static void disable(int state) {
		switch(state) {
			case COLOR_KEY: stateColorKey = DISABLE; break;
			default:
		}
	}
	
	/**
	 * <p>Test if a state is enabled/disabled</p>
	 * <pre>
	 * State:
	 * COLOR_KEY
	 * </pre>
	 * @param state state to be tested
	 * @return state value or UNKNOWN_STATE if state is currently not implemented
	 */
	public static int isEnabled(int state) {
		switch(state) {
			case COLOR_KEY: return stateColorKey;
			default: return UNKNOWN_STATE;
		}
	}
}
