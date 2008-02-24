/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2001-2008, Martin Schoeberl (martin@jopdesign.com)

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
