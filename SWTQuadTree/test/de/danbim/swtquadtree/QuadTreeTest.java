package de.danbim.swtquadtree;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.eclipse.swt.graphics.Rectangle;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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

	private static final int rectWidth = 5;
	private static final int rectHeight = 5;

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

		tree = new ISWTQuadTree.Factory<TestingObject>()
				.create(upperLeftX, upperLeftY, 1024, 16, 1);

		item1 = new TestingObject(new Rectangle(upperLeftQuadrant.x, upperLeftQuadrant.y,
				rectWidth, rectHeight));
		item2 = new TestingObject(new Rectangle(upperRightQuadrant.x, upperRightQuadrant.y,
				rectWidth, rectHeight));
		item3 = new TestingObject(new Rectangle(lowerRightQuadrant.x, lowerRightQuadrant.y,
				rectWidth, rectHeight));
		item4 = new TestingObject(new Rectangle(lowerLeftQuadrant.x, lowerLeftQuadrant.y,
				rectWidth, rectHeight));

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

		// move rect1 to upper right quadrant
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

		// move rect1 to lower right
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

		// move rect1 to lower left
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

		// move rect1 to upper left
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

		Rectangle rectBorderLeft 	= new Rectangle(upperLeftX, upperLeftY + (height /2), rectWidth, rectHeight);
		Rectangle rectBorderTop 	= new Rectangle(upperLeftX + (width / 2), upperLeftY, rectWidth, rectHeight);
		Rectangle rectBorderRight 	= new Rectangle(upperLeftX + width, upperLeftY + (height / 2), rectWidth, rectHeight);
		Rectangle rectBorderBottom 	= new Rectangle(upperLeftX + (width / 2), upperLeftY + height, rectWidth, rectHeight);
		
		TestingObject toBorderLeft 	= new TestingObject(rectBorderLeft);
		TestingObject toBorderTop 	= new TestingObject(rectBorderTop);
		TestingObject toBorderRight	= new TestingObject(rectBorderRight);
		TestingObject toBorderBottom = new TestingObject(rectBorderBottom);
		
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
			assertTrue(false);
		}
		
		try {
			tree.insertItem(toBorderRight, toBorderRight.box);
		} catch (RuntimeException e) {
			assertTrue(false);
		}
		
	}

	@Test
	public void manyItemsTest() {

		// generate 100 items and insert them
		Random rand = new Random();
		int itemX, itemY;
		Rectangle boundingBox;
		TestingObject item;
		List<TestingObject> list = new ArrayList<TestingObject>();

		for (int i = 0; i < 100; i++) {

			itemX = Math.abs(rand.nextInt() % width) + upperLeftX;
			itemY = Math.abs(rand.nextInt() % height) + upperLeftY;

			boundingBox = new Rectangle(itemX, itemY, rectWidth, rectHeight);
			item = new TestingObject(boundingBox);
			list.add(item);

			tree.insertItem(item, boundingBox);

		}

		// check if they are inside
		for (TestingObject to : list) {
			assertTrue(tree.containsItem(to, to.box));
		}

		for (TestingObject to : list) {

			itemX = Math.abs(rand.nextInt() % width) + upperLeftX;
			itemY = Math.abs(rand.nextInt() % height) + upperLeftY;

			boundingBox = to.box;
			to.box = new Rectangle(itemX, itemY, rectWidth, rectHeight);

			tree.moveItem(to, boundingBox, to.box);

			assertTrue(tree.searchItems(to.box).contains(to));
			assertTrue(to.box + " was not found", tree.containsItem(to, to.box));

		}

		// remove 10 items per time and check if the rest is still inside
		for (int i = 0; i < list.size(); i++) {

			tree.removeItem(list.get(i), list.get(i).box);

			if (i % 10 == 0) {

				// check if the first i elements have been removed
				for (int k = 0; k < i; k++)
					assertFalse(list.get(k).box + " konnte nicht gefunden werden", tree
							.containsItem(list.get(k), list.get(k).box));

				// check if the other elements are still inside
				for (int k = i + 1; k < list.size(); k++)
					assertTrue(list.get(k).box + " konnte nicht gefunden werden", tree
							.containsItem(list.get(k), list.get(k).box));

			}

		}

		System.out.println(tree);

	}

}