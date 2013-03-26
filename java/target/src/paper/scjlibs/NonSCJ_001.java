package scjlibs;

import scjlibs.util.Vector;

public class NonSCJ_001 {
	
	Vector<VectorElement> vector;
	
	
	public NonSCJ_001() {
		
		this.vector = new Vector<VectorElement>();
		
	}
	
	public static void main(String[] args){
		
		NonSCJ_001 app = new NonSCJ_001();
		
		for(int i = 0; i < app.vector.capacity(); i++){
			app.vector.add(new VectorElement());
		}
		
		app.vector.add(new VectorElement());
		
		System.out.println(app.vector.size());
		
	}
	
	
	static class VectorElement {
		
		String name;
		
		public void setName(String name){
			this.name = name;
		}
		
		public String getName(){
			return name;
		}

		
	}
	

}
