package org.json.test.data;

import java.io.Serializable;

/**
 * Class that holds our MyNumber override as a property.
 * @author John Aylward
 */
public class MyNumberContainer implements Serializable{
    public MyNumberContainer() {
		super();
	}
	private MyNumber myNumber = new MyNumber();
    /**
     * @return a MyNumber.
     */
    public Number getMyNumber() {return this.myNumber;}
}
