/**
 * A HashMap with a fixed size of entries and static allocation behaviour.
 *
 * @author Kun Wei, Frank Zeyda
 */
package hijac.cdx;

import hijac.cdx.Error;
import hijac.cdx.javacp.utils.HashSet;
import hijac.cdx.javacp.utils.Set;

//import java.util.HashSet;
//import java.util.Set;

/**
 * We note that the key and value of a map entry must not be null.
 */
public class CHashMap {
	/* The maximal capacity of this HashMap. */
	private int capacity;

	/* The actual size of the map; this is the number of entries. */
	private int size = 0;

	/* An array containing the key-value mappings. */
	private HashEntry[] table;

	/* An array that is used for the store of pre-allocated entry objects. */
	private HashEntry[] store;

	/* Mutable entry class; we access its fields directly. */

	static class HashEntry {
		private Object key;
		private Object value;
		private HashEntry next;
		private boolean valid;

		HashEntry() {
			key = null;
			value = null;
			next = null;
			valid = false;
		}
	}

	/**
	 * Constructs a new CHashMap with a specific initial capacity.
	 * 
	 * This pre-allocates memory for a respective number of entries.
	 * 
	 * @param initialCapacity
	 *            the initial capacity of this CHashMap
	 */
	public CHashMap(int initialCapacity) {
		/* Set the capacity of the table; note that it cannot increase. */
		capacity = initialCapacity;

		/* Initially the table is empty. */
		size = 0;

		/* Initialise the table array. */
		table = new HashEntry[initialCapacity];

		/* Initialise the store array. */
		store = new HashEntry[initialCapacity];

		/* Pre-allocate HashEntry object for potential entries. */
		for (int i = 0; i < initialCapacity; i++) {
			store[i] = new HashEntry();
		}
	}

	/**
	 * Helper method that returns an index into the buckets array for `key'
	 * based on its hashCode().
	 * 
	 * @param key
	 *            the key
	 * @return the bucket number
	 */
	private final int hash(Object key) {
		return key == null ? 0 : Math.abs(key.hashCode() % capacity);
	}

	/**
	 * Returns the value in this CHashMap associated with the supplied key, or
	 * <code>null</code> if no mapping for the key exists in the map.
	 * 
	 * @param key
	 *            the key for which to fetch an associated value
	 * @return value that the key maps to, if present
	 */
	public Object get(Object key) {
		int idx = hash(key);
		HashEntry entry = table[idx];
		while (entry != null) {
			if (key.equals(entry.key)) {
				/* Only return a value if the entry is valid. */
				if (entry.valid) {
					return entry.value;
				} else {
					return null;
				}
			}
			/* Sift through elements in the bucket. */
			entry = entry.next;
		}
		return null;
	}

	/**
	 * Puts the supplied value into the Map, stored under the supplied key.
	 * 
	 * @param key
	 *            the key under which the value is stored
	 * @param value
	 *            the value to be stored in the HashMap
	 */
	public void put(Object key, Object value) {
		int idx = hash(key);
		if (table[idx] == null) {
			/* Use a pre-allocated HashEntry from the store. */
			if (size == capacity) {
				Error.abort("Exceeding storage capacity in CHashMap.");
			}
			HashEntry entry = store[size++];
			table[idx] = entry;
			/* Initialise the new entry. */
			entry.key = key;
			entry.value = value;
			entry.next = null;
			entry.valid = true;
		} else {
			HashEntry entry = table[idx];
			HashEntry prev = null;
			while (entry != null) {
				if (key.equals(entry.key)) {
					/* If an entry for the key exists just update the value. */
					//TODO: What happens with the previous value?
					entry.value = value;
					entry.valid = true;
					return;
				} else {
					prev = entry;
					entry = entry.next;
				}
			}
			/* If no entry with a matching key is found, create a new entry. */
			if (size == capacity) {
				Error.abort("Exceeding storage capacity in CHashMap.");
			}
			entry = store[size++];
			/* Initialise the new entry. */
			entry.key = key;
			entry.value = value;
			entry.next = null;
			entry.valid = true;
			/* Added by Frank Zeyda */
			prev.next = entry;
			/* End of Addition */
		}
	}

	/**
	 * Removes an value from the map that is associated with a given key.
	 * 
	 * @param key
	 *            the key of the value to be remove from the map
	 * @param the
	 *            removed value
	 */
	public Object remove(Object key) {
		int idx = hash(key);
		HashEntry entry = table[idx];
		while (entry != null) {
			if (key.equals(entry.key)) {
				/*
				 * We cannot just remove the entry as this results in a memory
				 * leak. Instead, we mark the entry as invalid so that it can be
				 * reused.
				 */
				entry.valid = false;
				return entry.value;
			}
			/* Sift through elements in the bucket. */
			entry = entry.next;
		}
		return null;
	}

	/**
	 * Returns all keys within the hash map as a Set.
	 * 
	 * The implementation is not very efficient here as it traverses the table
	 * array index by index. However, for the sake of our example this may be
	 * sufficient; a more efficient implementation would back the map by a key
	 * and value set. We note that this method allocates memory in whatever
	 * scope it is called, namely for the Set object returned.
	 * 
	 * @return keys within the hash map viewed as a Set
	 */
	//TODO: illegal references
	public Set keySet() {
		HashSet set = new HashSet();
		for (int idx = 0; idx < table.length; idx++) {
			HashEntry entry = table[idx];
			while (entry != null) {
				set.add(entry.key);
				entry = entry.next;
			}
		}
		return set;
	}
}
