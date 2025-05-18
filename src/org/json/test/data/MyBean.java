package org.json.test.data;

import java.io.*;

/**
 * Used in testing when Bean behavior is needed
 */
public interface MyBean extends Serializable {
    public Integer getIntKey();
    public Double getDoubleKey();
    public String getStringKey();
    public String getEscapeStringKey();
    public Boolean isTrueKey();
    public Boolean isFalseKey();
    public StringReader getStringReaderKey();
}