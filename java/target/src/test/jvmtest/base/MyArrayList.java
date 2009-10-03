/* jvmtest - Testing your VM 
  Copyright (C) 20009, Guenther Wimpassinger

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
/**
 * Simple generic list implemented with java arrays
 */
package jvmtest.base;

/**
 * @author Günther Wimpassinger
 *
 */
public class MyArrayList<T extends Object> {
	private Object[] array;
	private int memSize;
	private int elementCount;
	
	public MyArrayList() {
		memSize = 4;
		elementCount = 0;
		array = new Object[memSize];		
	}
	
	private void setCapacity(int newSize) {
		Object[] newarray = new Object[newSize];
		int newCount = 0;
		
		for (int i=0;i<elementCount && i<newSize;i++) {
			newarray[i] = array[i];
			newCount++;
		}
		
		array = newarray;
		memSize = newSize;
		elementCount = newCount;
	}
	
	private boolean spaceAvailable() {
		return elementCount<memSize;		
	}
	
	private void checkCapacity() {
		if (!spaceAvailable()) {
			setCapacity(memSize << 1);
		}
	}
	
	public void clear() {
		setCapacity(4);
		elementCount = 0;
	}
	
	public boolean add(T item) {
		checkCapacity();
		
		array[elementCount] = item;
		elementCount++;
		return true;		
	}
	
	public boolean add(int index, T item) {
		
		if (index>=size()) {
			return add(item);
		} else {
			checkCapacity();
			
			for (int i=size();index<i;i--) {
				array[i]=array[i-1];				
			}
			array[index]=item;
			elementCount++;
			return true;		
		}
		
	}

	
	public int size() {
		return elementCount;
	}
	
	public boolean contains(Object o) {
		for (int i=0;i<elementCount;i++) {
			if (array[i]==o) {
				return true;
			}
		}
		return false;
	}
	
	/* @SuppressWarnings("unchecked") */
	public T get(int index) {
		return (T)array[index];
	}
	
}
