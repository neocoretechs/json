package org.json.test.data;

import java.io.Serializable;
import java.util.*;

/**
 * A resource bundle class
 */
public class StringsResourceBundle extends ListResourceBundle implements Serializable{
    public StringsResourceBundle() {
		super();
	}
	@Override
    public Object[][] getContents() {
        return contents;
    }
    static final Object[][] contents = {
        {"greetings.hello", "Hello, "},
        {"greetings.world", "World!"},
        {"farewells.later", "Later, "},
        {"farewells.gator", "Alligator!"}
    };
}