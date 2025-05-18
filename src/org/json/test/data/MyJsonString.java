package org.json.test.data;

import java.io.Serializable;

import org.json.*;

/**
 * Used in testing when a JSONString is needed
 */
public class MyJsonString implements JSONString, Serializable {

    public MyJsonString() {
		super();
	}

	@Override
    public String toJSONString() {
        return "my string";
    }
}