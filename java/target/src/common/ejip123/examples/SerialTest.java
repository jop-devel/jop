package ejip123.examples;

import com.jopdesign.sys.Const;
import joprt.RtThread;
import ejip123.util.Serial;

public class SerialTest{
private SerialTest(){
}

public static void main(String[] args){
	new RtThread(9, 10000){
		private Serial ser = new Serial(10, 400, Const.IO_UART1_BASE);
		private int err = 0;
		private int loop = 0;

		public void run(){
			while(true){
				waitForNextPeriod();
				int rxCnt = ser.rxCnt();
				for(int i = 0; i < rxCnt; i++){
					if(ser.txFreeCnt() <= 0){
						err++;
					} else
						ser.wr(ser.rd());
				}

			}
		}
	};
	RtThread.startMission();

	while(true){
	}
}
}
