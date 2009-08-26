
package com.jopdesign.tools;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;


public class JopDisplay extends Frame implements Runnable, KeyListener, MouseListener, MouseMotionListener {
	
	public static final int FRAME_WIDTH = 640;
	public static final int WIDTH = 320;
	public static final int FRAME_HEIGHT = 480;
	public static final int HEIGHT = 240;

	private static final int BASE_ADDRESS = 0x78500;

	private int kb_ctrl_reg = 0x00;	
	private int kb_data_reg = 0x00;
	private int kb_scancode_reg = 0x00;

	private int mouse_flag_reg = 0x00;
	private int mouse_x_inc_reg = 0x00;
	private int mouse_y_inc_reg = 0x00;

	private int last_x=0x00;
	private int last_y=0x00;

	public static final int IO_BASE = 0xffffff80;
	public static final int KB_CTRL = IO_BASE + 0x30 + 0;
	public static final int KB_DATA = IO_BASE + 0x30 + 1;
	public static final int KB_SCANCODE = IO_BASE + 0x30 + 2;

	private static final int MSK_PARITY_ERR = 0x01;
	private static final int MSK_RCV_RDY = 0x02;
	private static final int MSK_SND_RDY = 0x04;
	private static final int MSK_CAPS_LOCK = 0x08;
	private static final int MSK_SCC_RDY = 0x10;
	private static final int MSK_KEY_REL = 0x20;

	public static final int MOUSE_STATUS 	= IO_BASE+0x40+0;
	public static final int MOUSE_FLAG 	= IO_BASE+0x40+1;
	public static final int MOUSE_X_INC	= IO_BASE+0x40+2;
	public static final int MOUSE_Y_INC	= IO_BASE+0x40+3;
		
	public static final int MSK_DTA_RDY  	= 0x01;
	public static final int MSK_BTN_LEFT	= 0x02;
	public static final int MSK_BTN_RIGHT	= 0x04;
	public static final int MSK_BTN_MIDDLE	= 0x08;
	public static final int MSK_X_OVFLOW	= 0x10;
	public static final int MSK_Y_OVFLOW	= 0x20;

	private IOSimMin io;
	private FrameBuffer fb = FrameBuffer.CreateInstance();
	private Thread t;

	public JopDisplay()
	{
		super("JOP Simulation Display Output");

		this.io = io;
		fb.setFocusTraversalKeysEnabled(false); // we want tab keys!
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				t.interrupt();
				fb.disable();
				e.getWindow().setVisible(false);
				JopSim.exit = true;
				try {
					Thread.currentThread().sleep(1000);	// really dirty
				} catch(Exception ex) {
				}
				e.getWindow().dispose();
			}
		});
		
		fb.addKeyListener(this);
		fb.addMouseListener(this);
		fb.addMouseMotionListener(this);
		
		this.setResizable(false);
		this.setVisible(true);
		Insets i = this.getInsets();
		int width = FRAME_WIDTH + i.left + i.right;
		int height = FRAME_HEIGHT + i.top + i.bottom;
		this.setSize(width, height);
		this.add(fb, BorderLayout.CENTER);
		
		t = new Thread(this);
		t.start();	
	}


	public void keyPressed(KeyEvent e) {
		kb_data_reg = e.getKeyChar();
		kb_scancode_reg = e.getKeyCode();
		kb_ctrl_reg |= MSK_RCV_RDY | MSK_SCC_RDY;
	}
			
	public void keyReleased(KeyEvent e) {
		kb_ctrl_reg |= MSK_SCC_RDY | MSK_KEY_REL; 
		kb_scancode_reg = e.getKeyCode();
	}
			
	public void keyTyped(KeyEvent e) {
	}

	public void mouseClicked(MouseEvent e) {

	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
		mouse_flag_reg |= MSK_DTA_RDY;

		switch(e.getButton()){
			case MouseEvent.BUTTON1:
				mouse_flag_reg |= MSK_BTN_LEFT;
				break;
			case MouseEvent.BUTTON2:
				mouse_flag_reg |= MSK_BTN_RIGHT;
				break;	
			case MouseEvent.BUTTON3:
				mouse_flag_reg |= MSK_BTN_MIDDLE;
				break;
			default:
		}
	}

	public void mouseReleased(MouseEvent e) {
		mouse_flag_reg |= MSK_DTA_RDY;

		switch(e.getButton()){
			case MouseEvent.BUTTON1:
				mouse_flag_reg &= ~MSK_BTN_LEFT;
				break;
			case MouseEvent.BUTTON2:
				mouse_flag_reg &= ~MSK_BTN_RIGHT;
				break;	
			case MouseEvent.BUTTON3:
				mouse_flag_reg &= ~MSK_BTN_MIDDLE;
				break;
			default:
		}
	}

	public void mouseDragged(MouseEvent e) {
		mouse_flag_reg |= MSK_DTA_RDY;
		int tmp_x = e.getX();
		int tmp_y = e.getY();

		mouse_x_inc_reg += tmp_x - last_x;
		mouse_y_inc_reg += tmp_y - last_y;

		last_x = tmp_x;
		last_y = tmp_y;
	}

	public void mouseMoved(MouseEvent e) {
		mouse_flag_reg |= MSK_DTA_RDY;
		int tmp_x = e.getX();
		int tmp_y = e.getY();

		mouse_x_inc_reg += tmp_x - last_x;
		mouse_y_inc_reg += tmp_y - last_y;

		last_x = tmp_x;
		last_y = tmp_y;
	}
	

	public int read(int addr)
	{
		int tmp;

		switch(addr) {
			case KB_CTRL:
				tmp = kb_ctrl_reg;
				kb_ctrl_reg = 0x00;
				return tmp;
			case KB_DATA:
				return kb_data_reg;
			case KB_SCANCODE:
				return kb_scancode_reg;

			case MOUSE_STATUS:
				return 0x00;
			case MOUSE_FLAG:
				tmp = mouse_flag_reg;
				mouse_flag_reg &= ~MSK_DTA_RDY;
				return tmp;
			case MOUSE_X_INC:
				tmp = mouse_x_inc_reg;
				mouse_x_inc_reg = 0;
				return tmp;
			case MOUSE_Y_INC:
				tmp = mouse_y_inc_reg;
				mouse_y_inc_reg = 0;
				return tmp;
			default:
		}
		return 0x00;
	}

	public boolean write(int addr, int data)
	{
		if(addr < BASE_ADDRESS || addr >= (BASE_ADDRESS + WIDTH*HEIGHT/4) )
			return false;

		int tmp = ((data&0xff)<<24)+((data&0xff00)<<8)+((data&0xff0000)>>8)+((data>>24)&0xff);
		data = tmp;
		
		tmp = addr;
		
		addr = (addr-BASE_ADDRESS)<<2;
		addr = (FRAME_WIDTH-WIDTH)/2 + (addr%WIDTH) + FRAME_WIDTH * ((FRAME_HEIGHT-HEIGHT)/2 + (addr/WIDTH));

		fb.setPixelWord(addr>>2, data);
		if(tmp == ((BASE_ADDRESS + WIDTH*HEIGHT/4) - 1)) {	// redraw when last 4 pixels are written
			fb.Draw();
		}
		return true;
		
	}
	
	public void run()
	{
		try {
			while((!Thread.currentThread().isInterrupted()))
			{
				//fb.Draw();			
				Thread.sleep(1000);
			}

		} catch(Exception e) {
		}		
	}

}
