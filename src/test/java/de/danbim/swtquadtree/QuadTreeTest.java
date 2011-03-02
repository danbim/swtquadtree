package de.danbim.swtquadtree;

import org.eclipse.swt.graphics.Rectangle;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.*;

public class QuadTreeTest {

	private ISWTQuadTree<TestingObject> tree;

	private TestingObject item1;

	private TestingObject item2;

	private TestingObject item3;

	private TestingObject item4;

	private static final int width = (int) Math.pow(2, 10);
	private static final int height = (int) Math.pow(2, 10);

	private static final int upperLeftX = -(width / 2);
	private static final int upperLeftY = -(height / 2);

	private static final int rectangleWidth = 5;
	private static final int rectangleHeight = 5;

	private static final Rectangle upperLeftQuadrant = new Rectangle(upperLeftX, upperLeftY,
			width / 2, height / 2);

	private static final Rectangle upperRightQuadrant = new Rectangle(upperLeftX + (width / 2),
			upperLeftY, width / 2, height / 2);

	private static final Rectangle lowerLeftQuadrant = new Rectangle(upperLeftX, upperLeftY
			+ (height / 2), width / 2, height / 2);

	private static final Rectangle lowerRightQuadrant = new Rectangle(upperLeftX + (width / 2),
			upperLeftY + (height / 2), width / 2, height / 2);

	private void addAllItems() {

		tree.insertItem(item1, item1.box);
		tree.insertItem(item2, item2.box);
		tree.insertItem(item3, item3.box);
		tree.insertItem(item4, item4.box);

	}

	@Before
	public void setUp() throws Exception {

		tree = new ISWTQuadTree.Factory<TestingObject>().create(upperLeftX, upperLeftY, 1024, 16, 1);

		item1 = new TestingObject(new Rectangle(upperLeftQuadrant.x, upperLeftQuadrant.y,
				rectangleWidth, rectangleHeight
		));
		item2 = new TestingObject(new Rectangle(upperRightQuadrant.x, upperRightQuadrant.y,
				rectangleWidth, rectangleHeight
		));
		item3 = new TestingObject(new Rectangle(lowerRightQuadrant.x, lowerRightQuadrant.y,
				rectangleWidth, rectangleHeight
		));
		item4 = new TestingObject(new Rectangle(lowerLeftQuadrant.x, lowerLeftQuadrant.y,
				rectangleWidth, rectangleHeight
		));

	}

	@After
	public void tearDown() throws Exception {

		tree = null;

		item1 = null;
		item2 = null;
		item3 = null;
		item4 = null;

	}

	@Test
	public void testAddOrUpdate() {

		Set<TestingObject> set;
		Rectangle oldBox;

		addAllItems();

		set = tree.searchItems(upperLeftQuadrant);
		assertTrue(set.size() == 1);
		assertTrue(set.contains(item1));

		// move rectangle1 to upper right quadrant
		oldBox = new Rectangle(item1.box.x, item1.box.y, item1.box.width, item1.box.height);
		item1.box.x = upperRightQuadrant.x;
		item1.box.y = upperRightQuadrant.y;
		tree.moveItem(item1, oldBox, item1.box);

		set = tree.searchItems(upperLeftQuadrant);
		assertTrue(set.size() == 0);

		set = tree.searchItems(upperRightQuadrant);
		assertTrue(set.size() == 2);
		assertTrue(set.contains(item1));
		assertTrue(set.contains(item2));

		// move rectangle1 to lower right
		oldBox = new Rectangle(item1.box.x, item1.box.y, item1.box.width, item1.box.height);
		item1.box.x = lowerRightQuadrant.x;
		item1.box.y = lowerRightQuadrant.y;
		tree.moveItem(item1, oldBox, item1.box);

		set = tree.searchItems(upperRightQuadrant);
		assertTrue(set.size() == 1);
		assertTrue(set.contains(item2));

		set = tree.searchItems(lowerRightQuadrant);
		assertTrue(set.size() == 2);
		assertTrue(set.contains(item1));
		assertTrue(set.contains(item3));

		// move rectangle1 to lower left
		oldBox = new Rectangle(item1.box.x, item1.box.y, item1.box.width, item1.box.height);
		item1.box.x = lowerLeftQuadrant.x;
		item1.box.y = lowerLeftQuadrant.y;
		tree.moveItem(item1, oldBox, item1.box);

		set = tree.searchItems(lowerRightQuadrant);
		assertTrue(set.size() == 1);
		assertTrue(set.contains(item3));

		set = tree.searchItems(lowerLeftQuadrant);
		assertTrue(set.size() == 2);
		assertTrue(set.contains(item1));
		assertTrue(set.contains(item4));

		// move rectangle1 to upper left
		oldBox = new Rectangle(item1.box.x, item1.box.y, item1.box.width, item1.box.height);
		item1.box.x = upperLeftQuadrant.x;
		item1.box.y = upperLeftQuadrant.y;
		tree.moveItem(item1, oldBox, item1.box);

		set = tree.searchItems(lowerLeftQuadrant);
		assertTrue(set.size() == 1);
		assertTrue(set.contains(item4));

		set = tree.searchItems(upperLeftQuadrant);
		assertTrue(set.size() == 1);
		assertTrue(set.contains(item1));

	}

	@Test
	public void testClear() {
		addAllItems();
		assertTrue(tree.getItemCount() == 4);
		assertTrue(tree.searchItems().size() == 4);
		tree.clear();
		assertTrue(tree.getItemCount() == 0);
		assertTrue(tree.searchItems().size() == 0);
	}

	@Test
	public void testContains() {

		addAllItems();

		assertTrue(tree.containsItem(item1, item1.box));
		assertTrue(tree.containsItem(item2, item2.box));
		assertTrue(tree.containsItem(item3, item3.box));
		assertTrue(tree.containsItem(item4, item4.box));

	}

	@Test
	public void testSearchItemsRectangle() {

		addAllItems();

		Set<TestingObject> items;

		// get all entities from entire box
		items = tree.searchItems(new Rectangle(upperLeftX, upperLeftY, width, height));
		assertTrue(items.contains(item1));
		assertTrue(items.contains(item2));
		assertTrue(items.contains(item3));
		assertTrue(items.contains(item4));

		// get all entities from upper left quadrant
		items = tree.searchItems(upperLeftQuadrant);
		assertTrue(items.size() == 1);
		assertTrue(items.contains(item1));

		// get all entities from upper right quadrant
		items = tree.searchItems(upperRightQuadrant);
		assertTrue(items.size() == 1);
		assertTrue(items.contains(item2));

		// get all entities from lower right quadrant
		items = tree.searchItems(lowerRightQuadrant);
		assertTrue(items.size() == 1);
		assertTrue(items.contains(item3));

		// get all entities from lower left quadrant
		items = tree.searchItems(lowerLeftQuadrant);
		assertTrue(items.size() == 1);
		assertTrue(items.contains(item4));

	}

	@Test
	public void testSearchItems() {

		addAllItems();
		final Set<TestingObject> list = tree.searchItems();
		assertTrue(list.size() == 4);
		assertTrue(list.contains(item1));
		assertTrue(list.contains(item2));
		assertTrue(list.contains(item3));
		assertTrue(list.contains(item4));

	}

	@Test
	public void testGetItemCount() {

		assertSame(0, tree.getItemCount());

		tree.insertItem(item1, item1.box);
		assertSame(1, tree.getItemCount());

		tree.insertItem(item2, item2.box);
		assertSame(2, tree.getItemCount());

		tree.insertItem(item3, item3.box);
		assertSame(3, tree.getItemCount());

		tree.insertItem(item4, item4.box);
		assertSame(4, tree.getItemCount());

		tree.removeItem(item1, item1.box);
		assertSame(3, tree.getItemCount());

		tree.removeItem(item2, item2.box);
		assertSame(2, tree.getItemCount());

		tree.removeItem(item3, item3.box);
		assertSame(1, tree.getItemCount());

		tree.removeItem(item4, item4.box);
		assertSame(0, tree.getItemCount());

	}

	@Test
	public void testGetObjectsRecursive() {

		addAllItems();

		final Set<TestingObject> list = tree.searchItems();

		assertTrue(list.contains(item1));
		assertTrue(list.contains(item2));
		assertTrue(list.contains(item3));
		assertTrue(list.contains(item4));

	}

	@Test
	public void testInsertItem() {

		tree.insertItem(item1, item1.box);
		assertTrue(tree.containsItem(item1, item1.box));

		tree.insertItem(item2, item2.box);
		assertTrue(tree.containsItem(item1, item1.box));
		assertTrue(tree.containsItem(item2, item2.box));

		tree.insertItem(item3, item3.box);
		assertTrue(tree.containsItem(item1, item1.box));
		assertTrue(tree.containsItem(item2, item2.box));
		assertTrue(tree.containsItem(item3, item3.box));

		tree.insertItem(item4, item4.box);
		assertTrue(tree.containsItem(item1, item1.box));
		assertTrue(tree.containsItem(item2, item2.box));
		assertTrue(tree.containsItem(item3, item3.box));
		assertTrue(tree.containsItem(item4, item4.box));

	}

	@Test
	public void testRemove() {

		addAllItems();

		tree.removeItem(item1, item1.box);
		assertFalse(tree.containsItem(item1, item1.box));
		assertTrue(tree.containsItem(item2, item2.box));
		assertTrue(tree.containsItem(item3, item3.box));
		assertTrue(tree.containsItem(item4, item4.box));

		tree.removeItem(item2, item2.box);
		assertFalse(tree.containsItem(item1, item1.box));
		assertFalse(tree.containsItem(item2, item2.box));
		assertTrue(tree.containsItem(item3, item3.box));
		assertTrue(tree.containsItem(item4, item4.box));

		tree.removeItem(item3, item3.box);
		assertFalse(tree.containsItem(item1, item1.box));
		assertFalse(tree.containsItem(item2, item2.box));
		assertFalse(tree.containsItem(item3, item3.box));
		assertTrue(tree.containsItem(item4, item4.box));

		tree.removeItem(item4, item4.box);
		assertFalse(tree.containsItem(item1, item1.box));
		assertFalse(tree.containsItem(item2, item2.box));
		assertFalse(tree.containsItem(item3, item3.box));
		assertFalse(tree.containsItem(item4, item4.box));

	}

	/**
	 * Tests if items that lie exactly on the outer border line of the QuadTrees
	 * bounding box work.
	 * 
	 * @see <a href="http://danbim.de/swtquadtree/ticket/1">http://danbim.de/swtquadtree/ticket/1</a>
	 */
	@Test
	public void borderTest() {

		Rectangle rectangleBorderLeft 	= new Rectangle(upperLeftX, upperLeftY + (height /2), rectangleWidth,
				rectangleHeight
		);
		Rectangle rectangleBorderTop 	= new Rectangle(upperLeftX + (width / 2), upperLeftY, rectangleWidth,
				rectangleHeight
		);
		Rectangle rectangleBorderRight 	= new Rectangle(upperLeftX + width - rectangleWidth, upperLeftY + (height / 2),
				rectangleWidth, rectangleHeight
		);
		Rectangle rectangleBorderBottom 	= new Rectangle(upperLeftX + (width / 2), upperLeftY + height - rectangleHeight,
				rectangleWidth, rectangleHeight
		);
		
		TestingObject toBorderLeft 	= new TestingObject(rectangleBorderLeft);
		TestingObject toBorderTop 	= new TestingObject(rectangleBorderTop);
		TestingObject toBorderRight	= new TestingObject(rectangleBorderRight);
		TestingObject toBorderBottom = new TestingObject(rectangleBorderBottom);
		
		try {
			tree.insertItem(toBorderLeft, toBorderLeft.box);
		} catch (RuntimeException e) {
			assertTrue(false);
		}
		
		try {
			tree.insertItem(toBorderTop, toBorderTop.box);
		} catch (RuntimeException e) {
			assertTrue(false);
		}
		
		try {
			tree.insertItem(toBorderBottom, toBorderBottom.box);
		} catch (RuntimeException e) {
			assertTrue(e.getMessage(), false);
		}
		
		try {
			tree.insertItem(toBorderRight, toBorderRight.box);
		} catch (RuntimeException e) {
			assertTrue(false);
		}
		
	}

}