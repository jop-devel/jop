/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2007, Alberto Andreotti

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
public class SimpleTree {	
	class node{
	node left,right;
	boolean initialized;
	int myLevel;
	
	public void grow(int i){
			if (i>0 && initialized==false){
				left=new node();
				right=new node();
				left.grow(i-1);
				right.grow(i-1);
				initialized=true;
				myLevel=i;
			}
			return;
		}

	
	public boolean verify(int depth){
		boolean Ok;
		Ok= myLevel==depth;
		if (depth>0){
		Ok=Ok && left.verify(depth-1);
		Ok=Ok && right.verify(depth-1);
			}
		return Ok;
	}
	}
	private node root;
	private int depth;
	public void setRootNode(node n){
		root=n;
		
	}
	public  SimpleTree(int depth){
		root= new node();
		root.grow(depth);
		this.depth=depth;
	}
	public  SimpleTree(){
		
	}
	
	
	
	public boolean verify(){
		
	return root.verify(depth);	
	}
	
	public SimpleTree getLeftSubtree(){
		SimpleTree st;
		st=new SimpleTree();
		st.setRootNode(root.left);
		st.depth=depth -1;
		return st;
		}
	
	public SimpleTree getRightSubtree(){
		SimpleTree st;
		st=new SimpleTree();
		st.setRootNode(root.right);
		st.depth=depth -1;
		return st;
	}
	
	}

	
	

