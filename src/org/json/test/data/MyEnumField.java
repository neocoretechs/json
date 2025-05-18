package org.json.test.data;

import java.io.Serializable;

/**
 * An enum that contains getters and some internal fields
 */
@SuppressWarnings("boxing")
public enum MyEnumField implements Serializable{
    VAL1(1, "val 1"),
    VAL2(2, "val 2"),
    VAL3(3, "val 3");

    private String value;
    private Integer intVal;
    private MyEnumField(Integer intVal, String value) {
        this.value = value;
        this.intVal = intVal;
    }
    MyEnumField() {}
    public String getValue() {
        return this.value;
    }
    public Integer getIntVal() {
        return this.intVal;
    }
    @Override
    public String toString(){
        return this.value;
    }
}
