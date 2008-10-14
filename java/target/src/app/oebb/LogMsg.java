package oebb;

import java.util.Vector;

public class LogMsg {

	// TODO: reduce to 5 again when FERL issue is solved
	static final int MAX_MSG = 10;
	static final int MAX_SIZE = 40;
	
	static Vector free = new Vector(MAX_MSG);
	static Vector toSend = new Vector(MAX_MSG);
	static {
		for (int i=0; i<MAX_MSG; ++i) {
			free.addElement(new LogMsg());
		}
	}
	
	int bgid;
	int date;
	int time;
	StringBuffer msg;
	
	private LogMsg() {
		msg = new StringBuffer(MAX_SIZE);
	}
	
	/**
	 * Get a free message buffer and set date/time and StrinBuffer
	 * length to 0
	 * @return
	 */
	static LogMsg getFreeMsg() {
		LogMsg lm = null;
		synchronized (free) {
			if (!free.isEmpty()) {
				lm = (LogMsg) free.remove(0);
			}			
		}
		if (lm!=null) {
			lm.msg.setLength(0);
			lm.date = State.getDate();
			lm.time = State.getTime();			
		}

		return lm;
	}
	
	static LogMsg getSendMsg() {
		LogMsg lm = null;
		synchronized (toSend) {
			if (!toSend.isEmpty()) {
				lm = (LogMsg) toSend.remove(0);		
			}			
		}
		return lm;
	}
	
	void addToFreeList() {
		free.addElement(this);
	}
	
	void addToSendList() {
		toSend.addElement(this);
	}

}
