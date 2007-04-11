/*
 * Created on 12.07.2004
 *
 * 
 */
package jbe.lift;


/**
 * @author martin
 *
 * Control version for jbe
 */
public abstract class Control {

//	private static RtThread th;
//	private int period;
	protected TalIo io;
	
	protected int dly1, dly2;
	
	private int min, max;			// execution time status
	
	public Control(int ms) {
		
//	original Lift controller creates the periodic thread here
//
//		if (th==null) {
			io = new TalIo();
			dly1 = 0;
			dly2 = 0;
//			min = 9999999;
//			max = 0;
//			getVals();				// set 'real' values for the first iteration
//			th = new RtThread(10, ms*1000) {
//				public void run() {
//					for (;;) {
//						waitForNextPeriod();
//						int t = Native.rd(Const.IO_US_CNT);
//						
//						setVals();	// update output with minimum jitter
//						getVals();	// read new input and
//						loop(io);	// process the values
//						
//						// some statistcs
//						t = Native.rd(Const.IO_US_CNT)-t;
//						if (t<min) min = t;
//						if (t>max) max = t;
//					}
//				}
//
//			};
//		}
	}
	
	// was private - public for WCET tests
	public void getVals() {
		int in0 = SimLiftIo.rd(SimLiftIo.IO_IN);
		int in1 = dly1;
		int in2 = dly2;
		dly2 = dly1;
		dly1 = in0;
		for (int i=0; i<10; ++i) { // @WCA loop=10
			// majority voting for input values
			// delays input value change by one period
			io.in[i] = ((in0&1) + (in1&1) + (in2&1)) > 1;
			in0 >>>= 1;
			in1 >>>= 1;
			in2 >>>= 1;
		}
		io.analog[0] = SimLiftIo.rd(SimLiftIo.IO_ADC1);
		io.analog[1] = SimLiftIo.rd(SimLiftIo.IO_ADC2);
		io.analog[2] = SimLiftIo.rd(SimLiftIo.IO_ADC3);
	}
	
	public void setVals() {
		int val = 0;
		for (int i=3; i>=0; --i) { // @WCA loop=3
			val <<= 1;
			val |= io.out[i] ? 1 : 0;
		}
		SimLiftIo.wr(val, SimLiftIo.IO_OUT);
		for (int i=13; i>=0; --i) { // @WCA loop=14
			val <<= 1;
			val |= io.led[i] ? 1 : 0;
		}
		SimLiftIo.wr(val, SimLiftIo.IO_LED);
	}
	/**
	 * The only method that should be overwritten.
	 */
	public abstract void loop(TalIo io);
	/**
	 * @return
	 */
	public int getMax() {
		return max;
	}

	/**
	 * @return
	 */
	public int getMin() {
		return min;
	}

}
