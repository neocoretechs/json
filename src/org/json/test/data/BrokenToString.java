package org.json.test.data;

import java.io.Serializable;

/**
 * test class for verifying write errors.
 * @author John Aylward
 *
 */
public class BrokenToString implements Serializable {
	private static final long serialVersionUID = 1L;
	public BrokenToString() {}
	@Override
    public String toString() {
        throw new IllegalStateException("Something went horribly wrong!");
    }
}