package de.danbim.swtquadtree;

import java.util.List;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import de.danbim.swtquadtree.SWTQuadTree.Entry;

class QuadTreeDrawingTest {

	private TestingObject selectedObject = null;
	
	private Text text;

	private SWTQuadTree<TestingObject> tree;

	public QuadTreeDrawingTest(SWTQuadTree<TestingObject> tree) {
		this.tree = tree;
	}
	
	void paintTree(final GC gc, final SWTQuadTree<TestingObject> node, final TestingObject selected) {
		if (!node.leaf) {
			for (final SWTQuadTree<TestingObject> child : node.children)
				if (child != null)
					paintTree(gc, child, selected);
		}
		gc.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
		gc.drawRectangle(node.boundingBox);
		if (paintItemsInNode(gc, node.objects, selected) || paintItemsInNode(gc, node.overflows, selected)) {
			gc.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLUE));
			gc.drawRectangle(node.boundingBox);
		}
	}
	
	boolean paintItemsInNode(final GC gc, final List<Entry<TestingObject>> itemList, final TestingObject selected) {
		boolean containedSelected = false;
		for (final Entry<TestingObject> entry : itemList) {
			if (entry.item == selected)
				containedSelected = true;
			gc.setForeground(Display.getDefault().getSystemColor(entry.item == selected ? SWT.COLOR_YELLOW : SWT.COLOR_GREEN));
			final org.eclipse.swt.graphics.Rectangle rect = entry.item.box;
			gc.drawRectangle(rect);
			gc.drawString(entry.item.toString() + "{"+entry.boundingBox.toString()+"}", rect.x + rect.width + 2, rect.y);
		}
		return containedSelected;
	}

	private Shell createPaintShell(final Display display, final String msg) {

		final Shell paintShell = new Shell(display);
		paintShell.setBounds(10, 10, 1000, 800);
		final int transOffset = Math.abs(tree.boundingBox.x);
		final Canvas paintCanvas = new Canvas(paintShell, SWT.DOUBLE_BUFFERED);
		paintCanvas.setSize(1000, 800);

		paintCanvas.addPaintListener(new PaintListener() {
			public void paintControl(final PaintEvent event) {

				final Transform transform = new Transform(display);

				event.gc.setBackground(display.getSystemColor(SWT.COLOR_BLACK));
				event.gc.fillRectangle(paintCanvas.getBounds());

				transform.translate(transOffset, transOffset);
				event.gc.setTransform(transform);
				
				paintTree(event.gc, tree, selectedObject);

				event.gc.drawString(msg, 10 - transOffset, 10 - transOffset);

				transform.dispose();

			}
		});

		paintCanvas.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(final MouseEvent e) {
				try {
					if (e.button == 1) {
						final Rectangle rect = new Rectangle(e.x - transOffset, e.y - transOffset, 10, 10);
						TestingObject dob = new TestingObject(rect);
						tree.insertItem(dob, rect);
						selectedObject = dob;
						text.setText(tree.toString());
						paintCanvas.redraw();
					} else if (e.button == 3) {
						final Set<TestingObject> search = tree.searchItems(new Rectangle(e.x - transOffset, e.y - transOffset, 1, 1));
						if (search.size() == 1) {
							selectedObject = search.iterator().next();
							text.setText(tree.toString());
							paintCanvas.redraw();
						}
					}
				} catch (RuntimeException exc) {
					System.err.println(exc);
				}
			}
		});

		paintCanvas.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(final KeyEvent e) {
				try {
					if (selectedObject != null) {
						int offsetX = 0, offsetY = 0;
						final boolean isLeft = e.keyCode == SWT.ARROW_LEFT;
						final boolean isRight = e.keyCode == SWT.ARROW_RIGHT;
						final boolean isDown = e.keyCode == SWT.ARROW_DOWN;
						final boolean isUp = e.keyCode == SWT.ARROW_UP;
						final boolean isDel = e.keyCode == SWT.DEL;
						if (isLeft) {
							offsetX = -10;
						} else if (isRight) {
							offsetX = 10;
						} else if (isDown) {
							offsetY = 10;
						} else if (isUp) {
							offsetY = -10;
						}
						if (isLeft || isRight || isUp || isDown) {
							final Rectangle oldBox = selectedObject.box;
							selectedObject.box = new Rectangle(oldBox.x + offsetX, oldBox.y + offsetY, oldBox.width, oldBox.height);
							tree.moveItem(selectedObject, oldBox, selectedObject.box);
							text.setText(tree.toString());
							paintCanvas.redraw();
						} else if (isDel) {
							tree.clear();
							paintCanvas.redraw();
						}
					}
				} catch (RuntimeException exc) {
					System.err.println(exc);
				}
			}
		});

		return paintShell;
	}

	private Shell createStringShell(final Display display) {

		final Shell stringShell = new Shell(display);
		stringShell.setBounds(1000, 10, 300, 800);
		text = new org.eclipse.swt.widgets.Text(stringShell, SWT.MULTI);
		text.setText(tree.toString());
		text.setSize(stringShell.getSize());
		return stringShell;

	}

	public void run(final String msg) {

		final Display display = new Display();

		final Shell paintShell = createPaintShell(display, msg);
		final Shell stringShell = createStringShell(display);

		paintShell.open();
		stringShell.open();
		while (!paintShell.isDisposed() && !stringShell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		stringShell.dispose();
		display.dispose();
	}
	
	public static final void main(String args[]) {
		SWTQuadTree<TestingObject> tree = new SWTQuadTree<TestingObject>(-512, -512, 1024, 16, 1);
		new QuadTreeDrawingTest(tree).run("Testing");
	}
}
