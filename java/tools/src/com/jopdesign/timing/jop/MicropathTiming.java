/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2009, Benedikt Huber (benedikt.huber@gmail.com)

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.jopdesign.timing.jop;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import com.jopdesign.timing.jop.MicrocodeAnalysis.MicrocodeVerificationException;
import com.jopdesign.timing.jop.MicrocodePath.PathEntry;
import com.jopdesign.tools.Instruction;

/** Timing information for one microcode path of the form<br/>
 *  {@code sum(exprs) + max(0, b - hidden) } <br/>
 *
 *  Modelsim reports (25.11.09, r=1, w=2):
 *  stgs: stgs | nop | wait (3) = wait(2+r)  , mit read@wait[0]
 *  stps: stps | nop | wait (4) = wait(2+w)  , mit write@wait[0]
 *  stgf: stgf | nop | wait (7) = wait(5+ 2r), mit read@wait[0] und read@wait[4]
 *  stpf: stpf | nop | wait (8) = wait(5+r+w), mit read@wait[0] und write@wait[4]
 */
public class MicropathTiming {
    /** Timing expression: {@code Math.max(0, C + x r + y w - hidden)} */
    public static class TimingExpression {
        private int constCycles;
        private int reads;
        private int writes;
        private int hidden;
        public static TimingExpression constTime(int constCycles) {
            return new TimingExpression(constCycles,0,0);
        }
        public static TimingExpression read(int hidden)
            throws MicrocodeVerificationException {
            return new TimingExpression(0,1,0,hidden);
        }
        public static TimingExpression write(int hidden)
            throws MicrocodeVerificationException {
            return new TimingExpression(0,0,1,hidden);
        }
        public TimingExpression(int constCycles, int reads, int writes) {
            this.constCycles = constCycles;
            this.reads = reads;
            this.writes = writes;
            this.hidden = 0;
        }
        public TimingExpression(int constCycles, int reads, int writes, int hidden)
            throws MicrocodeVerificationException {
            this(constCycles,reads,writes);
            if(hidden < 0) {
                throw new MicrocodeVerificationException("Too few wait states ?");
            }
            this.hidden = hidden;
        }
        public int eval(int r, int w) {
            int cycles = constCycles + reads * r + writes * w - hidden;
            return (cycles > 0 ? cycles : 0);
        }
        public String toString() {
            Vector<String> sb = new Vector<String>();
            // System.out.println(String.format("consts: %d, r: %d, w: %d, hidden: %d",constCycles,reads,writes,hidden));
            int consts = this.constCycles - hidden;
            if(reads > 0) sb.add(product(reads,"r"));
            if(writes > 0) sb.add(product(writes,"w"));
            if(sb.isEmpty())
            {
            	if(consts <= 0) throw new AssertionError("0 cost instruction");
            	return ""+consts;
            }
            StringBuffer s = MicropathTiming.concat(" + ",sb);
            if(consts < 0) {
                String expr = String.format("[%s - %d]",s,-consts);
                return expr;
            } else if(consts > 0) {
                return String.format("%d + %s",consts,s);
            } else {
                return s.toString();
            }
        }
        private static String product(int count, String name) {
            if(count == 0) return "";
            else if(count == 1) return name;
            else if(count == -1) return "-"+name;
            else return count + " " + name;
        }
    }

    private List<TimingExpression> timing = new Vector<TimingExpression>();
    private int bcAccessHidden = -1;

    /** Calculate timing info for the given path */
    public MicropathTiming(MicrocodePath p) throws MicrocodeVerificationException {
        computeTiming(p);
        compress();
    }

    public boolean hasBytecodeLoad() {
        return this.bcAccessHidden > 0;
    }
    public long getHiddenBytecodeLoadCycles() {
        return this.bcAccessHidden;
    }

    private void computeTiming(MicrocodePath p) throws MicrocodeVerificationException {
        int start = -1;
        int constantCycles = 0;
        boolean wasWait = false;
        int accessKind = -1;
        Integer accessAddress = null;
        for(PathEntry l : p.getPath()) {
            Instruction i = l.getInstruction();
            switch(i.opcode) {
            case MicrocodeConstants.STMWA:
            accessAddress = l.getTOS(); /* Deal with OP addresses here */
            break;
            case MicrocodeConstants.STMRA:
            case MicrocodeConstants.STMRAC:
            case MicrocodeConstants.STMRAF:
            accessAddress = l.getTOS();
            /* fallthrough */
            case MicrocodeConstants.STMWD:
            case MicrocodeConstants.STMWDF:
            case MicrocodeConstants.STBCRD:
            case MicrocodeConstants.STGF:
            case MicrocodeConstants.STPF:
            case MicrocodeConstants.STALD:
            case MicrocodeConstants.STCP:
            case MicrocodeConstants.STAST:
            case MicrocodeConstants.STGS:
            case MicrocodeConstants.STPS:
                if(accessKind >= 0) {
                    throw new MicrocodeVerificationException("No wait after memory access (unsafe) !");
                }
                start = constantCycles;
                accessKind = i.opcode;
                wasWait = false;
                break;
            case MicrocodeConstants.WAIT:
                if(wasWait && accessKind > 0) { // unhandled wait
                    int passed = constantCycles - start;
                    /* We currently have some possibly redundant wait s in null_pointer: */
//					if(accessKind < 0) {
//						throw new MicrocodeVerificationException("Wait without memory access (or 3 waits in a row) ?: "+p.getPath());
//					}
                    switch(accessKind) {
                    case MicrocodeConstants.STMRA:
                        if(accessAddress != null) {
                            throw new MicrocodeVerificationException("Timing for IO access not yet implemented[r]: "+accessAddress);
                        }
                        timing.add(TimingExpression.read(passed - 2));
                        break;
                    case MicrocodeConstants.STMWD:
                        if(accessAddress != null && accessAddress < 0) {
                            // no wait states
                        } else {
                            timing.add(TimingExpression.write(passed - 2));
                        }
                        break;
                    case MicrocodeConstants.STBCRD:
                        if(bcAccessHidden >= 0) {
                            throw new MicrocodeVerificationException("More than one bytecode read in a microcode sequence");
                        }
                        bcAccessHidden = passed - 2;
                        break;
                    case MicrocodeConstants.STGS:
                        /* wait(2+r) */
                        timing.add(new TimingExpression(2,1,0,passed-1));
                        break;
                    case MicrocodeConstants.STPS:
                        /* wait(2+w) */
                        timing.add(new TimingExpression(2,0,1,passed-1));
                        break;
                    case MicrocodeConstants.STMRAC:
                        timing.add(TimingExpression.read(passed - 2));
                        break;
                    case MicrocodeConstants.STMRAF:
                        timing.add(TimingExpression.read(passed - 2));
                        break;
                    case MicrocodeConstants.STMWDF:
                        /* wait(2+w) */
                        timing.add(new TimingExpression(2,0,1,passed-1));
                        break;
                    case MicrocodeConstants.STGF:
                        /* wait(5+2r) */
                    	// TODO: MS don't know if this is ok after
                    	// changes by WP and changes back by MS?
                        timing.add(new TimingExpression(5,2,0,passed-1));
                        break;
                    case MicrocodeConstants.STPF:
                        /* wait (6+r+w) */
                        timing.add(new TimingExpression(6,1,1,passed-1));
                        break;
                    case MicrocodeConstants.STALD:
                        /* wait (TODO) */
                        timing.add(new TimingExpression(1,3,0,passed - 3));
                        break;
                    case MicrocodeConstants.STAST:
                        /* wait (TODO) */
                        timing.add(new TimingExpression(4,2,1,passed - 4));
                        break;
                    case MicrocodeConstants.STCP:
                        throw new MicrocodeVerificationException("stcp not yet supported: "+accessKind);
                    default:
                        throw new MicrocodeVerificationException("Unsupport kind of memory access: "+accessKind);
                    }
                    accessKind = start = -1;
                } else {
                    wasWait = true;
                }
                break;
            default: break;
            }
            constantCycles++;
        }
        timing.add(TimingExpression.constTime(constantCycles));
    }

    /* compact all timing expressions without hidden cycles */
    private void compress() {
        int constCycles = 0, reads = 0, writes = 0;
        ArrayList<TimingExpression> timingNew = new ArrayList<TimingExpression>();
        for(TimingExpression te : this.timing) {
            if(te.hidden == 0) {
                constCycles += te.constCycles;
                reads       += te.reads;
                writes      += te.writes;
            } else {
                timingNew.add(te);
            }
        }
        if(writes > 0 || reads > 0 | constCycles > 0) {
            timingNew.add(0,new TimingExpression(constCycles,reads,writes));
        }
        timing = timingNew;
    }
    public int getCycles(int readDelay, int writeDelay, long bytecodeDelay) {
        int r = 0;
        for(TimingExpression te : timing) {
            r += te.eval(readDelay, writeDelay);
        }
        if(bcAccessHidden > 0) r += Math.max(0, bytecodeDelay - bcAccessHidden);
        return r;
    }

    /* Abbrev. form: [expr] denotes max(0,expr) */
    public String toString() {
        Vector<String> timingExprs = new Vector<String>();
        // FIXME: Fold constant factors
        for(TimingExpression x : timing) {
        	timingExprs.add(x.toString());
        }
        StringBuffer s = concat(" + ",timingExprs);
        if(this.hasBytecodeLoad()) {
            s.append(" + [b - "+bcAccessHidden+"]");
        }
        return s.toString();
    }

    /** String representation of several path timing informations.<br/>
     *  {@code [expr]} denotes {@code max(0,expr)}
     * @param timings
     * @return
     */
    public static String toString(Vector<MicropathTiming> timings) {
        Vector<String> strs = new Vector<String>();
        for(MicropathTiming timing : timings) {
            strs.add(timing.toString());
        }
        return concat(" <|> ",strs).toString();
    }

    /* helper, FIXME: move to util lib */
    static StringBuffer concat(String sep, List<? extends Object> list) {
        boolean first = true;
        StringBuffer buf = new StringBuffer();
        for(Object s : list) {
            String str = s.toString();
            if(str.length() == 0) continue;
            if(first) { buf.append(str); first = false; }
            else      { buf.append(sep); buf.append(str); }
        }
        return buf;
    }

}
