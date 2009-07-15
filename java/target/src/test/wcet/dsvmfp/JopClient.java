package wcet.dsvmfp;

import com.jopdesign.sys.Native;

import util.Dbg;

import joprt.RtThread;
import wcet.dsvmfp.util.UdpJop;
import wcet.dsvmfp.model.smo.classification.SMOBinaryClassifierFP;
import wcet.dsvmfp.model.smo.kernel.FP;
import wcet.dsvmfp.util.DsvmPacket;
/**
 * 
 * @author rup.inf
 * GPL
 */
public class JopClient {
	// Set by UdpJop receive thread
	static boolean info;

	static UdpJop udpJop;

	static int sendcnt;

	static int BOOTSTATE = 0;

	static int INITSTATE = 1;

	static int TRAININGSTATE = 2;

	static int TESTINGSTATE = 3;

	static int ENDSTATE = 4;

	static int NEVER = 100;

	static int state;

	static boolean receivedPacket;

  static int counter = 0;

  static int m;
  static int n = 1;
  static int[][] data_fp;
  static int[] y_fp;

  // PC IP //130.226.36.11
  static int destIp;
  static int destPort;
  static int receivePort;

	static public void main(String[] args) {

		Dbg.initSerWait();
		DsvmPacket.init();

		receivedPacket = false;

		state = BOOTSTATE;

		sendcnt = 0;
		info = false;
		counter = 0;
		// SVM data and the place to decide the size of the training set
		m = 100; // i, number of data
		n = 2; // j, dimensions
		data_fp = new int[m][];
		y_fp = new int[m];
		// PC IP //130.226.36.11
		destIp = (130 << 24) + (226 << 16) + (36 << 8) + 11;
		destPort = 1234;
		receivePort = 2345;
    // 24-2-2006: Hack to get the thread to init now because "new UdpJop..."  
    //            causes a stack overflow if the thread init is done in that 
    //            call flow
    new RtThread(0,1000000000){
      public void run(){
        for(;;){
          waitForNextPeriod();
        }
      }
         
    };
    udpJop = new UdpJop(destIp, destPort, receivePort);

//		new RtThread(20, 100000) {
//			public void run() {
//				for (;;) {
//					if (SMOBinaryClassifierFP.printSMOInfo) {
//						SMOBinaryClassifierFP.smoInfo();
//						SMOBinaryClassifierFP.printSMOInfo = false;
//					}
//					waitForNextPeriod();
//				}
//			}
//		};
//
//		new RtThread(19, 100000) {
//			public void run() {
//				for (;;) {
//					if (SMOBinaryClassifierFP.takeStepFlag
//							&& SMOBinaryClassifierFP.go) {
//						SMOBinaryClassifierFP.takeStep();
//						SMOBinaryClassifierFP.takeStepFlag = false;
//					}
//					waitForNextPeriod();
//				}
//			}
//		};

//		class Test extends SwEvent {
//			static Test testsmo;
//			public Test(int priority, int minTime) {
//				super(priority, minTime);
//			}
//
//			public void handle() {
//				test();
//			}
//		};
		
		
		

		RtThread.startMission();

		// System.out.println("about to sleep");
		//RtThread.sleepMs(2000); // Waiting for alive packet to initialize arp
		// System.out.println("slept");
		/*
		 * DsvmPacket dp = new DsvmPacket(); dp.setData(new int[2]);
		 * System.out.println("about to send"); udpJop.send(dp);
		 * System.out.println("test packet sent"); okSend = true;
		 * System.out.println("HERE2");
		 */

		int cnt = 0;
		// gccheck();
		// System.out.println("before initstate loop");
		// System.out.println("state " + state);
		while (state != INITSTATE) {
			RtThread.sleepMs(100);
		}

		System.out.println("INITSTATE");
		RtThread.sleepMs(1000);
		DsvmPacket.setCommand(DsvmPacket.INIT);
		// System.out.println("about to send init");
		udpJop.send(); // Training

		RtThread.sleepMs(500);

		System.out.println("TRAINING");
		for (int i = 0; i < m; i++) {
			
			// System.out.println("About to DsvmPacket.TRAININGDATAREQUEST" +
			// i);
      DsvmPacket.setCommand(DsvmPacket.TRAININGDATAREQUEST);
			receivedPacket = false;
			udpJop.send();
			waitForReply();
			// System.out.println("got training data i=" + i);
			if (DsvmPacket.getCommand() != DsvmPacket.TRAININGDATA) {
				System.out.println("Expected training data!");
				System.exit(1);
			}

			//Creates a new object
      data_fp[i] = DsvmPacket.getDataFP();
			y_fp[i] = DsvmPacket.getLabelFP();

			if (info) {
				System.out.println((counter++) + " trainingData={");
				for (int j = 0; j < n; j++) {
					System.out.println("data_fp[" + i + "][" + j + "]="
							+ data_fp[i][j]);
				}
				System.out.println("}, y_fp[" + i + "]=" + y_fp[i]);
			}
		}
		SMOBinaryClassifierFP.setData_fp(data_fp);
		SMOBinaryClassifierFP.setY_fp(y_fp);
		//SMOBinaryClassifierFP.smo.fire();
    SMOBinaryClassifierFP.mainRoutine();
		
		//while(!SMOBinaryClassifierFP.done)
		//	RtThread.sleepMs(500);
        
		System.out.println("About to test");
		test();
		
		for (;;) {
			System.out.println("cnt " + (cnt++));
			// Timer.wd();
			RtThread.sleepMs(1000);
			if (state == NEVER)
				break;
		}
	}

	public static void test() {
		for (int i = 0; i < SMOBinaryClassifierFP.m; i++) {
			DsvmPacket.setCommand(DsvmPacket.TESTDATAREQUEST);
      receivedPacket = false;
      udpJop.send();
System.out.println("DsvmPacket.TESTDATAREQUEST sent");			
      
			waitForReply();
			System.out.println("Receive command="+DsvmPacket.getCommand());
			if (DsvmPacket.getCommand() != DsvmPacket.TESTDATA){
				System.out.println("Expected test data! Not "+DsvmPacket.getCommand());
        System.exit(-1);
      }

			int[] testData_fp = DsvmPacket.getDataFP();
			int funcOut_fp = SMOBinaryClassifierFP
					.getFunctionOutputTestPointFP(testData_fp);
			int yguess_fp = 0;
			if (funcOut_fp >= 0)
				yguess_fp = FP.ONE;
			else
				yguess_fp = -FP.ONE;
			
			//packetData = DsvmPacket.makeLabelFP(yguess_fp);
			DsvmPacket.setCommand(DsvmPacket.TESTDATAGUESS);
			DsvmPacket.setLabelFP(yguess_fp);
			udpJop.send();
      receivedPacket = false;
System.out.println("DsvmPacket.TESTDATAGUESS sent");			

			waitForReply();
			if (DsvmPacket.getCommand() != DsvmPacket.TESTDATAANSWER){
				System.out.println("Expected test data answer! Not "+DsvmPacket.getCommand());
        System.exit(-1);
      }

  		int yanswer_fp = DsvmPacket.getLabelFP();
			boolean ok;
			if (yanswer_fp == yguess_fp)
				ok = true;
			else
				ok = false;
			if (info) {
				int counter = 0;
				System.out.println((counter++) + " testData_fp={");
				for (int j = 0; j < SMOBinaryClassifierFP.n; j++) {
					System.out.println("testData_fp[" + j + "]="
							+ testData_fp[j]);
				}
				if (ok)
					System.out.println("}, correct:true");
				else
					System.out.println("}, correct:false");
			}
		}
    SMOBinaryClassifierFP.smoInfo();
		DsvmPacket.setCommand(DsvmPacket.END);
		udpJop.send();
		System.out.println("Training error count="
				+ SMOBinaryClassifierFP.getTrainingErrorCountFP());

	}

	private static void waitForReply() {
		while (!receivedPacket){
			RtThread.sleepMs(10);
		}
	}

	public static void receive() {
		System.out.print("R ");
    System.out.println(DsvmPacket.getCommand());

    if (info) {
			for (int i = 0; i < DsvmPacket.length; i++) {
				System.out.println("JopCliet.receive, rcv.payLoad[" + i
						+ "] = " + DsvmPacket.pLoad[i]);
			}
		}

		if (DsvmPacket.getCommand() == DsvmPacket.ALIVEREQUEST) {
			System.out.println("ALIVEREQUEST");
			DsvmPacket.setCommand(DsvmPacket.ALIVEREPLY);
      DsvmPacket.setId(sendcnt++);
			udpJop.send();
			state = INITSTATE;
		}
		receivedPacket = true;
	}

	public static void gccheck() {
		System.out.print("GC free words ");
		//System.out.println(GC.free());
	}
}