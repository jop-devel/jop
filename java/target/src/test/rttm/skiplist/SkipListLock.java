package rttm.skiplist;

public class SkipListLock extends AbstractSkipList {

	private volatile Object lockObject = new Object();

	public boolean Insert(int key) {
		Node[] update = new Node[MaxLevel];
		Node curr = null;
		int newLevel = GetNewLevel();
		Node newNode = new Node(newLevel, key);
		boolean result = false;

		synchronized (lockObject) {
			result = false;
			curr = null;
			curr = Search(key, update);
			if (curr == null) {
				Insert(key, update, newNode, newLevel);
				result = true;
			}
		}

		return result;

	}

	public boolean Remove(int key) {
		Node[] update = new Node[MaxLevel];
		Node curr;
		boolean result = false;
		synchronized (lockObject) {
			curr = Search(key, update);
			if (curr != null) {
				for (int i = 0; i < listLevel && update[i].get(i) == curr; i++) {
					update[i].set(i, curr.get(i));
				}
				result = true;
			}
		}
		return result;
	}


	protected void Insert(int key, Node[] update, Node newNode, int newLevel) {
		if (newLevel > listLevel) {
			
			for (int i = listLevel; i < newLevel; i++) {
				update[i] = header;
			}
			synchronized (lockObject) {
				listLevel++;
			}
		}
		for (int i = 0; i < newLevel; i++) {
			newNode.set(i, update[i].get(i));
			update[i].set(i, newNode);
		}
	}

	protected Node Search(int key, Node[] update) {
		Node curr;

		synchronized (lockObject) {
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
		}

		return curr;
	}

}
