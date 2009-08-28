package jopui;

import com.jopdesign.jopui.core.Graphics;
import com.jopdesign.jopui.core.Image;
import com.jopdesign.jopui.helper.KeyBoard;
import com.jopdesign.sys.Native;

public class KeyboardTest {
	public static final int MEM_START = 0x78500;
	public static final int SCREEN_WIDTH = 320;
	public static final int SCREEN_HEIGHT = 240;
	
	static Image screen = Image.createImage(SCREEN_WIDTH, SCREEN_HEIGHT);
	Graphics g = null;
	
	public static void drawToScreen() {
		int [] data = screen.getData();
		for(int i=0; i<data.length; ++i) {
			Native.wr(data[i], MEM_START+i);
		}
	}
	
	public void run() {
		Graphics g = screen.getGraphics();
		g.drawRect(0,0,320,240);	
		KeyBoard kb = new KeyBoard();
		int x_pos = 0;
		int y_pos = 0;
			
		for(;;) {
				
			
			int ctrl = kb.getCtrlReg();

			if((ctrl & KeyBoard.MSK_ASCII_RDY) != 0) {
				int c_in = kb.getAscii();
				
					g.drawChar((char)c_in,x_pos,y_pos);
					x_pos+=8;
					
				
			} else if((ctrl & KeyBoard.MSK_SCC_RDY) != 0) {
				int sc_in = kb.getScanCode();
				
				if((ctrl & KeyBoard.MSK_KEY_REL) != 0) {
					sc_in = sc_in & 0xff;
					if(sc_in == 0x5a)
						x_pos = 320;
					
					else if(sc_in == 0x0d)
						x_pos += (4*8);
				}
			}
			
			if(x_pos >= 320) {
				y_pos = y_pos + 10;
				x_pos = 0;
			}
			if(y_pos>= 240) {
				y_pos=0;
				g.setColor(0x00);
				g.fillRect(0,0,320,240);
				g.setColor(0xFF);
			}
			
			drawToScreen();

		}
	}
	
	public static void main(String [] args) throws Exception {
		new KeyboardTest().run();
	}
}

