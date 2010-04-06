/*
 * Copyright (c) Daniel Reichhard, daniel.reichhard@gmail.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *	This product includes software developed by Daniel Reichhard
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 */
package ejip.nfs.datastructs;

import ejip.nfs.NfsConst;
import ejip.nfs.Xdr;

public class Exports extends ResultType {
	private Export[] exports;
	
	public Exports(int maxExports, int maxGroups) {
		exports = new Export[maxExports];
		for (int i = 0; i < maxExports-1; i++) {
			exports[i] = new Export(maxGroups);
		}
	}
	
	public boolean loadFields(StringBuffer sb) {
		int i = 0;

		while (Xdr.getNextInt(sb) == NfsConst.TRUE) {
			if (i < exports.length) {
				exports[i].loadFields(sb);
				i++;
			} else { //consume only, dont store
				Xdr.getNextStringBuffer(sb, null);
				while (Xdr.getNextInt(sb) == NfsConst.TRUE) {
					Xdr.getNextStringBuffer(sb, null);
				}
			}
		}
		return true;
	}
	
	public Export[] getExports() {
		return exports;
	}
	
	public class Export {
		private StringBuffer exDir = new StringBuffer();
		private Group[] exGroups;
		
		public Export(int maxGroups) {
			exGroups = new Group[maxGroups];
			for (int i = 0; i < maxGroups; i++) {
				exGroups[i] = new Group();
			}
		}
		
		public StringBuffer getExDir() {
			return exDir;
		}
		
		public Group[] getExGroups() {
			return exGroups;
		}

		public boolean loadFields(StringBuffer sb) {
			int i = 0;
			Xdr.getNextStringBuffer(sb, exDir);
			while (Xdr.getNextInt(sb) == NfsConst.TRUE) {
				if (i < exGroups.length) {
					exGroups[i].loadFields(sb);
					i++;
				} else { //consume only, dont store
					Xdr.getNextStringBuffer(sb, null);
				}
			}
			return true;
		}
	}
	
	
	private class Group {
		StringBuffer grName = new StringBuffer();
		
		public StringBuffer getGrName() {
			return grName;
		}
		
		public boolean loadFields(StringBuffer sb) {
			Xdr.getNextStringBuffer(sb, grName);
			return true;
		}
	}
}
