package de.danbim.swtquadtree;

import java.util.Set;

import org.eclipse.swt.graphics.Rectangle;

/**
 * Synchronized version of {@link SWTQuadTree}. Overrides all public methods and
 * makes them synchronized, calling the super implementation.
 * 
 * @author Daniel Bimschas
 * 
 * @param <T>
 *            the type of the Elements the tree manages
 */
class SynchronizedSWTQuadTree<T> extends SWTQuadTree<T> {

	public SynchronizedSWTQuadTree(int originX, int originY, int totalSideLength,
			int minSideLength, int capacity) {
		super(originX, originY, totalSideLength, minSideLength, capacity);
	}

	@Override
	public synchronized void clear() {
		super.clear();
	};

	@Override
	public synchronized boolean containsItem(T item, Rectangle itemBoundingBox) {
		return super.containsItem(item, itemBoundingBox);
	};

	@Override
	public synchronized boolean equals(Object obj) {
		return super.equals(obj);
	}

	@Override
	public synchronized int getItemCount() {
		return super.getItemCount();
	}

	@Override
	public synchronized int hashCode() {
		return super.hashCode();
	}

	@Override
	public synchronized void insertItem(T item, Rectangle itemBoundingBox) {
		super.insertItem(item, itemBoundingBox);
	};

	@Override
	public synchronized void moveItem(T item, Rectangle oldItemBoundingBox,
			Rectangle newItemBoundingBox) {
		super.moveItem(item, oldItemBoundingBox, newItemBoundingBox);
	};

	@Override
	public synchronized void removeItem(T item, Rectangle itemBoundingBox) {
		super.removeItem(item, itemBoundingBox);
	};

	@Override
	public synchronized Set<T> searchItems() {
		return super.searchItems();
	}

	@Override
	public synchronized Set<T> searchItems(Rectangle boundingBox) {
		return super.searchItems(boundingBox);
	}

	@Override
	public synchronized String toString() {
		return super.toString();
	}

}
