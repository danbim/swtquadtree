package de.danbim.swtquadtree;

import org.eclipse.swt.graphics.Rectangle;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class QuadTreePerformanceTest {

	private ISWTQuadTree<TestingObject> tree;

	private static final int width = (int) Math.pow(2, 10);
	private static final int height = (int) Math.pow(2, 10);

	private static final int upperLeftX = -(width / 2);
	private static final int upperLeftY = -(height / 2);

	private static final int rectWidth = 5;
	private static final int rectHeight = 5;

	@Before
	public void setUp() throws Exception {
		tree = new ISWTQuadTree.Factory<TestingObject>().create(upperLeftX, upperLeftY, 1024, 16, 1);
	}

	@After
	public void tearDown() throws Exception {
		tree = null;
	}
	
	@Test
	public void manyItemsTestLarge() {
		manyItemsTestInternal(width, height);
	}
	
	@Test
	public void manyItemsTestSmall() {
		manyItemsTestInternal(100, 100);
	}

	private void manyItemsTestInternal(final int testWidth, final int testHeight) {

		// generate 100 items and insert them
		int objectCnt = 5000;
		int moveCnt = 5;
		Random rand = new Random();
		int itemX, itemY;
		Rectangle boundingBox;
		TestingObject item;
		List<TestingObject> list = new ArrayList<TestingObject>();

		for (int i = 0; i < objectCnt; i++) {

			itemX = Math.abs(rand.nextInt() % testWidth) + upperLeftX;
			itemY = Math.abs(rand.nextInt() % testHeight) + upperLeftY;

			boundingBox = new Rectangle(itemX, itemY, rectWidth, rectHeight);
			item = new TestingObject(boundingBox);
			list.add(item);

			tree.insertItem(item, boundingBox);

		}

		// check if they are inside
		for (TestingObject to : list) {
			assertTrue(tree.containsItem(to, to.box));
		}
		
		System.out.println("All objects are inside. Now randomly moving every of the " + objectCnt + " objects " + moveCnt + " times");

		long startMovement = System.currentTimeMillis();
		for (int i=0; i<moveCnt; i++) {
			for (TestingObject to : list) {
	
				itemX = Math.abs(rand.nextInt() % testWidth) + upperLeftX;
				itemY = Math.abs(rand.nextInt() % testHeight) + upperLeftY;
	
				boundingBox = to.box;
				to.box = new Rectangle(itemX, itemY, rectWidth, rectHeight);
	
				tree.moveItem(to, boundingBox, to.box);
	
				assertTrue(tree.searchItems(to.box).contains(to));
				assertTrue(to.box + " was not found", tree.containsItem(to, to.box));
	
			}
		}

		System.out.println("Moving took " + (System.currentTimeMillis()-startMovement) + " ms.");

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