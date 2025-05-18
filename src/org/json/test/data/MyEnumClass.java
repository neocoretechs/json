package org.json.test.data;

import java.io.Serializable;

/**
 * this is simply a class that contains some enum instances
 */
public class MyEnumClass implements Serializable{
    public MyEnumClass() {
		super();
	}
	@Override
	public String toString() {
		return "MyEnumClass [myEnum=" + myEnum + ", myEnumField=" + myEnumField + "]";
	}
	private MyEnum myEnum;
    private MyEnumField myEnumField;

    public MyEnum getMyEnum() {
        return this.myEnum;
    }
    public void setMyEnum(MyEnum myEnum) {
        this.myEnum = myEnum;
    }
    public MyEnumField getMyEnumField() {
        return this.myEnumField;
    }
    public void setMyEnumField(MyEnumField myEnumField) {
        this.myEnumField = myEnumField;
    }
}
