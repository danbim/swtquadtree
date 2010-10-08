package de.danbim.swtquadtree;

import org.eclipse.swt.graphics.Rectangle;

class TestingObject {

	private static int lastTestingObjectID = 0;
	
	public int id;

	public Rectangle box;

	public TestingObject(Rectangle box) {
		this.id = ++lastTestingObjectID;
		this.box = box;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TestingObject other = (TestingObject) obj;
		if (id != other.id)
			return false;
		return true;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public String toString() {
		return "" + id;
	}

}