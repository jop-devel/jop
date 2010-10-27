package com.jopdesign.tools;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.MemoryImageSource;
import java.util.Arrays;

public class FrameBuffer extends Canvas {
	
	private static FrameBuffer instance = null;
	
	private ColorModel colorModel;
	private MemoryImageSource memoryImageSource;
	private Image image;
	
	private int width;
	private int height;
	private int [] data;
	
	private boolean enabled = true;

	public static FrameBuffer CreateInstance() {
		if(instance == null)
			instance = new FrameBuffer();
		return instance;
	}
	
	private FrameBuffer() {
		width = JopDisplay.FRAME_WIDTH;
		height = JopDisplay.FRAME_HEIGHT;
		data = new int[width * height];
	
		Arrays.fill(data,0x00);

		super.setSize(this.getSize());
		
		colorModel = new DirectColorModel(8, 0xE0, 0x1C, 0x03);
		memoryImageSource = new MemoryImageSource(width, height, colorModel, data, 0, width);
		memoryImageSource.setAnimated(true);
		memoryImageSource.setFullBufferUpdates(false);
		image = createImage(memoryImageSource);
		
//		System.out.println("width: " + image.getGraphics().getClipBounds().getWidth() + " height: " + image.getGraphics().getClipBounds().getHeight());
		
	}
	
	public void disable() {
		enabled = false;
	}

	public void setPixel(int addr, int data) {
		this.data[addr] = 0xFF & data;
	}

	public void setPixelWord(int addr, int data) {
		for(int i=0; i<=3; i++)
			this.setPixel(addr*4+i, 0xFF & (data>>((3-i)*8)));
	}

	public void Draw() {
		if(enabled == false)
			return;
		Graphics g = getGraphics();
		paint(g);
		g.dispose();
	}
	
	@Override 
	public int getWidth() 
	{
		return this.width;
	}
	
	@Override 
	public int getHeight() 
	{
		return this.height;
	}
	
	
	@Override 
	public Dimension getSize() 
	{
		return new Dimension(this.width, this.height);
	}
	
	@Override 
	public void paint(Graphics g) 
	{
		if(enabled == false)
			return;
		memoryImageSource.newPixels(0, 0, width, height);
		g.drawImage(image, 0, 0, width, height, null);
//		System.out.println("paint g: width: " + g.getClipBounds().getWidth() + " height: " + g.getClipBounds().getHeight());
	}
	
}
