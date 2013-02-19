package orego.util;

import static orego.core.Coordinates.pointToString;

import java.io.Serializable;

/**
 * A set implementation that offers constant time insertion, search, deletion,
 * clearing, and size, assuming that the keys are all in the range [0, n).
 * If space is important, or if the set is fairly dense, BitVector may be
 * preferable.
 * @see BitVector
 */
public class IntSet implements Serializable {

	private static final long serialVersionUID = 3336799989129195312L;

	/** data[i] is the ith element of this set. */
	private int[] data;

	/** locations[i] is the index in data where i is stored (if any). */
	private int[] locations;

	/** Number of elements in this set. */
	private int size;

	/** All keys must be in [0, capacity). */
	public IntSet(int capacity) {
		data = new int[capacity];
		locations = new int[capacity];
	}

	/**
	 * Adds key, which may or may not be present, to this set.
	 */
	public void add(int key) {
		if (!contains(key)) {
			addKnownAbsent(key);
		}
	}

	/** This set becomes the result of the union of this and that. */
	public void addAll(IntSet that) {
		for (int i = 0; i < that.size; i++) {
			add(that.get(i));
		}
	}

	/** Adds key, which is known to be absent, to this set. */
	public void addKnownAbsent(int key) {
		data[size] = key;
		locations[key] = size;
		size++;
	}

	/** Removes all elements from this set. */
	public void clear() {
		size = 0;
	}

	/** Returns true if key is in this set. */
	public boolean contains(int key) {
		int location = locations[key];
		return (location < size) & (data[locations[key]] == key);
	}

	/**
	 * Makes this into a copy of that, without the overhead of creating a new
	 * object.
	 */
	public void copyDataFrom(IntSet that) {
		size = that.size;
		System.arraycopy(that.data, 0, data, 0, size);
		System.arraycopy(that.locations, 0, locations, 0, locations.length);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		IntSet that = (IntSet) obj;
		if (that.data.length != data.length) {
			// If we have different universes, we're not equal.
			return false;
		}
		if (that.size != size) {
			return false;
		}
		for (int i = 0; i < size; i++) {
			if (!that.contains(data[i])) {
				return false;
			}
		}
		return true;
	}

	/** Returns the ith element of this list. */
	public int get(int i) {
		return data[i];
	}

	/** Removes key, which may or may not be present, from this set. */
	public void remove(int key) {
		if (contains(key)) {
			removeKnownPresent(key);
		}
	}

	/** Removes key, which is known to be present, from this set. */
	public void removeKnownPresent(int key) {
		size--;
		int location = locations[key];
		int replacement = data[size];
		data[location] = replacement;
		locations[replacement] = location;
	}

	/** Returns the number of elements in this set. */
	public int size() {
		return size;
	}

	public String toString() {
		String result = "{";
		if (size > 0) {
			result += data[0];
			for (int i = 1; i < size; i++) {
				result += ", " + data[i];
			}
		}
		return result + "}";
	}

	/**
	 * Similar to toString(), but displays human-readable point labels (e.g.,
	 * "d3") instead of ints.
	 */
	public String toStringAsPoints() {
		String result = size + ": {";
		if (size > 0) {
			result += pointToString(data[0]);
			for (int i = 1; i < size; i++) {
				result += ", " + pointToString(data[i]);
			}
		}
		return result + "}";
	}

}
