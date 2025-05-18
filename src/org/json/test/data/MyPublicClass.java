package org.json.test.data;

import java.io.Serializable;

/**
 * Need a class with some public data members for testing
 */
@SuppressWarnings("boxing")
public class MyPublicClass implements Serializable{
    public MyPublicClass() {
		super();
	}
	@Override
	public String toString() {
		return "MyPublicClass [publicInt=" + publicInt + ", publicString=" + publicString + "]";
	}
	public Integer publicInt = 42;
    public String publicString = "abc";
}
