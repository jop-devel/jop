package csp.scj.watchdog;

import javax.realtime.InterruptServiceRoutine;
import javax.realtime.PeriodicParameters;
import javax.realtime.PriorityParameters;
import javax.realtime.RelativeTime;
import javax.safetycritical.Mission;
import javax.safetycritical.PeriodicEventHandler;
import javax.safetycritical.StorageParameters;

import util.Timer;

import csp.Buffer;
import csp.Constants;
import csp.IODevice;
import csp.ImmortalEntry;
import csp.Node;
import csp.Services;

public class WatchdogHandler extends PeriodicEventHandler {

	int source;
	Node[] slaves;
	int count, slaveIndex = 0;

	public WatchdogHandler(int interval, Node[] slaves) {

		super(new PriorityParameters(11), new PeriodicParameters(
				new RelativeTime(0, 0), new RelativeTime(interval, 0)),
				new StorageParameters(1024, null), 512, "WD_task");

		this.slaves = slaves;

	};

	@Override
	public void handleAsyncEvent() {
		
		ImmortalEntry.log.addEvent(getName());

		// First check the reply from previously sent ping
		checkReply();

		// Set address of next slave to ping
		if (slaveIndex >= Constants.NUM_SLAVES - 1) {
			slaveIndex = 0;
		} else {
			slaveIndex++;
		}

		ImmortalEntry.ping.destination = slaves[slaveIndex].getAddress();

		// Send CSP ping packet
		Services.sendPacket(ImmortalEntry.ping, null);//new int[]{0,1,3,7,15,31});
		slaves[slaveIndex].incPacketSent();
		
		// Check transmission status
		checkTxStatus();

		// Change to slave mode to wait for the reply
		ImmortalEntry.ping.iface.getIODevice().setControl(
				I2CBusController.SLAVE);

		count++;
		if (count == 1000) {
			cleanUp();
			Mission.getCurrentMission().requestSequenceTermination();
		}
	}

	private void checkReply() {

		// If there was a reply, there should be a packet in the ping connection
		// queue
		Buffer buffer = ImmortalEntry.ping.queue.deq();

		if (buffer == null) {
			//ImmortalEntry.term.writeln("Node : " + ImmortalEntry.ping.destination+ " FAIL");
			slaves[slaveIndex].setAlive(false);

		} else {
			// Do ping processing...
			//ImmortalEntry.term.writeln("Node: "+ ImmortalEntry.ping.destination+ " OK");
			ImmortalEntry.slaves[slaveIndex].incPacketReceived();
			ImmortalEntry.bufferPool.freeCSPbuffer(buffer);
		}

	}

	private void checkTxStatus() {

		IODevice device = ImmortalEntry.ping.iface.getIODevice();

		int status = device.readStatus();
	}

}
