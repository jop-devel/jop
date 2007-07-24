package wcet.framework.interfaces.cfg;


public interface IEdge {
	
	public int getFromVertex();
	
	public int getToVertex();
	
	public int getIndex();
	
	public int getFrequency();
	
	public void setFrequency(int f);
	
	public void setExceptionEdge();
	
	public boolean isExceptionEdge();
}
