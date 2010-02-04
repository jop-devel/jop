package scd_micro;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * The constructor runs and the instance lives in the persistent detector scope. The state table 
 * passed to it lives in the persistent detector scope. The thread runs in transient detector
 * scope. The frame (currentFrame) lives in immortal memory.
 * 
 * The real collision detection starts here.
 */
public class TransientDetectorScopeEntry implements Runnable {

	private StateTable state;
	private float voxelSize;
	private RawFrame currentFrame;

	public TransientDetectorScopeEntry(final StateTable s, final float voxelSize) {
		state = s;
		this.voxelSize = voxelSize;
	}

	public void run() {

		if (Constants.SYNCHRONOUS_DETECTOR || Constants.DEBUG_DETECTOR) {
			dumpFrame("CD-PROCESSING-FRAME (indexed as received): ");
		}

		final Reducer reducer = new Reducer(voxelSize);
		final List collisions = lookForCollisions(reducer, createMotions());

		int numberOfCollisions = collisions.size();
		if (ImmortalEntry.recordedRuns < ImmortalEntry.maxDetectorRuns) {
			ImmortalEntry.detectedCollisions[ ImmortalEntry.recordedRuns  ] = numberOfCollisions;
		}

		if (Constants.SYNCHRONOUS_DETECTOR || Constants.DEBUG_DETECTOR) {
			System.out.println("CD detected  "+numberOfCollisions+" collisions.");
			int colIndex = 0;

			for(final Iterator iter = collisions.iterator(); iter.hasNext();) {
				Collision col = (Collision) iter.next();
				List aircraft = col.getAircraftInvolved();
				System.out.println("CD collision "+colIndex+" occured at location "+col.getLocation() + " with "+aircraft.size()+" involved aircraft.");

				for(final Iterator aiter = aircraft.iterator(); aiter.hasNext();) {
					Aircraft a = (Aircraft) aiter.next();

					System.out.println("CD collision "+colIndex+" includes aircraft "+a);
				}
				colIndex++;
			}

			System.out.println("");
		}
		/*
		if (Constants.SYNCHRONOUS_DETECTOR) {
			FrameSynchronizer.consumeFrame();        
		}
		*/
	}

	public List lookForCollisions(final Reducer reducer, final List motions) {
		final List check = reduceCollisionSet(reducer, motions);
		final CollisionCollector c = new CollisionCollector();

		int suspectedSize = check.size();
		if (ImmortalEntry.recordedRuns < ImmortalEntry.maxDetectorRuns) {
			ImmortalEntry.suspectedCollisions[ ImmortalEntry.recordedRuns  ] = suspectedSize;
		}
		if ((Constants.SYNCHRONOUS_DETECTOR || Constants.DEBUG_DETECTOR) && !check.isEmpty()) {
			System.out.println("CD found "+suspectedSize+" potential collisions");
			int i=0;
			for(final Iterator iter = check.iterator(); iter.hasNext();) {
				final List col = (List)iter.next();

				for(final Iterator aiter = col.iterator(); aiter.hasNext();) {
					final Motion m = (Motion)aiter.next();

					System.out.println("CD: potential collision "+i+" (of "+col.size()+" aircraft) includes motion "+m);
				}
				i++;            
			}
		}

		for (final Iterator iter = check.iterator(); iter.hasNext();)
			c.addCollisions(determineCollisions((List) iter.next()));
		return c.getCollisions();
	}

	/**
	 * Takes a List of Motions and returns an List of Lists of Motions, where the inner lists implement RandomAccess.
	 * Each Vector of Motions that is returned represents a set of Motions that might have collisions.
	 */
	public List reduceCollisionSet(final Reducer it, final List motions) {

		final HashMap voxel_map = new HashMap();
		final HashMap graph_colors = new HashMap();

		for (final Iterator iter = motions.iterator(); iter.hasNext();)
			it.performVoxelHashing((Motion) iter.next(), voxel_map, graph_colors);

		final List ret = new LinkedList();
		for (final Iterator iter = voxel_map.values().iterator(); iter.hasNext();) {
			final List cur_set = (List) iter.next();
			if (cur_set.size() > 1) ret.add(cur_set);
		}
		return ret;
	}

	public List determineCollisions(final List motions) {
		final List ret = new LinkedList();
		for (int i = 0; i < motions.size() - 1; i++) {
			final Motion one = (Motion) motions.get(i);
			for (int j = i + 1; j < motions.size(); j++) {
				final Motion two = (Motion) motions.get(j);
				final Vector3d vec = one.findIntersection(two);
				if (vec != null) ret.add(new Collision(one.getAircraft(), two.getAircraft(), vec));
			}
		}
		return ret;
	}

	public void dumpFrame( String debugPrefix ) {

		String prefix = debugPrefix + frameno + " ";
		int offset = 0;
		for (int i=0;i<currentFrame.planeCnt;i++) {

			int cslen = currentFrame.lengths[i];
			System.out.println(prefix+new String( currentFrame.callsigns, offset, cslen )+" "+
					currentFrame.positions[3*i]+" "+
					currentFrame.positions[3*i+1]+" "+
					currentFrame.positions[3*i+2]+" ");
			offset += cslen;
		}        
	}

	int frameno=-1; // just for debug
	public void setFrame(final RawFrame f) {

		if (Constants.DEBUG_DETECTOR || Constants.DUMP_RECEIVED_FRAMES || Constants.SYNCHRONOUS_DETECTOR) {
			frameno++;    
		}
		currentFrame = f;
		if (Constants.DUMP_RECEIVED_FRAMES) {
			dumpFrame( "CD-R-FRAME: ");
		}


	}

	
	
	
	/**
	 * 
	 * This method computes the motions and current positions of the aircraft
	 * 
	 * Afterwards, it stores the positions of the aircrafts into the StateTable in the persistentScope
	 * 
	 * @return
	 */
	public List createMotions() {

		final List ret = new LinkedList();
		final HashSet poked = new HashSet();

		Aircraft craft;
		Vector3d new_pos;

		for (int i = 0, pos = 0; i < currentFrame.planeCnt; i++) {

			final float x = currentFrame.positions[3*i], y = currentFrame.positions[3*i + 1], z = currentFrame.positions[3*i + 2];
			final byte[] cs = new byte[currentFrame.lengths[i]];
			for (int j = 0; j < cs.length; j++)
				cs[j] = currentFrame.callsigns[pos + j];
			pos += cs.length;
			craft = new Aircraft(cs);
			new_pos = new Vector3d(x, y, z);

			poked.add(craft);
			
			// get the last known position of this aircraft
			final Vector3d old_pos = state.get(mkCallsignInPersistentScope(craft.getCallsign()));

			if (old_pos == null) {
				// Ales : we have detected a new aircraft
				
				//here, we create a new callsign and store the aircraft into the state table.
				state.put(mkCallsignInPersistentScope(craft.getCallsign()), new_pos.x, new_pos.y, new_pos.z);

				final Motion m = new Motion(craft, new_pos);
				if (Constants.DEBUG_DETECTOR || Constants.SYNCHRONOUS_DETECTOR) {
					System.out.println("createMotions: old position is null, adding motion: " + m);
				}

				ret.add(m);
			} else {
				// this is already detected aircraft, we we need to update its position
				
				final Vector3d save_old_position = new Vector3d(old_pos.x, old_pos.y, old_pos.z);
				
				//updating position in the StateTable
				old_pos.set(new_pos.x, new_pos.y, new_pos.z);

				final Motion m = new Motion(craft, save_old_position, new_pos);
				if (Constants.DEBUG_DETECTOR || Constants.SYNCHRONOUS_DETECTOR) {
					System.out.println("createMotions: adding motion: " + m);
				}                

				ret.add(m);
			}
		}
		return ret;
	}

	
	
	/**
	 * 
	 * This Runnable enters the StateTable in order to allocate the callsign in the PersistentScope
	 * 
	 * 
	 * @author Ales
	 *
	 */
	static class R implements Runnable {
		CallSign c;
		byte[] cs;

		public void run() {
			final byte[] b = new byte[cs.length];
			for (int i = 0; i < b.length; i++)
				b[i] = cs[i];
			c = new CallSign(b);
		}
	}
	private final R r = new R();

	CallSign mkCallsignInPersistentScope(final byte[] cs) {
		r.cs = cs;
		//MemoryArea.getMemoryArea(state).executeInArea(r);
		r.run();
		return r.c;
	}


}