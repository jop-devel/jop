package com.jopdesign.wcet08.uppaal;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;

import org.apache.log4j.Logger;

import com.jopdesign.wcet08.Config;

/**
 * Binary search for WCET using UppAal
 * @author Benedikt Huber <benedikt.huber@gmail.com>
 *
 */
public class WcetSearch {
	private File modelFile;
	private File queryFile;
	private Logger logger = Logger.getLogger(WcetSearch.class);
	private double lastSolverTime = -1;
	public double getLastSolverTime() {
		return lastSolverTime;
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
	private boolean checkBound(String[] cmd, long m) throws IOException {
		writeQueryFile(queryFile, m);
		long start = System.nanoTime();
		Process verifier = Runtime.getRuntime().exec(cmd);
		InputStream is = new BufferedInputStream(verifier.getInputStream());
		try {
			if(verifier.waitFor() != 0) {
				InputStream eis = new BufferedInputStream(verifier.getErrorStream());
				BufferedInputStream ebis = new BufferedInputStream(eis);
				BufferedReader ebr = new BufferedReader(new InputStreamReader(ebis));
				String l;
				while((l=ebr.readLine()) != null) logger.error("verifyta: "+l);
				throw new IOException("Uppaal verifier terminated with exit code: "+verifier.exitValue());
			} else {
				long stop  = System.nanoTime();
				lastSolverTime = ((double)(stop-start)) / 1.0E9;				
			}
		} catch (InterruptedException e) {
			throw new IOException("Interrupted while waiting for verifier to finish");
		}
		BufferedInputStream bis = new BufferedInputStream(is);
		BufferedReader br = new BufferedReader(new InputStreamReader(bis));
		return checkIfSafe(br);	
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
	private boolean checkIfSafe(BufferedReader br) throws IOException {
		String l,last=null;
		while(null != (l=br.readLine())) {
			last = l; 
		}
		if(last == null) {
			throw new IOException("No output from verifyta");
		}
		if(last.matches(".*NOT satisfied.*")) {
			return false;
		} else if(last.matches(".*Property is satisfied.*")) {
			return true;
		} else {
			throw new IOException("Unexpected output from verifyta: "+last);
		}
	}
}
