package csp.scj.watchdog;

import javax.realtime.RawByte;
import javax.realtime.RawByteArray;
import javax.realtime.RawByteArrayRead;
import javax.realtime.RawByteArrayWrite;
import javax.realtime.RawByteRead;
import javax.realtime.RawByteWrite;
import javax.realtime.RawInt;
import javax.realtime.RawIntArray;
import javax.realtime.RawIntArrayRead;
import javax.realtime.RawIntArrayWrite;
import javax.realtime.RawIntRead;
import javax.realtime.RawIntWrite;
import javax.realtime.RawIntegralAccessFactory;
import javax.realtime.RawMemory;
import javax.realtime.RawMemoryName;
import javax.safetycritical.annotate.Level;
import javax.safetycritical.annotate.SCJAllowed;
import javax.safetycritical.annotate.SCJRestricted;

import com.jopdesign.sys.Native;

public class GeneralIOFactory implements RawIntegralAccessFactory{
	
	@Override
	@SCJAllowed(Level.LEVEL_0)
	public RawMemoryName getName() {
		return RawMemory.IO_MEM_MAPPED;
	}

	@Override
	@SCJAllowed(Level.LEVEL_0)
	@SCJRestricted(mayAllocate = false, maySelfSuspend = false)
	public RawByte newRawByte(long offset) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@SCJAllowed(Level.LEVEL_0)
	@SCJRestricted(mayAllocate = false, maySelfSuspend = false)
	public RawByteArray newRawByteArray(long base, int entries) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@SCJAllowed(Level.LEVEL_0)
	@SCJRestricted(mayAllocate = false, maySelfSuspend = false)
	public RawByteArrayRead newRawByteArrayRead(long base, int entries) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@SCJAllowed(Level.LEVEL_0)
	@SCJRestricted(mayAllocate = false, maySelfSuspend = false)
	public RawByteArrayWrite newRawByteArrayWrite(long base, int entries) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@SCJAllowed(Level.LEVEL_0)
	@SCJRestricted(mayAllocate = false, maySelfSuspend = false)
	public RawByteRead newRawByteRead(long offset) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@SCJAllowed(Level.LEVEL_0)
	@SCJRestricted(mayAllocate = false, maySelfSuspend = false)
	public RawByteWrite newRawByteWrite(long offset) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@SCJAllowed(Level.LEVEL_0)
	@SCJRestricted(mayAllocate = false, maySelfSuspend = false)
	public RawInt newRawInt(long offset) {

		final int address = (int) offset;
		
		return new RawInt() {
			
			@Override
			@SCJAllowed(Level.LEVEL_0)
			@SCJRestricted(mayAllocate = false, maySelfSuspend = false)
			public void put(int value) {
				Native.wrMem(value, address);
				
			}
			
			@Override
			@SCJAllowed(Level.LEVEL_0)
			@SCJRestricted(mayAllocate = false, maySelfSuspend = false)
			public int get() {
				return Native.rdMem(address);
			}
		};
		
	}
	
	@Override
	@SCJAllowed(Level.LEVEL_0)
	@SCJRestricted(mayAllocate = false, maySelfSuspend = false)
	public RawIntRead newRawIntRead(long offset) {
		
		final int address = (int) offset;
		
		return new RawIntRead() {
			
			@Override
			@SCJAllowed(Level.LEVEL_0)
			@SCJRestricted(mayAllocate = false, maySelfSuspend = false)
			public int get() {
				return Native.rdMem(address);
			}
		};
		
	}

	@Override
	@SCJAllowed(Level.LEVEL_0)
	@SCJRestricted(mayAllocate = false, maySelfSuspend = false)
	public RawIntWrite newRawIntWrite(long offset) {
		
		final int address = (int) offset;
		
		return new RawIntWrite() {
			
			@Override
			@SCJAllowed(Level.LEVEL_0)
			@SCJRestricted(mayAllocate = false, maySelfSuspend = false)
			public void put(int value) {
				Native.wrMem(value, address);
				
			}
		};
		
	}


	@Override
	@SCJAllowed(Level.LEVEL_0)
	@SCJRestricted(mayAllocate = false, maySelfSuspend = false)
	public RawIntArray newRawIntArray(long base, int entries) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@SCJAllowed(Level.LEVEL_0)
	@SCJRestricted(mayAllocate = false, maySelfSuspend = false)
	public RawIntArrayRead newRawIntArrayRead(long base, int entries) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@SCJAllowed(Level.LEVEL_0)
	@SCJRestricted(mayAllocate = false, maySelfSuspend = false)
	public RawIntArrayWrite newRawIntArrayWrite(long base, int entries) {
		// TODO Auto-generated method stub
		return null;
	}

}
