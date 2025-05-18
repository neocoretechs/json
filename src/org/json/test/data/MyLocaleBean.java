package org.json.test.data;

import java.io.Serializable;

public class MyLocaleBean implements Serializable{
    public MyLocaleBean() {
		super();
	}
	private final String id = "beanId";
    private final String i = "beanI";
    public String getId() {
        return this.id;
    }
    public String getI() {
        return this.i;
    }
}
