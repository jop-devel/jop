package jopui;

import java.util.Hashtable;

import com.jopdesign.jopui.core.Graphics;
import com.jopdesign.jopui.core.Image;
import com.jopdesign.jopui.core.JPFResource;
import com.jopdesign.jopui.helper.Color8Bit;
import com.jopdesign.sys.Native;

public class JPFTest {
	
	public static final int MEM_START = 0x78500;
	public static final int SCREEN_WIDTH = 320;
	public static final int SCREEN_HEIGHT = 240;
	
	static Image screen = Image.createImage(SCREEN_WIDTH, SCREEN_HEIGHT);

	public static void drawToScreen() {
		int [] data = screen.getData();
		for(int i=0; i<data.length; ++i) {
			Native.wr(data[i], MEM_START+i);
		}
	}

	public void run() {
		Hashtable font = new Hashtable();
		String id;
		JPFResource jpfr = null;
		Graphics g = screen.getGraphics();
		
		while((jpfr = JPFResource.createJPF(System.in)) == null) {
			//id = "" + jpfr.getIdentifier().charAt(0);
			//font.put(id, jpfr);
		//}
		//System.out.write('Q');
		

			//int c = g.getColor();
			//g.setColor(Color8Bit.WHITE);
			//g.fillRect(0,0,320,240);
			//g.setColor(c);
		
			g.drawImage(jpfr, 0, 0);
		
			drawToScreen();
		}
	}
	
	public static void main(String [] args) {
		new JPFTest().run();
		System.out.println("Ende");
	}
}
