package com.jopdesign.wcet08.uppaal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.jopdesign.wcet08.config.Config;

/**
 * Binary search for WCET using UppAal
 * @author Benedikt Huber <benedikt.huber@gmail.com>
 *
 */
public class WcetSearch {
	private File modelFile;
	private File queryFile;
	private Logger logger = Logger.getLogger(WcetSearch.class);
	private double maxSolverTime = 0.0;
	public double getMaxSolverTime() {
		return maxSolverTime;
	}
	public WcetSearch(File modelFile) {
		this.modelFile = modelFile;
	}
	public long searchWCET() throws IOException {
		queryFile = File.createTempFile("query", ".q");
		String[] cmd = {
				Config.instance().getOption(UppAalConfig.UPPAAL_VERIFYTA_BINARY),
				"-q",
				modelFile.getPath(),
				queryFile.getPath()				
		};
		long unsafe = 1;
		long safe = 100;
		try {
			while(! checkBound(cmd,safe)) {
				unsafe = safe;
				safe *= 2;
				System.err.println(MessageFormat.format("WCET bounds (unsafe/safe): {0}/{1}",
							  				     unsafe, safe));
			}
			while(unsafe + 1 < safe) {
				long m = unsafe+((safe-unsafe)/2);
				if(checkBound(cmd,m)) { /* m is a safe bound */
					safe = m;
				} else {
					unsafe = m;
				}
				System.err.println(MessageFormat.format("WCET bounds (unsafe/safe): {0}/{1}",
	  				                             unsafe, safe));
			}
		} finally {
			queryFile.delete();
		}
		return safe;
	}
	private class StreamReaderThread extends Thread {
		private Vector<String> data = null;
		private BufferedReader reader;
		private InputStream is;
		private int limit;
		private LinkedList<String> dataList = null;
		private boolean doEcho = false;
		private int doStatusEcho;

		public StreamReaderThread(InputStream inputStream) {
			this.is = inputStream;
			this.reader = new BufferedReader(new InputStreamReader(inputStream));
			this.data = new Vector<String>();
			this.limit = -1;
		}
		public StreamReaderThread(InputStream inputStream,int limit) {
			this.is = inputStream;
			this.reader = new BufferedReader(new InputStreamReader(inputStream));
			this.limit = limit;
			this.dataList = new LinkedList<String>();
		}

		@Override
		public void run() {
			String l ;
			try {
				while(null != (l=reader.readLine())) {
					if(doEcho) System.out.println(l);
					if(doStatusEcho > 0) {
						System.out.print(".");
						System.out.flush();
					}
					process(l);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		public Vector<String> getData() {
			if(dataList != null) {
				data = new Vector<String>(dataList);
			}
			return data;
		}
		protected void process(String l) {
			if(dataList == null) data.add(l);
			else {
				dataList.offer(l);
				if(dataList.size() > limit) dataList.remove();
			}			
		}
		public String getMessage() {
			return this.data.toString();
		}
		public void setEcho(boolean b) {
			doEcho  = b;			
		}
		public void setStatusEcho(boolean b) {
			doStatusEcho = 1;			
		}
	}
	private boolean checkBound(String[] cmd, long m) throws IOException {
		writeQueryFile(queryFile, m);
		long start = System.nanoTime();
		Process verifier = Runtime.getRuntime().exec(cmd);
		StreamReaderThread outLines = new StreamReaderThread(verifier.getInputStream(),3);
		//outLines.setEcho(true);
		outLines.setStatusEcho(true);
		outLines.run();
		StreamReaderThread errLines = new StreamReaderThread(verifier.getErrorStream());
		errLines.run();
		try {
			if(verifier.waitFor() != 0) {
				logger.error("verifyta: "+errLines.getMessage());
				throw new IOException("Uppaal verifier terminated with exit code: "+verifier.exitValue());
			} else {
				long stop  = System.nanoTime();
				maxSolverTime = Math.max(maxSolverTime,((double)(stop-start)) / 1.0E9);				
			}
		} catch (InterruptedException e) {
			throw new IOException("Interrupted while waiting for verifier to finish");
		}
		return checkIfSafe(outLines.getData());	
	}
	private String getQuery(long bound) {
		return "A[] (M0.E imply t<="+bound+")";
	}
	private void writeQueryFile(File queryFile, long bound) throws IOException {
		FileWriter fw = new FileWriter(queryFile);
		fw.write(getQuery(bound));
		fw.write('\n');
		fw.close();		
	}
	private boolean checkIfSafe(Vector<String> vector) throws IOException {
		if(vector.size() == 0) {
			throw new IOException("No output from verifyta");
		}
		String last = vector.lastElement();
		if(last.matches(".*NOT satisfied.*")) {
			return false;
		} else if(last.matches(".*Property is satisfied.*")) {
			return true;
		} else {
			throw new IOException("Unexpected output from verifyta: "+last);
		}
	}
}
