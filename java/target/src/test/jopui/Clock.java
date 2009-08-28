package jopui;

import com.jopdesign.jopui.core.Graphics;
import com.jopdesign.jopui.core.Image;
import com.jopdesign.jopui.core.JopFont;
import com.jopdesign.jopui.helper.Color8Bit;
import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

public class Clock {
	public static final int MEM_START = 0x78500;
	public static final int SCREEN_WIDTH = 320;
	public static final int SCREEN_HEIGHT = 240;
	
	static Image screen = Image.createImage(SCREEN_WIDTH, SCREEN_HEIGHT);
	static Graphics g = null;
	static String strTime = null;
	static int hour = 0;
	static int minute = 0;
	static int second = 0;
	static int ms = 0;
	static String strHour = null;
	static String strMinute = null;
	static String strSecond = null;
	
	public static void drawToScreen() {
		int [] data = screen.getData();
		for(int i=0; i<data.length; ++i) {
			Native.wr(data[i], MEM_START+i);
		}
	}
	
	public static void drawClock() {
		createString();
		
		g = screen.getGraphics();
		g.setColor(Color8Bit.BLACK);
		g.fillRect(0,0,strTime.length()*JopFont.JOPFONT_WIDTH, JopFont.JOPFONT_HEIGHT);
		g.setColor(Color8Bit.RED);
		g.drawString(strTime, 0, 0);
		drawToScreen();
	}
	
	public static void resetTimer() {
		hour = 0;
		minute = 0;
		second = 0;
		ms = 0;
		strHour = null;
		strMinute = null;
		strSecond = null;
		strTime = new String("00:00:00");
	}
	
	public static void createString() {
		if(second < 10) {
			strSecond = new String("0");
			strSecond += Integer.toString(second);
		}
		else {
			strSecond = Integer.toString(second);
		}
		
		if(minute < 10) {
			strMinute = new String("0");
			strMinute += Integer.toString(minute);
		}
		else {
			strMinute = Integer.toString(minute);
		}
		
		if(hour < 10) {
			strHour = new String("0");
			strHour += Integer.toString(hour);
		}
		else {
			strHour = Integer.toString(hour);
		}
		
		strTime = new String(strHour);
		strTime += ":";
		strTime += strMinute;
		strTime += ":";
		strTime += strSecond;
	}
	
	static void time() {
		int next;
		next = 0;
		second = -1;

		while(true) {

			++ms;
			if (ms==1000) {
				ms = 0;
				++second;
				if (second==60) {
					second = 0;
					++minute;
				}
				if (minute==60) {
					minute = 0;
					++hour;
				}
				if(hour==24) 
					hour = 0;
	
				createString();
				drawClock();
				
				Native.wr(second & 1, Const.IO_WD);
			}

			Native.wr(~second & 1, Const.IO_WD);
			Native.wr(second & 1, Const.IO_WD);

			next = waitForNextInterval(next);
		}
	}

	static int waitForNextInterval(int next) {

		final int INTERVAL = 1000;		// one ms

		if (next==0) {
			next = Native.rd(Const.IO_US_CNT)+INTERVAL;
		} else {
			next += INTERVAL;
		}

		while (next-Native.rd(Const.IO_US_CNT) >= 0)
				;

		return next;
	}
	
	public static void main(String [] args) throws Exception {

		Native.wr(0, Const.IO_WD);		// make WD happy
		Native.wr(1, Const.IO_WD);
		Native.wr(0, Const.IO_WD);
		time();
	}
}
