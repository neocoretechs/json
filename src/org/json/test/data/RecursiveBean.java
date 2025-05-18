package org.json.test.data;

import java.io.Serializable;

/**
 * test class for verifying if recursively defined bean can be correctly identified
 * @author Zetmas
 *
 */
public class RecursiveBean implements Serializable{
    private String name;
    public RecursiveBean() {
		super();
	}

	@Override
	public String toString() {
		return "RecursiveBean [name=" + name + ", reference=" + reference + ", reference2=" + reference2 + "]";
	}

	private Object reference;
    private Object reference2;
    public String getName() { return name; }
    public Object getRef() {return reference;}
    public Object getRef2() {return reference2;}
    public void setRef(Object refObj) {reference = refObj;}
    public void setRef2(Object refObj) {reference2 = refObj;}

    public RecursiveBean(String name) {
        this.name = name;
        reference = null;
        reference2 = null;
    }
}