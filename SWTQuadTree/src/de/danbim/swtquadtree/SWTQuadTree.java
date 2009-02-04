package de.danbim.swtquadtree;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.graphics.Rectangle;

class SWTQuadTree<T> implements ISWTQuadTree<T> {

	static class Entry<K> {

		public Rectangle boundingBox;

		public K item;

		public Entry(Rectangle boundingBox, K item) {
			this.boundingBox = boundingBox;
			this.item = item;
		}

	}

	private static final String ERROR_BOUNDING_BOX_NO_INTERSECTION = "The bounding box must "
			+ "intersect the tree elements' bounding box.";

	private static final String ERROR_ITEM_NOT_CONTAINED = "Could not remove the item because it "
			+ "was not found in this quad tree. The existence in the tree is a precondition for "
			+ "calling the remove or move function.";

	private static final int LOWER_LEFT = 3;

	private static final int LOWER_RIGHT = 2;

	private static final int UPPER_LEFT = 0;

	private static final int UPPER_RIGHT = 1;

	/**
	 * Checks if the rectangle <code>itemBoundingBox</code> really lies inside
	 * this nodes bounding box, not only if they intersect.
	 * 
	 * @param itemBoundingBox
	 * @return <code>true</code> if <code>itemBoundingBox</code> lies within
	 *         <code>boundingBox</code>, <code>false</code> otherwise
	 */
	static boolean boundingBoxContains(Rectangle boundingBox, Rectangle itemBoundingBox) {

		int x, y;

		// check upper left point of itemBoundingBox
		x = itemBoundingBox.x;
		y = itemBoundingBox.y;
		if (!boundingBox.contains(x, y))
			return false;

		// check lower right point of itemBoundingBox
		x = itemBoundingBox.x + itemBoundingBox.width;
		y = itemBoundingBox.y + itemBoundingBox.height;
		if (!boundingBox.contains(x, y))
			return false;

		// both point defining the rectangle lie within bounding box
		return true;

	}

	/**
	 * Checks if an integer is a power of two by checking if <code>n</code> > 0
	 * and if only one bit in <code>n</code> is set.
	 * 
	 * @param n
	 *            the number to check
	 * @return <code>true</code> if <code>n</code> is a power of two,
	 *         <code>false</code> otherwise
	 */
	private static boolean isPowerOfTwo(int n) {
		return ((n != 0) && (n & (n - 1)) == 0);
	}

	/**
	 * The bounding box of this tree element.
	 */
	Rectangle boundingBox;

	/**
	 * The maximum number of objects that a node is allowed to hold in
	 * <code>objects</code>.
	 */
	final int capacity;

	/**
	 * An array containing the bounding boxes of the child nodes of this tree
	 * element. Mostly used for checking if an item would fit into a child node.
	 */
	Rectangle[] childBoxes;

	/**
	 * An array containing the child nodes of this tree element
	 */
	SWTQuadTree<T>[] children;

	/**
	 * Set to <code>true</code> when this node is a leaf, i.e. it has no child
	 * nodes. Must be set in all manipulating algorithms and is validated
	 * {@link SWTQuadTree#isOfIntegrity()} in assertion calls.
	 */
	boolean leaf;

	/**
	 * Set to true when the maximum resolution is reached. Used for speeding up
	 * checks. Must be set by the constructor.
	 */
	boolean maximumResolutionReached;

	/**
	 * The minimum side length which defines the maximum resolution of the
	 * QuadTree.
	 */
	int minSideLength;

	/**
	 * All objects that belong into this quadrant. Here should only be objects
	 * that don't fit into a child but totally fit into this nodes bounding box.
	 */
	List<Entry<T>> objects;

	/**
	 * All objects that don't fit into child nodes but intersect with this nodes
	 * bounding box. Also contains objects that don't go into
	 * <code>objects</code> if the capacity and maximum resolution is reached.
	 */
	List<Entry<T>> overflows;

	/**
	 * The parent node. Must always be set, except when this node is the root of
	 * the tree. Assured by {@link SWTQuadTree#isOfIntegrity()} in assertion
	 * calls.
	 */
	private SWTQuadTree<T> parent;

	public SWTQuadTree(int originX, int originY, int totalSideLength, int minSideLength,
			int capacity) {

		// this constructor is only to be called from the factory in
		// ISWTQuadTree and not internally, therefore parent is null
		// as we're constructing the root node here.
		this(null, new Rectangle(originX, originY, totalSideLength, totalSideLength),
				minSideLength, capacity);

	}

	@SuppressWarnings("unchecked")
	private SWTQuadTree(SWTQuadTree<T> parent, Rectangle boundingBox, int minSideLength,
			int capacity) {

		if (!isPowerOfTwo(boundingBox.width) || !isPowerOfTwo(minSideLength))
			throw new RuntimeException("Both arguments must be powers of two!");

		this.parent = parent;
		this.boundingBox = boundingBox;
		this.minSideLength = minSideLength;
		this.capacity = capacity;

		children = new SWTQuadTree[4];
		childBoxes = new Rectangle[4];
		objects = new ArrayList<Entry<T>>(capacity);
		overflows = new ArrayList<Entry<T>>(capacity);
		leaf = true;

		int childSideLength = boundingBox.width / 2;

		if (!(childSideLength < minSideLength)) {

			maximumResolutionReached = false;

			childBoxes[UPPER_LEFT] = new Rectangle(boundingBox.x, boundingBox.y, childSideLength,
					childSideLength);
			childBoxes[UPPER_RIGHT] = new Rectangle(boundingBox.x + childSideLength, boundingBox.y,
					childSideLength, childSideLength);
			childBoxes[LOWER_RIGHT] = new Rectangle(boundingBox.x + childSideLength, boundingBox.y
					+ childSideLength, childSideLength, childSideLength);
			childBoxes[LOWER_LEFT] = new Rectangle(boundingBox.x, boundingBox.y + childSideLength,
					childSideLength, childSideLength);

		} else {

			maximumResolutionReached = true;

		}

		// check invariants
		assert isOfIntegrity();

	}

	void boundingBoxCheck(Rectangle itemBoundingBox) {

		// check precondition
		if (!boundingBox.intersects(itemBoundingBox))
			throw new RuntimeException(ERROR_BOUNDING_BOX_NO_INTERSECTION);

	}

	/**
	 * Checks if this node has reached it's maximum node capacity.
	 * 
	 * @return
	 */
	boolean capacityReached() {
		return capacity <= objects.size();
	}

	/**
	 * Cleans up the current tree element by deleting obsolete children if
	 * possible and cleaning up the tree upwards.
	 */
	void cleanUp() {

		// check if all items in the subnodes would fit into this and in this
		// case reorder them
		int itemCount = getItemCountRecursive();
		if (itemCount <= capacity) {

			// put all objects from children into this node
			List<Entry<T>> reorderSet = new ArrayList<Entry<T>>(itemCount);
			getChildItemsRecursive(reorderSet);
			objects.addAll(reorderSet);

			// remove child nodes
			children[UPPER_LEFT] = children[UPPER_RIGHT] = children[LOWER_RIGHT] = children[LOWER_LEFT] = null;
			leaf = true;

		} else {

			// remove empty children if there are any
			for (int i = 0; i < 4; i++)
				if (children[i] != null && children[i].getItemCountRecursive() == 0)
					children[i] = null;

		}

		// check if there are empty children so we can remove them
		if (!leaf) {
			boolean isLeaf = true;
			for (int i = 0; i < 4; i++) {
				if (children[i] != null && children[i].getItemCountRecursive() == 0)
					children[i] = null;
				else if (children[i] != null)
					isLeaf = false;
			}
			leaf = isLeaf;
		}
		
		// propagate cleanup to parent
		if (parent != null) {
			parent.cleanUp();
			return;
		}

		// check invariants
		assert isOfIntegrity();

	}

	public void clear() {

		// check invariants
		assert isOfIntegrity();

		// remove child nodes
		children[UPPER_LEFT] = children[UPPER_RIGHT] = children[LOWER_RIGHT] = children[LOWER_LEFT] = null;
		leaf = true;

		// empty objects and overflows
		objects.clear();
		overflows.clear();

		// check invariants
		assert isOfIntegrity();

	}

	public boolean containsItem(T item, Rectangle itemBoundingBox) {

		SWTQuadTree<T> node = searchNode(itemBoundingBox);

		for (Entry<T> entry : node.objects)
			if (entry.item == item)
				return true;

		for (Entry<T> entry : node.overflows)
			if (entry.item == item)
				return true;

		return false;
	}

	void createChild(int position) {

		assert position == UPPER_LEFT || position == UPPER_RIGHT || position == LOWER_RIGHT
				|| position == LOWER_LEFT;

		assert children[position] == null;
		assert childBoxes[position] != null;

		children[position] = new SWTQuadTree<T>(this, childBoxes[position], minSideLength, capacity);

		leaf = false;

	}

	/**
	 * Returns the number of objects recursively excluding this node.
	 * 
	 * @return
	 */
	int getChildItemCountRecursive() {

		int itemCount = 0;

		for (SWTQuadTree<T> child : children)
			if (child != null)
				itemCount += child.getItemCountRecursive();

		return itemCount;

	}

	void getChildItemsRecursive(List<Entry<T>> set) {

		for (SWTQuadTree<T> child : children)
			if (child != null)
				child.getItemsRecursive(set);

	}

	/**
	 * Searches the child rectangle's position (one of
	 * {@link SWTQuadTree#UPPER_LEFT}, {@link SWTQuadTree#UPPER_RIGHT},
	 * {@link SWTQuadTree#LOWER_RIGHT}, {@link SWTQuadTree#LOWER_LEFT}) into
	 * which the bounding box <code>itemBoundingBox</code> would fit.
	 * 
	 * @return the child rectangle's position, -1 if it doesn't fit into a
	 *         child rectangle, -1 if maximum resolution is reached
	 */
	int getFittingChildRectanglePosition(Rectangle itemBoundingBox) {

		if (maximumResolutionReached)
			return -1;
		
		for (int i = 0; i < 4; i++)
			if (boundingBoxContains(childBoxes[i], itemBoundingBox))
				return i;

		return -1;

	}

	public int getItemCount() {

		return getItemCountRecursive();

	}

	/**
	 * Returns the number of items recursively including this node.
	 * 
	 * @return
	 */
	int getItemCountRecursive() {

		return objects.size() + overflows.size() + getChildItemCountRecursive();

	}

	void getItemsRecursive(List<Entry<T>> set) {

		assert isDisjoint(set, overflows);
		assert isDisjoint(set, objects);

		set.addAll(overflows);
		set.addAll(objects);

		getChildItemsRecursive(set);

	}

	public void insertItem(T item, Rectangle itemBoundingBox) {

		// check invariants
		assert isOfIntegrity();

		// this node must be the root node, this method must not be called
		// internally
		assert parent == null;

		boundingBoxCheck(itemBoundingBox);

		insertItemInternal(item, itemBoundingBox);

		// check invariants
		assert isOfIntegrity();

	}

	/**
	 * Inserts the item either inside this tree element. It does this by
	 * <ul>
	 * <li>passing it to the appropriate child element (after creating it if not
	 * existing) or
	 * <li>passing the element to the parent node if this elements bounding box
	 * doesn't intersect with the items bounding box.
	 * </ul>
	 * 
	 * Should only be called by
	 * {@link SWTQuadTree#moveItem(Object, Rectangle, Rectangle)} and
	 * {@link SWTQuadTree#insertItem(Object, Rectangle)}.
	 * 
	 * @param item
	 * @param boundingBox
	 */
	void insertItemInternal(T item, Rectangle itemBoundingBox) {

		if (!boundingBoxContains(boundingBox, itemBoundingBox)) {

			if (parent == null)
				overflows.add(new Entry<T>(itemBoundingBox, item));
			else
				parent.insertItemInternal(item, itemBoundingBox);

		}

		else if (!maximumResolutionReached && wouldFitIntoChildNode(itemBoundingBox)) {

			if (leaf && !capacityReached()) {

				objects.add(new Entry<T>(itemBoundingBox, item));

			} else {

				int fittingChildRectanglePosition = getFittingChildRectanglePosition(itemBoundingBox);

				assert fittingChildRectanglePosition == UPPER_LEFT
						|| fittingChildRectanglePosition == UPPER_RIGHT
						|| fittingChildRectanglePosition == LOWER_RIGHT
						|| fittingChildRectanglePosition == LOWER_LEFT;

				// create child node
				if (children[fittingChildRectanglePosition] == null)
					createChild(fittingChildRectanglePosition);

				// reorder children after creating child node
				int reorderFitPosition;
				for (Entry<T> entry : objects) {

					// objects items must fit into a child node because
					// otherwise they wouldn't have been placed into objects but
					// into overflows
					assert wouldFitIntoChildNode(entry.boundingBox);

					// test where reorder item would fit
					reorderFitPosition = getFittingChildRectanglePosition(entry.boundingBox);

					assert reorderFitPosition == UPPER_LEFT || reorderFitPosition == UPPER_RIGHT
							|| reorderFitPosition == LOWER_RIGHT
							|| reorderFitPosition == LOWER_LEFT;

					// assure the node for the item to reorder exists
					if (children[reorderFitPosition] == null)
						createChild(reorderFitPosition);

					// finally insert the item to be reordered
					children[reorderFitPosition].insertItemInternal(entry.item, entry.boundingBox);

				}
				// all objects should now be reordered
				objects.clear();
				// end reordering children

				// now, at last, really insert the item we wanted to insert in
				// the first place
				children[fittingChildRectanglePosition].insertItemInternal(item, itemBoundingBox);

			}

		} else {

			// the item doesn't fit into one of the child boxes or the maximum
			// resolution is reached
			overflows.add(new Entry<T>(itemBoundingBox, item));

		}

	}

	/**
	 * Used in {@link SWTQuadTree#isOfIntegrity()}.
	 */
	private boolean integrityCheckParentReferences(boolean isRoot) {

		if (!isRoot && parent == null)
			return false;

		for (SWTQuadTree<T> child : children)
			if (child != null)
				return child.integrityCheckParentReferences(false);

		return true;

	}

	private boolean isDisjoint(List<Entry<T>> set1, List<Entry<T>> set2) {

		for (Entry<T> item1 : set1)
			for (Entry<T> item2 : set2)
				if (item1.item == item2.item)
					return false;

		return true;

	}

	private boolean isDisjoint(List<Entry<T>> set1, Set<T> set2) {

		for (Entry<T> entry : set1)
			for (T item : set2)
				if (entry.item == item)
					return false;

		return true;

	}

	/**
	 * Checks the integrity of this tree element. For all nodes in the tree it
	 * checks
	 * 
	 * <ul>
	 * <li>if there are children then objects must be empty,
	 * <li>if the items in objects are not more than capacity allows,
	 * <li>if the items in the children would fit into this node,
	 * <li>if there are empty children,
	 * <li>if the maximum resolution is reached and
	 * <code>maximumResolutionReached</code> is set correctly,
	 * <li>if the maximum resolution is reached there should be no child nodes
	 * and node child boxes,
	 * <li>if all references to <code>parent</code> are set correctly
	 * <li>and if <code>leaf</code> is set correctly.
	 * <ul>
	 * 
	 * Each of these checks stands for an invariant and by this the method can
	 * be used as a precondition as well as a postcondition check in every
	 * method call of the public methods that are defined by
	 * {@link ISWTQuadTree} if they do updates to the tree structure.
	 * 
	 * Should only be called by assertions to ensure the correctness of the
	 * implementation. During runtime we don't want to do checks all the time
	 * since we're focusing on performance here.
	 */
	boolean isOfIntegrity() {

		// check if there are children then objects must be empty
		if (!leaf && objects.size() > 0)
			return false;

		// check if items in objects are not more than capacity allows
		if (capacity < objects.size())
			return false;

		// check if the items in the children would fit into this node
		if (!leaf && capacity >= objects.size() + getChildItemCountRecursive())
			return false;

		// check if there are empty children
		for (SWTQuadTree<T> child : children)
			if (child != null)
				if (child.getItemCountRecursive() == 0)
					return false;

		// check if <code>leaf</code> is set correctly
		for (SWTQuadTree<T> child : children)
			if (child != null && leaf)
				return false;

		// check if the maximum resolution is reached and
		// <code>maximumResolutionReached</code> is set correctly
		if (boundingBox.width / 2 < minSideLength && !maximumResolutionReached)
			return false;

		// check: if the maximum resolution is reached there should be no child
		// nodes and node child boxes
		if (maximumResolutionReached
				&& (!leaf || childBoxes[UPPER_LEFT] != null || childBoxes[UPPER_RIGHT] != null
						|| childBoxes[LOWER_RIGHT] != null || childBoxes[LOWER_LEFT] != null))
			return false;

		// check if all connections to <code>parent</code> are set correctly
		// search parent
		SWTQuadTree<T> currentNode = this;
		while (currentNode.parent != null)
			currentNode = currentNode.parent;
		// run check starting from root node
		if (!currentNode.integrityCheckParentReferences(true))
			return false;
		// end check if all connections to <code>parent</code> are set correctly

		// all checks were fine
		return true;

	}

	public void moveItem(T item, Rectangle oldItemBoundingBox, Rectangle newItemBoundingBox) {

		// check invariants
		assert isOfIntegrity();

		SWTQuadTree<T> node = searchNode(oldItemBoundingBox);

		// check if item is contained and then remove it, otherwise throw an
		// exception since it's a precondition that item is managed by the tree
		if (!removeItemFromList(node.objects, item))
			if (!removeItemFromList(node.overflows, item))
				throw new RuntimeException(ERROR_ITEM_NOT_CONTAINED);

		node.insertItemInternal(item, newItemBoundingBox);
		node.cleanUp();

		// check invariants
		assert isOfIntegrity();

	}

	public void removeItem(T item, Rectangle itemBoundingBox) {

		// check invariants
		assert isOfIntegrity();

		SWTQuadTree<T> node = searchNode(itemBoundingBox);

		// check if item is contained and then remove it, otherwise throw an
		// exception since it's a precondition that item is managed by the tree
		if (!removeItemFromList(node.objects, item))
			if (!removeItemFromList(node.overflows, item))
				throw new RuntimeException(ERROR_ITEM_NOT_CONTAINED);

		node.cleanUp();

		// check invariants
		assert isOfIntegrity();

	}

	boolean removeItemFromList(List<Entry<T>> list, T item) {

		int toRemove = -1;

		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).item == item)
				toRemove = i;
		}

		if (toRemove != -1) {
			list.remove(toRemove);
			return true;
		}

		return false;

	}

	public Set<T> searchItems() {
		return searchItems(boundingBox);
	}

	public Set<T> searchItems(Rectangle boundingBox) {

		boundingBoxCheck(boundingBox);

		HashSet<T> set = new HashSet<T>();
		searchItemsInternal(set, boundingBox);
		return set;

	}

	void searchItemsInternal(HashSet<T> set, Rectangle boundingBox) {

		assert isDisjoint(overflows, set);
		assert isDisjoint(objects, set);

		for (Entry<T> e : objects)
			if (e.boundingBox.intersects(boundingBox))
				set.add(e.item);

		for (Entry<T> e : overflows)
			if (e.boundingBox.intersects(boundingBox))
				set.add(e.item);

		if (!leaf) {
			for (int i = 0; i < 4; i++)
				if (children[i] != null && childBoxes[i].intersects(boundingBox))
					children[i].searchItemsInternal(set, boundingBox);
		}

	}

	/**
	 * Search the tree element which should contain an item with the bounding
	 * box <code>boundingBox</code>.
	 * 
	 * @param rect
	 *            the bounding box for which the tree element should be searched
	 */
	SWTQuadTree<T> searchNode(Rectangle boundingBox) {

		int position = !maximumResolutionReached ? getFittingChildRectanglePosition(boundingBox) : -1;

		if (position != -1) {

			return leaf ? this : children[position] == null ? this : children[position].searchNode(boundingBox);

		}

		return this;

	}

	@Override
	public String toString() {
		StringBuffer buff = new StringBuffer();
		toString(buff, 0);
		return buff.toString();
	};

	private void toString(StringBuffer buff, int indent) {

		buff.append(boundingBox.toString() + "(");

		buff.append("objects: ");
		for (Entry<T> entry : objects)
			buff.append("(" + entry.item.toString() + "), ");

		buff.append("overflows: ");
		for (Entry<T> entry : overflows)
			buff.append("(" + entry.item.toString() + "), ");

		buff.append(")\n");

		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < indent + 3; j++)
				buff.append(" ");
			if (children[i] != null) {
				buff.append("+--");
				children[i].toString(buff, indent + 3);
				buff.append("\n");
			} else {
				buff.append("|\n");
			}
		}

	}

	/**
	 * Checks if the rectangle <code>itemBoundingBox</code> fits into one of the
	 * child nodes (which must not be existing).
	 * 
	 * @param itemBoundingBox
	 * @return
	 */
	boolean wouldFitIntoChildNode(Rectangle itemBoundingBox) {

		for (Rectangle rect : childBoxes)
			if (boundingBoxContains(rect, itemBoundingBox))
				return true;

		return false;

	}

}
