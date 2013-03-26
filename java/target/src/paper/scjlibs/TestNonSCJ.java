package scjlibs;

import scjlibs.util.HashMap;

public class TestNonSCJ {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		System.out.println("Hello");
		
		GenericEntry[] table = new GenericEntry[10];
		GenericEntry[] freeEntry = new GenericEntry[10];
		
		for(int i = 0; i<freeEntry.length;i++){
			freeEntry[i] = new GenericEntry("GE"+i);
		}
		
		GenericEntry e = table[5];
		
		table[5] = freeEntry[5];
		
		if(e == null){
			System.out.println("Still null");
		}
		
		
//		HashMap<GenericEntry, GenericEntry> hm = new HashMap<GenericEntry, GenericEntry>(10);
//		
//		GenericEntry[] geKey = new GenericEntry[hm.capacity()];
//		GenericEntry[] geValue = new GenericEntry[hm.capacity()];
//		
//		for(int i = 0; i < hm.capacity(); i++){
//			geKey[i] = new GenericEntry("Juan"+i);
//			geValue[i] = new GenericEntry("Ricardo"+i);
//		}
//		
//		System.out.println(hm.capacity());
//		
//		for(int i = 0; i < hm.capacity(); i++){
//			hm.put(geKey[i], geValue[i]);
//			geKey[i].initialize();
//			geValue[i].initialize();
//			System.out.println(hm.size());
//		}
//		
//		
//		for(int i = 0; i < hm.capacity(); i++){
//			System.out.print(geKey[i].isFree()+":");
//			System.out.println(geValue[i].isFree());
//		}
//		
//		System.out.println("-----------------------------");
//		
//		System.out.println("to remove: "+geKey[1]);
//		hm.remove(geKey[1]).finalize();
//		
//		System.out.println(hm.get(geKey[7]).getName());
//		
//		GenericEntry extraValue = new GenericEntry("New guy");
//		GenericEntry ow = hm.put(geKey[7], extraValue);  
//		
//		System.out.println(hm.get(geKey[7]).getName());
//		
//		if(ow != null)
//			ow.finalize();
//		
//		System.out.println(hm.containsKey(geKey[7]));
//		System.out.println(hm.containsKey(geKey[1]));
//		
//		System.out.println(hm.containsValue(geValue[8]));
//		System.out.println(hm.containsValue(geValue[1]));
//		
//		for(int i = 0; i < hm.capacity(); i++){
//			System.out.print(geKey[i].isFree()+":");
//			System.out.println(geValue[i].isFree());
//		}
//		
//		hm.clear();
		
//		System.out.println(hm.get("Juan0").getName());
//		
//		hm.remove("Juan0");
//		System.out.println(hm.size());
//		
//		hm.put("NEW", new GenericEntry("New Entry"));
		
//		System.out.println(hm.get("NEW").getName());
		
	}	

}
