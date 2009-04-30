package com.jopdesign.timing.jop;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.jopdesign.timing.jop.MicrocodeAnalysis.MicrocodeVerificationException;
import com.jopdesign.tools.JopInstr;

/** 
 *  Table of micropathes.
 *  Used to generate the JOPTimingTable and will be used to calculate CMP timings.
 *  If the assembler File is not available, we use a static timing table.
 */
public class MicropathTable implements Serializable {
	private static final long serialVersionUID = 1L;

	private HashSet<Integer> hasMicrocode = new HashSet<Integer>();	
	private HashMap<Integer,Vector<MicrocodePath>> paths = new HashMap<Integer, Vector<MicrocodePath>>();
	private HashSet<Integer> notImplemented = new HashSet<Integer>();
	private TreeMap<Integer, MicrocodeVerificationException> analysisErrors =
		new TreeMap<Integer, MicrocodeVerificationException>();
	
	public boolean hasMicrocodeImpl(int opcode) {
		return hasMicrocode.contains(opcode);
	}

	public Vector<MicrocodePath> getMicroPaths(int opcode) {
		return paths.get(opcode);
	}
	
	
	public boolean isImplemented(int opcode) {
		return ! (JopInstr.isReserved(opcode) || notImplemented.contains(opcode));
	}
	
	public boolean hasTiming(int opcode) {
		return paths.containsKey(opcode);
	}
	
	public MicrocodeVerificationException getAnalysisError(int opcode) { 
		return analysisErrors.get(opcode); 
	}
	public TreeMap<Integer, MicrocodeVerificationException> getAnalysisErrors() {
		return analysisErrors;
	}
	/** Get the timing table for JOP
	 * 
	 * @param asm the assembler file, or null if no File is available
	 * @return the path table
	 */
	public static MicropathTable getTimingTable(File asm) {		
		MicropathTable mpt;
		mpt = getPrecompiledTable(asm);
		if(mpt == null) {
			try {
				mpt = getTimingTableFromAsmFile(asm);
			} catch (IOException e) {
				throw new AssertionError("Failed to read assembler file");
			}
			try {
				mpt.dump(asm);
			} catch (IOException e) {
				Logger.getLogger(MicropathTable.class).warn("Failed to dump precompiled table");
			}
		}
		return mpt;
	}


	public static MicropathTable getTimingTableFromAsmFile(File asm) throws IOException  {
		MicrocodeAnalysis ana = new MicrocodeAnalysis(asm.getPath());
		MicropathTable pt = new MicropathTable();
		for(int i = 0; i < 256; i++) {
			if(JopInstr.isReserved(i)) {
				continue;
			}
			try {
				Integer addr = ana.getStartAddress(i);
				
				if(addr != null) {
					pt.hasMicrocode.add(i);
					pt.paths.put(i,ana.getMicrocodePaths(JopInstr.OPCODE_NAMES[i], addr));
				} else {
					pt.notImplemented.add(i);
				}
			} catch(MicrocodeVerificationException e) {
				pt.analysisErrors.put(i,e);
			} 
		}
		return pt;
	}
	private static final File getPrecompiledFile(File asmFile) {
		return new File(asmFile.getPath() + ".precompiled");
	}
	public static MicropathTable getPrecompiledTable(File asmFile) {
		try {
			File precompiled = getPrecompiledFile(asmFile);
			if(! precompiled.exists()) {
				Logger.getLogger(MicropathTable.class).warn("No precompiled timing info");
				return null;
			}
			byte[] checksum = getDigest(asmFile);
			InputStream is = new FileInputStream(precompiled);
			if(is == null) return null;
			ObjectInputStream ois = new ObjectInputStream(is);
			byte[] digest = (byte[]) ois.readObject();
			if(checksum != null) {
				if(! digest.equals(checksum)) return null;
				Logger.getLogger(MicropathTable.class).info("Precompiled table's digest matches, using it");
			}
			MicropathTable table = (MicropathTable) ois.readObject();
			ois.close();
			return table;
		} catch (IOException e) {
			e.printStackTrace();
			throw new AssertionError("IOException when reading precompiled table: "+e);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new AssertionError("Class Not Found when reading precompiled table: "+e);
		} catch (ClassCastException e) {
			e.printStackTrace();
			throw new AssertionError("Class Cast Exception when reading precompiled table: "+e);
		}
	}
	public static void dumpTable(File asm) throws IOException {
		MicropathTable mpt = getTimingTableFromAsmFile(asm);
		mpt.dump(asm);
	}
	private void dump(File assemblerFile) throws IOException {
		File out = getPrecompiledFile(assemblerFile);
		FileOutputStream fileOut = new FileOutputStream(out);
		ObjectOutputStream oos = new ObjectOutputStream(fileOut);
		byte[] digest = getDigest(assemblerFile);
		oos.writeObject(digest);
		oos.writeObject(this);
		fileOut.close();		
	}
	public static void main(String[] argv) {
		String currentDir = new File(".").getAbsolutePath();
		System.out.println("Running in [needs to be JOP root]: "+currentDir);
		try {
			dumpTable(MicrocodeAnalysis.DEFAULT_ASM_FILE);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/** FIXME: Move to common utility package, if we have one.
	 *  I do not want to depend on MiscUtils in the WCET package.
	 */
	public static boolean fileMatch(File f, byte[] checksum)
		throws IOException {
		return getDigest(f).equals(checksum);
	}
	public static byte[] getDigest(File f) throws IOException {
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new IOException("Failed to instantiate MD5 digest");
		}
		FileInputStream fr = new FileInputStream(f);
		byte[] buffer = new byte[1024];
		int readBytes;
		while((readBytes = fr.read(buffer)) > 0) {
			 md.update(buffer,0,readBytes);
		}
		fr.close();
		return md.digest();
	}
	
}
