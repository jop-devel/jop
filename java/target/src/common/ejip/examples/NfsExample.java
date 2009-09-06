package ejip.examples;

import ejip.Ejip;
import ejip.nfs.Callbackable;
import ejip.nfs.NfsClient;
import ejip.nfs.NfsConst;
import ejip.nfs.datastructs.*;


public class NfsExample {
	public static final int ACTION_QUERY_PORT_MAPPER_FOR_MOUNT = 6; 
	public static final int ACTION_QUERY_PORT_MAPPER_FOR_NFS = 7;
	public static final int ACTION_QUERY_PORT_MAPPER_FOR_NLM = 8;

	public static final int ACTION_CALL_NFS_NULL = 11;
	public static final int ACTION_CALL_NFS_LOOKUP = 12;
	
	public static final int MAX_EXPORTS = 4;
	public static final int MAX_GROUPS = 4;
	public static final int MAX_MOUNTS = 4;
	public static final int MAX_FLAVORS = 4;

	int destIP;
	int ownIP;
	int destPort;
	int mountPort;
	int nfsPort;
	
	static StringBuffer hostname = new StringBuffer("jopClient");
	StringBuffer messageBuffer = new StringBuffer();
	
	static NfsClient nc;
	static NfsExample nex;
	
	/**
	 * List of Directories
	 */
	static StringBuffer[] exportsList = new StringBuffer[] {new StringBuffer(), new StringBuffer(), new StringBuffer(), new StringBuffer()};
	/**
	 * groups of corresponding export
	 */
	static StringBuffer[][] groups = new StringBuffer[][] {{new StringBuffer(), new StringBuffer(), new StringBuffer(), new StringBuffer()},
							{new StringBuffer(), new StringBuffer(), new StringBuffer(), new StringBuffer()},
							{new StringBuffer(), new StringBuffer(), new StringBuffer(), new StringBuffer()},
							{new StringBuffer(), new StringBuffer(), new StringBuffer(), new StringBuffer()}};
	/**
	 * List of Mounts
	 * index 0: hostname
	 * index 1: directory
	 */
	static StringBuffer[][] mountList = new StringBuffer[][] 	{{new StringBuffer(), new StringBuffer()}, {new StringBuffer(), new StringBuffer()},
																{new StringBuffer(), new StringBuffer()}, {new StringBuffer(), new StringBuffer()}
	};
	public static StringBuffer mountPointHandle = new StringBuffer();
	public static int[] flavors = new int[MAX_FLAVORS];

	public static GetAttr3Res getAttrRes = new GetAttr3Res(); 
	public static SetAttr3Res setAttrRes = new SetAttr3Res(); 
	public static Lookup3Res lookupRes = new Lookup3Res();
	public static CreateRes createRes = new CreateRes();
	public static Write3Res writeRes = new Write3Res();
	
	public static Diropargs3 what = new Diropargs3();
//	public static StringBuffer fHandle = new StringBuffer();
//	public static int[] flavors = new int[MAX_FLAVORS];
//	public static Fattr3 fattr = new Fattr3();
	public static Sattr3 sattr = new Sattr3();
	public static Nfstime3 guard = new Nfstime3();
//	public static WccData wccData = new WccData(); 
//	public static StringBuffer object = new StringBuffer();
	public static Sattr3 setObjAttributes = new Sattr3();
//	public static Fattr3 dir_attributes = new Fattr3();
	public static Diropargs3 where = new Diropargs3();
//	public static Sattr3 obj_attributes2 = new Sattr3();
	public static StringBuffer createverf = new StringBuffer(NfsConst.NFS3_CREATEVERFSIZE);
//	public static StringBuffer createObjHandle = new StringBuffer();
//	public static Fattr3 createObjAttributes = new Fattr3(); 
//	public static StringBuffer mkdirObjHandle = new StringBuffer();
//	public static Fattr3 mkdirObjAttributes = new Fattr3(); 
//	public static int access, accessAccess;
//	public static Fattr3 accessAttributes = new Fattr3();
//	public static Fattr3 readAttributes = new Fattr3();
//	public static int count, stableHow;
//	public static boolean eof;
//	public static StringBuffer data = new StringBuffer();
	public static Remove3Res removeRes = new Remove3Res();
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		nex = new NfsExample();
		StateHandler sh = new StateHandler();
		nc = new NfsClient(Ejip.makeIp(192, 168, 1, 2), Ejip.makeIp(192, 168, 1, 1), hostname, sh);
		nc.destPort = 111; //portmapper
		nc.mount.getMountPort();
	}
	
	static class StateHandler implements Callbackable {
		int nextState = 0;
		int i = 0;
		StringBuffer mountDir = new StringBuffer("/pub/");
		public void callback(ResultType message) {

			switch (nextState) {
			case 0:
				System.out.println("mount Port: "+nc.mount.mountPort);
				nextState = 1;
				nc.getNfsPort();
				break;
			case 1:
				nextState = 2;
//				nc.mount.getExports(exportsList, groups);
//				nc.mount.unmountAll();
				System.out.println("MOUNTING: " + mountDir);
				nc.mount.mount(mountDir	, mountPointHandle, flavors); //fHandle takes result
				break;
			case 2:
				nextState = 3;
				System.out.println("TESTING: LOOKUP");
				what.setDir(mountPointHandle);
				what.setName(new StringBuffer("test"));
				nc.lookup(what, lookupRes);
				break;
			case 3:
				nextState = 4;
//				System.out.println(lookupRes.toString());
				System.out.println("SETTING ATTRIBUTES");
				guard = lookupRes.getObjAttributes().getAttributes().getCtime();
				sattr.setMode(0x1b6); // equals chmod 666
				nc.setAttr(lookupRes.getObject(), sattr, guard, setAttrRes);
				break;
			case 4:
				nextState = 5;
				where.setDir(mountPointHandle);
				where.setName(new StringBuffer("jopMadeIt"));
//				System.out.println(setAttrRes.toString());
				System.out.println("LOOKING UP FILE");
				nc.lookup(where,lookupRes);
				break;
			case 5:
				nextState = 6;
				System.out.println("lookup result:");
				if (lookupRes.getError() == NfsConst.NFS3_OK) {
					System.out.println("DELETING FILE");
					nc.remove(where, removeRes);
					break;
				} else {
					System.out.println("Error: " + lookupRes.getErrorDescription());
					//else fall through
				}
			case 6:
				nextState = 7;
				System.out.println("CREATING FILE");
				nc.create(where, 0, setObjAttributes, createverf, createRes);
//				System.out.println("MAKING NEW DIRECTORY");
//				nc.mkDir(where, setObjAttributes, createRes);
				break;
			case 7:
				nextState = 8;
				nc.write(lookupRes.getObject(), 10, 100, 0, new StringBuffer("jop wrote this"), writeRes);
//			case 7:
//				nextState = 8;
//				dumpFattr(createObjAttributes);
//				dumpFattr(wccData.after);
//				System.out.println("CHECKING ACCESS");
//				nc.access(createObjHandle, access, accessAttributes, accessAccess);
//				break;
//			case 8:
//				nextState = 9;
//				dumpFattr(accessAttributes);
//				System.out.println(accessAccess);
//				System.out.println("READ");
//				data.append(" appended data");
//				nc.write(object, 0, 100, stableHow, data);
//				break;
//			case 9:
//				break;
			}
		}
		
		public void dumpExports() {
			i = 0;
			System.out.println("exports:");
			System.out.println(exportsList.length);
			while (i < exportsList.length) {
				if (exportsList[i] != null) {
					System.out.println("export " + i + ": " + exportsList[i] );
				}
				i++;
			}
		}
		
		public void dumpMountList() {
			
			for (int i = 0; i < mountList.length; i++) {
				if (mountList[i][0] != null) {
					System.out.println("mount "+i+": <"+mountList[i][0]+"> "+mountList[i][1]);
				}
			}
			
			if (mountList[0][0] == null) {
				System.out.println("No Mounts on Nfs Server!");
			}
		}


	}
	

}
