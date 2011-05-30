package rttm.skiplist;


import com.jopdesign.sys.Native;

public class SkipListTM_2 extends AbstractSkipList {
	

	public boolean Insert(int key) {
		Node[] update = new Node[MaxLevel];
		Node curr = null;
		int newLevel = GetNewLevel();
		Node newNode = new Node(newLevel, key);
		boolean result = false;

		Native.wrMem(1, Const.MAGIC);
//		result = false;
//		curr = null;
		curr = Search(key, update);
		if (curr == null) {
			Insert(key, update, newNode, newLevel);
			result = true;
		}
		Native.wrMem(0, Const.MAGIC);
		return result;
	}

	public boolean Remove(int key) {
		Node[] update = new Node[MaxLevel];
		Node curr;
		boolean result = false;
		Native.wrMem(1, Const.MAGIC);
		curr = Search(key, update);
		if (curr != null) {
			for (int i = 0; i < listLevel && update[i].get(i) == curr; i++) {
				update[i].set(i, curr.get(i));
			}
			result = true;
		}
		Native.wrMem(0, Const.MAGIC);
		return result;
	}

	protected void Insert(int key, Node[] update, Node newNode, int newLevel) {
		if (newLevel > listLevel) {
			for (int i = listLevel; i < newLevel; i++) {
				update[i] = header;
			}
			Native.wrMem(1, Const.MAGIC);
			listLevel++;
			Native.wrMem(0, Const.MAGIC);
		}
		for (int i = 0; i < newLevel; i++) {
			newNode.set(i, update[i].get(i));
			update[i].set(i, newNode);
		}
	}

	protected Node Search(int key, Node[] update) {
		Node curr;
		Native.wrMem(1, Const.MAGIC);
		int comp;
		curr = header;
		for (int i = listLevel - 1; i >= 0; i--) {
			comp = curr.get(i).key;
			while (curr.get(i) != header && comp < key) {
				curr = curr.get(i);
				comp = curr.get(i).key;
			}
			update[i] = curr;
		}
		curr = curr.get(0);

		comp = curr.key;

		if (curr == header || comp != key)
			curr = null;
		Native.wrMem(0, Const.MAGIC);

		return curr;
	}

}
