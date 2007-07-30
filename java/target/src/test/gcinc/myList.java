package gcinc;
import java.util.Iterator;
public class myList implements Iterator {	
	class Item{
		int id;
		Object item;
		Item nextItem;
	}
	public myList(){}
	public Item lastItem,firstItem,currentItem;
	public int size;
	
	
	public void add(Object o) {
		size++;
		Item e = new Item();
		e.item = o;
		//add the element at the end of the list
		synchronized (this) {
			if (lastItem!=null) {
				lastItem.nextItem= e;
			} else {
				firstItem = e;
			}
			lastItem = e;
		}
	}

	public int size(){
	return size;
	}

	
	//Iterator interface methods
	
	public Object next() {
		
		if (currentItem==null){
			currentItem=firstItem;
		}
		Object currentObject=currentItem.item;
		//make iteration cyclic 
		if(currentItem.nextItem!=null){
		currentItem=currentItem.nextItem;
		}else{
			currentItem=firstItem;
		}
		return currentObject;
				
		}
	public boolean hasNext() {
		
		if(currentItem.nextItem==null){
			return false;
		}else {
			return true;
		}
		
		}
	public void remove(){
		
		}
}
