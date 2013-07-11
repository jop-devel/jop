/**
 *  This file is part of miniCDx benchmark for oSCJ.
 *
 *   miniCDx is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   miniCDx is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with miniCDx.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *   Copyright 2009, 2010 
 *   @authors  Daniel Tang, Ales Plsek
 *
 *   See: http://sss.cs.purdue.edu/projects/oscj/
 */
 
 package cdx.cdx;

import cdx.utils.javacp.util.HashMap;
import cdx.utils.javacp.util.HashSet;
import cdx.utils.javacp.util.Iterator;
import cdx.utils.javacp.util.LinkedList;
import cdx.utils.javacp.util.List;
import javax.safetycritical.ManagedMemory;

import cdx.collision.Vector3d;

/**
 * The constructor runs and the instance lives in the persistent detector scope.
 * The state table passed to it lives in the persistent detector scope. The
 * thread runs in transient detector scope. The frame (currentFrame) lives in
 * immortal memory. The real collision detection starts here.
 */
/* @javax.safetycritical.annotate.Scope("cdx.Level0Safelet") */
/* @javax.safetycritical.annotate.RunsIn("cdx.CollisionDetectorHandler") */
public class TransientDetectorScopeEntry implements Runnable {
	
//    private StateTable state;
	public StateTable state;
//    private float voxelSize;
	public float voxelSize;
    public RawFrame currentFrame;

    /*
     * public TransientDetectorScopeEntry(final StateTable s, final float
     * voxelSize) { state = s; this.voxelSize = voxelSize; }
     */

    public TransientDetectorScopeEntry(StateTable s, float voxelSize) {
        state = s;
        this.voxelSize = voxelSize;
    }

    public void run() {
    	
        Benchmarker.set(1);
        
        Benchmarker.set(Benchmarker.RAPITA_REDUCER_INIT);
        
        final Reducer reducer = new Reducer(voxelSize);
        Benchmarker.done(Benchmarker.RAPITA_REDUCER_INIT);
        
        Benchmarker.set(Benchmarker.LOOK_FOR_COLLISIONS);
        
        int numberOfCollisions = lookForCollisions(reducer, createMotions());
        Benchmarker.done(Benchmarker.LOOK_FOR_COLLISIONS);

        if (cdx.cdx.ImmortalEntry.recordedRuns < cdx.cdx.ImmortalEntry.maxDetectorRuns) {
            cdx.cdx.ImmortalEntry.detectedCollisions[cdx.cdx.ImmortalEntry.recordedRuns] = numberOfCollisions;
        }

        if (Constants.SYNCHRONOUS_DETECTOR || Constants.DEBUG_DETECTOR) {
            System.out.println("CD detected  " + numberOfCollisions
                    + " collisions.");
            int colIndex = 0;

            /*
             * for(final Iterator iter = collisions.iterator(); iter.hasNext();)
             * { Collision col = (Collision) iter.next(); // fixme update /*
             * List aircraft = col.getAircraftInvolved();
             * System.out.println("CD collision "
             * +colIndex+" occured at location "+col.getLocation() +
             * " with "+aircraft.size()+" involved aircraft.");
             * 
             * for(final Iterator aiter = aircraft.iterator(); aiter.hasNext();)
             * { Aircraft a = (Aircraft) aiter.next();
             * 
             * 
             * 
             * 
             * 
             * System.out.println("CD collision "+colIndex+" includes aircraft "+
             * a); }
             */
            /*
             * colIndex++; }
             */

            System.out.println("");
        }
        
        Benchmarker.done(1);
    }

    public int lookForCollisions(final Reducer reducer, final List motions) {
        Benchmarker.set(2);

        final List check = reduceCollisionSet(reducer, motions);
        // final CollisionCollector c = new CollisionCollector();

        int suspectedSize = check.size();
        if (cdx.cdx.ImmortalEntry.recordedRuns < cdx.cdx.ImmortalEntry.maxDetectorRuns) {
            cdx.cdx.ImmortalEntry.suspectedCollisions[cdx.cdx.ImmortalEntry.recordedRuns] = suspectedSize;
        }
        
        int c = 0;
        final List ret = new LinkedList();
        
        for (final Iterator iter = check.iterator(); iter.hasNext();){
        	
        	c += determineCollisions((List) iter.next(), ret);
        	
        }
            
        Benchmarker.done(2);
//        System.out.println("Good2");
        return c; // .getCollisions();
    }

    /**
     * Takes a List of Motions and returns an List of Lists of Motions, where
     * the inner lists implement RandomAccess. Each Vector of Motions that is
     * returned represents a set of Motions that might have collisions.
     */
    public List reduceCollisionSet(final Reducer it, final List motions) {
        Benchmarker.set(3);
        final HashMap voxel_map = new HashMap();
        final HashMap graph_colors = new HashMap();

        for (final Iterator iter = motions.iterator(); iter.hasNext();)
            it.performVoxelHashing((Motion) iter.next(), voxel_map,
                    graph_colors);

        final List ret = new LinkedList();
        for (final Iterator iter = voxel_map.values().iterator(); iter
                .hasNext();) {
            final List cur_set = (List) iter.next();
            if (cur_set.size() > 1)
                ret.add(cur_set);
        }
        Benchmarker.done(3);
        return ret;
    }

    public boolean checkForDuplicates(final List collisions, Motion one,
            Motion two) {
        // (Peta) I have also changed the comparison employed in this method as
        // it is another major source of overhead
        // Java was checking all the callsign elements, while C just checked the
        // callsign array addresses
        byte c1 = one.getAircraft().getCallsign()[5];
        byte c2 = two.getAircraft().getCallsign()[5];
        for (final Iterator iter = collisions.iterator(); iter.hasNext();) {
            Collision c = (Collision) iter.next();
            if ((c.first().getCallsign()[5] == c1)
                    && (c.second().getCallsign()[5] == c2)) {
                // Benchmarker.done(4);
                return false;
            }
        }
        return true;
        /*
         * old code: Benchmarker.set(4); for (int i=0;i<collisions.size();i++) {
         * Collision c=(Collision)collisions.get(i); if
         * ((c.first().equals(one.getAircraft())) &&
         * (c.second().equals(two.getAircraft()))) { Benchmarker.done(4); return
         * false; } } Benchmarker.done(4); return true;
         */
    }

    public int determineCollisions(final List motions, List ret) {
    	
        // (Peta) changed to iterators so that it's not killing the algorithm
        Benchmarker.set(5);
        int _ret = 0;
        
//        System.out.println("Good");
        Object[] objs = motions.toArray(new Motion[motions.size()]);
//        System.out.println("GoodX");
        
        Motion[] _motions = new Motion[objs.length];
        System.arraycopy(objs, 0, _motions, 0, objs.length);
        
//        Motion[] _motions = (Motion[]) motions.toArray(new Motion[motions.size()]);
        
        // Motion[] _motions= (Motion)motions.toArray();
        for (int i = 0; i < _motions.length - 1; i++) {
            final Motion one = _motions[i]; // m2==two, m=one
            for (int j = i + 1; j < _motions.length; j++) {
                final Motion two = _motions[j];
                // if (checkForDuplicates(ret, one, two)) { // This is removed
                // because it is very very slow...
                final Vector3d vec = one.findIntersection(two);
                if (vec != null) {
                    ret.add(new Collision(one.getAircraft(), two.getAircraft(),
                            vec));
                    _ret++;
                }
                // }
            }
        }
        Benchmarker.done(5);
        return _ret;
        /*
         * Old code: Benchmarker.set(5); int _ret=0; for (int i = 0; i <
         * motions.size() - 1; i++) { final Motion one = (Motion)
         * motions.get(i); //m2==two, m=one for (int j = i + 1; j <
         * motions.size(); j++) { final Motion two = (Motion) motions.get(j); if
         * (checkForDuplicates(ret, one, two)) { final Vector3d vec =
         * one.findIntersection(two); if (vec != null) { ret.add(new
         * Collision(one.getAircraft(), two.getAircraft(), vec)); _ret++; }
         * 
         * } } } Benchmarker.done(5); return _ret; //
         */
    }

    public void dumpFrame(String debugPrefix) {

        String prefix = debugPrefix + frameno + " ";
        int offset = 0;
        for (int i = 0; i < currentFrame.planeCnt; i++) {

            int cslen = currentFrame.lengths[i];
            System.out.println(prefix
                    + new String(currentFrame.callsigns, offset, cslen) + " "
                    + currentFrame.positions[3 * i] + " "
                    + currentFrame.positions[3 * i + 1] + " "
                    + currentFrame.positions[3 * i + 2] + " ");
            offset += cslen;
        }
    }

    int frameno = -1; // just for debug

    public void setFrame(final RawFrame f) {
        if (Constants.DEBUG_DETECTOR || Constants.DUMP_RECEIVED_FRAMES
                || Constants.SYNCHRONOUS_DETECTOR) {
            frameno++;
        }
        currentFrame = f;
        if (Constants.DUMP_RECEIVED_FRAMES) {
            dumpFrame("CD-R-FRAME: ");
        }

    }

    /**
     * This method computes the motions and current positions of the aircraft
     * Afterwards, it stores the positions of the aircrafts into the StateTable
     * in the persistentScope
     * 
     * @return
     */
    public List createMotions() {
    	
        Benchmarker.set(6);

        final List ret = new LinkedList();
        final HashSet poked = new HashSet();
        
        Aircraft craft;
        Vector3d new_pos;

        for (int i = 0, pos = 0; i < currentFrame.planeCnt; i++) {
        	
            final float x = currentFrame.positions[3 * i], y = currentFrame.positions[3 * i + 1], z = currentFrame.positions[3 * i + 2];
            final byte[] cs = new byte[currentFrame.lengths[i]];
            for (int j = 0; j < cs.length; j++)
                cs[j] = currentFrame.callsigns[pos + j];
            pos += cs.length;
            craft = new Aircraft(cs);
            new_pos = new Vector3d(x, y, z);
            
            poked.add(craft);
            
            // get the last known position of this aircraft
            final cdx.statetable.Vector3d old_pos = state.get(new CallSign(craft.getCallsign()));
            

            if (old_pos == null) {
                // we have detected a new aircraft

                // here, we create a new callsign and store the aircraft into
                // the state table.
            	
            	// FIXME: There are illegal static references in the following line because
            	// it performs a change in memory area. Currently JOP has this issue. 
            	state.put(mkCallsignInPersistentScope(craft.getCallsign()) , new_pos.x, new_pos.y, new_pos.z);
                
            	final Motion m = new Motion(craft, new_pos);
                if (cdx.cdx.Constants.DEBUG_DETECTOR
                        || cdx.cdx.Constants.SYNCHRONOUS_DETECTOR) {
                    System.out
                            .println("createMotions: old position is null, adding motion: "
                                    + m);
                }
                
                ret.add(m);
                
            } else {
                // this is already detected aircraft, we we need to update its
                // position
                final Vector3d save_old_position = new Vector3d(old_pos.x,
                        old_pos.y, old_pos.z);
                // updating position in the StateTable
                old_pos.set(new_pos.x, new_pos.y, new_pos.z);

                final Motion m = new Motion(craft, save_old_position, new_pos);
                if (cdx.cdx.Constants.DEBUG_DETECTOR
                        || cdx.cdx.Constants.SYNCHRONOUS_DETECTOR) {
                    System.out.println("createMotions: adding motion: " + m);
                }

                ret.add(m);
            }
        	
            
            
        }
        Benchmarker.done(6);
       
        return ret;
    }

    /**
     * This Runnable enters the StateTable in order to allocate the callsign in
     * the PersistentScope
     */
    /* @javax.safetycritical.annotate.RunsIn("cdx.Level0Safelet") */
    static class R implements Runnable {
        CallSign c;
        byte[] cs;

        public void run() {
            c = new CallSign(cs);
        }
    }
    
    static class H implements Runnable {
    	
    	byte[] cs_ret;
    	byte[] cs_in;
    	
		@Override
		public void run() {
			
			cs_ret = new byte[cs_in.length];
			
		}
    	
    	
    }

    private final R r = new R();
    private final H h = new H();

    CallSign mkCallsignInPersistentScope(final byte[] cs) {
//        try {
    		h.cs_in = cs;
        	ManagedMemory.executeInAreaOf(r, h);
        	
        	r.cs = h.cs_ret;
//          r.cs = (byte[]) MemoryArea.newArrayInArea(r, byte.class, cs.length);
            
            
            
//        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
        	
        for (int i = 0; i < cs.length; i++){
            r.cs[i] = cs[i];
        }
//        MemoryArea.getMemoryArea(state).executeInArea(r);
        ManagedMemory.executeInAreaOf(state, r);
        return r.c;
    }

}