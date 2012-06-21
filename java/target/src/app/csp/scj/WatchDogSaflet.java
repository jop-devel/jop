package csp.scj;

import java.io.IOException;
import java.io.OutputStream;

import javax.microedition.io.Connector;
import javax.realtime.PriorityParameters;
import javax.safetycritical.LinearMissionSequencer;
import javax.safetycritical.Mission;
import javax.safetycritical.MissionSequencer;
import javax.safetycritical.Safelet;
import javax.safetycritical.StorageParameters;
import javax.safetycritical.io.SimplePrintStream;

import com.jopdesign.io.I2CFactory;
import com.jopdesign.io.I2Cport;

import csp.CSP;

public class WatchDogSaflet implements Safelet{

	static SimplePrintStream out;

	static final int SRC_ADDRESS = 15;
	static final int NUM_SLAVES = 10;
	static final int WD_TIMEOUT = 1;

	public static I2Cport portA;
	public static int[] slaves;

	public void setup(){

		OutputStream os = null;
		try {
			os = Connector.openOutputStream("console:");
		} catch (IOException e) {
			throw new Error("No console available");
		}
		out = new SimplePrintStream(os);
		out.println("Startup...");

		I2CFactory fact = I2CFactory.getFactory();

		// Source IIC
		portA = fact.getI2CportA();
		portA.initConf(SRC_ADDRESS);

		// Initialize CSP buffer pool
		CSP.initBufferPool();

		slaves = new int[NUM_SLAVES];

		for (int i = 0; i < NUM_SLAVES; i++){
			slaves[i] = i;
		}

		out.println("Startup ok...");
	}

	@Override
	public MissionSequencer<Mission> getSequencer() {

		StorageParameters sp = new StorageParameters(1000000000, null);
		WatchDogMission m = new WatchDogMission();
		return new LinearMissionSequencer<Mission>(new PriorityParameters(13), sp, m);
	}

	@Override
	public long immortalMemorySize() {
		// TODO Auto-generated method stub
		return 100;
	}

}
