package de.danbim.swtquadtree;

import java.util.Set;

import org.eclipse.swt.graphics.Rectangle;

/**
 * Generic class representing a quadratic QuadTree using
 * {@link org.eclipse.swt.graphics.Rectangle} as a basis for positioning,
 * removing, updating and searching its elements.
 * 
 * @author Daniel Bimschas
 * 
 * @param <T>
 *            the type of the elements that are to be used as the items managed
 *            by this ISWTQuadTree
 */
public interface ISWTQuadTree<T> {

	/**
	 * Factory class for constructing ISWTQuadTree instances.
	 * 
	 * @author Daniel Bimschas
	 * 
	 * @param <T>
	 *            see {@link ISWTQuadTree}
	 */
	public static class Factory<T> {

		public Factory() {
			// nothing to do
		}

		/**
		 * Creates a new {@link ISWTQuadTree} instance. The
		 * <code>totalSideLength</code> as well as the
		 * <code>minSideLength</code> must be powers of two because then they
		 * are divisable into quadrants.
		 * 
		 * @param originX
		 *            the x coordinate of the origin
		 * @param originY
		 *            the y coordinate of the origin
		 * @param totalSideLength
		 *            the total length of a side of this quadratic
		 *            {@link ISWTQuadTree} instance, must be a power of 2
		 * @param minSideLength
		 *            the minimal side length of an instance of
		 *            {@link ISWTQuadTree}, must be a power of 2. The minimal
		 *            side length determines the resolution of the
		 *            {@link ISWTQuadTree}.
		 * @param capacity
		 *            the maximum number of objects that one node should handle
		 *            (excluding overflows when reaching maximum resolution)
		 * 
		 * @throws RuntimeException
		 *             if <code>totalSideLength</code> is not a power of 2
		 * @return a newly created ISWTQuadTree instance
		 */
		public ISWTQuadTree<T> create(int originX, int originY, int totalSideLength,
				int minSideLength, int capacity) {
			return new SWTQuadTree<T>(originX, originY, totalSideLength, minSideLength, capacity);
		}

	}

	/**
	 * Removes all items.
	 */
	void clear();

	/**
	 * Checks if the item <code>item</code> can be found with the bounding box
	 * <code>itemBoundingBox</code>.
	 * 
	 * @param item
	 *            the item to search for
	 * @param itemBoundingBox
	 *            the bounding box to use in search
	 * @return <code>true</code> if the item could be found, <code>false</code>
	 *         otherwise.
	 */
	boolean containsItem(T item, Rectangle itemBoundingBox);

	/**
	 * Returns the number of items currently held by this instance.
	 * 
	 * @return the number of items currently held by this instance
	 */
	int getItemCount();
	
	/**
	 * Inserts the object <code>item</code> into the ISWTQuadTree using the
	 * Rectangle <code>boundingBox</code> as its bounding box.
	 * 
	 * @param item
	 *            the item to insert
	 * @param boundingBox
	 *            the bounding box to use
	 * @throws RuntimeException
	 *             if one of the following occurs:
	 *             <ul>
	 *             <li>the bounding box does not intersect with the bounding box
	 *             of this ISWTQuadTree instance
	 *             <li> <code>item</code> is <code>null</code>
	 *             <li> <code>boundingBox</code> is <code>null</code>
	 *             </ul>
	 */
	void insertItem(T item, Rectangle boundingBox);

	/**
	 * Moves an object <code>item</code> from its old position and shape (i.e.
	 * the old bounding box <code>oldBoundingBox</code>) to a new position and
	 * shape (i.e. the bounding box <code>newBoundingBox</code>).
	 * 
	 * @param item
	 *            the item to move
	 * @param oldBoundingBox
	 *            the old position and shape
	 * @param newBoundingBox
	 *            the new position and shape
	 * @throws RuntimeException
	 *             if <code>item</code> is not found
	 */
	void moveItem(T item, Rectangle oldBoundingBox, Rectangle newBoundingBox);

	/**
	 * Removes the object <code>item</code> from the ISWTQuadTree using the
	 * Rectangle <code>boundingBox</code> for searching its position inside the
	 * tree.
	 * 
	 * @param item
	 *            the item to remove
	 * @param boundingBox
	 *            the bounding box to use for removal search
	 * @throws RuntimeException
	 *             if <code>item</code> is not found
	 */
	void removeItem(T item, Rectangle boundingBox);

	/**
	 * Returns all items managed by the {@link ISWTQuadTree} instance.
	 * 
	 * @return all items managed by the {@link ISWTQuadTree} instance
	 */
	Set<T> searchItems();
	
	/**
	 * Returns a set of all items that have bounding boxes intersecting with the
	 * bounding box <code>boundingBox</code>.
	 * 
	 * @param boundingBox
	 *            the boundingBox of the area in which to search
	 * @return the set of all items that have bounding boxes intersecting with
	 *         <code>boundingBox</code>
	 */
	Set<T> searchItems(Rectangle boundingBox);

}
