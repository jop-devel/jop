package jopui;

import com.jopdesign.jopui.core.Graphics;
import com.jopdesign.jopui.core.Image;
import com.jopdesign.jopui.helper.Color8Bit;
import com.jopdesign.sys.Native;

public class PrimitivesTest {
	public static final int MEM_START = 0x78500;
	public static final int SCREEN_WIDTH = 320;
	public static final int SCREEN_HEIGHT = 240;
	
	static Image screen = Image.createImage(SCREEN_WIDTH, SCREEN_HEIGHT);
	Graphics g = null;
	
	Image img01 = Image.createImage(50,50);
	
	public static void drawToScreen() {
		int [] data = screen.getData();
		for(int i=0; i<data.length; ++i) {
			Native.wr(data[i], MEM_START+i);
		}
	}
	
	public void run() {
		g = img01.getGraphics();
		g.setColor(Color8Bit.BLUE);
		g.fillRect(0,0,50,50);
		g.setColor(Color8Bit.RED);
		g.drawRect(0,0,50,50);
		g.drawLine(0,0,49,49);
		g.drawLine(0,49,49,0);
		
		screen.setColorKey(Color8Bit.BITS_BLUE);
		g = screen.getGraphics();
		Graphics.enable(Graphics.COLOR_KEY);
		g.drawImage(img01, 0, 0);
		Graphics.disable(Graphics.COLOR_KEY);
		g.drawImage(img01, 70, 0);
		
		g.setColor(Color8Bit.GREEN);
		g.drawString("colorkey is blue", 0, 55);
		
		g.setColor(7,1,2);
		g.drawArc(200, 25, 20, 20);
		g.setColor(7,7,0);
		g.drawArc(260, 25, 30, 15);
		g.setColor(2,5,4);
		g.fillArc(200, 70, 20, 20);
		g.setColor(2,0,4);
		g.fillArc(260, 70, 30, 15);
		
		for(int i=0; i<=0xFF; ++i) {
			g.setColor(i);
			g.drawLine(i, 100, i, 150);
		}
		
		drawToScreen();
	}
	
	public static void main(String [] args) {
		new PrimitivesTest().run();
	}
}
