package rttm.skiplist;

import java.util.Random;

public abstract class AbstractSkipList {
	// / <summary>
	// / Represents a node in the SkipList.
	// / </summary>
	class Node {

		// References to nodes further along in the skip list.
		private Node[] forward;

		// The key/value pair.
		int key;

		// / <summary>
		// / No-arg constructor
		// / </summary>
		public Node() {
		}

		// / <summary>
		// / Initializes an instant of a Node with its node level.
		// / </summary>
		// / <param name="level">
		// / The node level.
		// / </param>
		public Node(int level) {
			forward = new Node[level];
		}

		// / <summary>
		// / Initializes an instant of a Node with its node level and
		// / key/value pair.
		// / </summary>
		// / <param name="level">
		// / The node level.
		// / </param>
		// / <param name="key">
		// / The key for the node.
		// / </param>
		// / <param name="val">
		// / The value for the node.
		// / </param>
		public Node(int level, int key) {
			forward = new Node[level];
			this.key = key;
		}

		public Node get(int i) {

			return this.forward[i];
		}

		public void set(int i, Node value) {
			this.forward[i] = value;

		}
	}

	public AbstractSkipList() {
		int size = 0;
		header = new Node(MaxLevel);
		listLevel = 1;
		for (int i = 0; i < MaxLevel; i++) {
			header.set(i, header);
		}
		// initialize transaction objects
		while (size < INITIAL_SIZE) {
			int v = random.nextInt();
			if ((boolean) Insert(v)) {
				size++;
				Shadow += v;
			}
		}
	}

	protected abstract Node Search(int key, Node[] update);

	protected abstract void Insert(int key, Node[] update, Node newNode,
			int newLevel);

	public abstract boolean Remove(int key);

	public abstract boolean Insert(int key);

	protected final int MaxLevel = 16;
	private final int Probability = 500;
	public static int INITIAL_SIZE = 1000;
	protected Node header;
	protected Random random = new Random();
	protected int listLevel;
	public int Shadow = 0;

	public int Actual() {
		Node n = header.get(0);
		int total = 0;

		while (n != header) {
			total += n.key;
			n = n.get(0);
		}

		return total;
	}

	protected int GetNewLevel() {
		int level = 1;

		// Determines the next node level.
		while ((random.nextInt() % 1000) < Probability && level < MaxLevel
				&& level <= listLevel) {
			level++;
		}

		return level;
	}

	public boolean Contains(int key) {
		Node[] dummy = new Node[MaxLevel];
		return Search(key, dummy) != null;
	}

}
