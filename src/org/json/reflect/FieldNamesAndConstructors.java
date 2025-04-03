package org.json.reflect;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;

public class FieldNamesAndConstructors implements Serializable {
	private static final long serialVersionUID = -738720973959363650L;
	
    public transient Class<?> classClass;
    public String className;
	protected transient Field[] fields;
    public ArrayList<String> fieldNames = new ArrayList<String>();
    public transient Class<?>[] fieldTypes;
    public transient Constructor<?>[] constructors;
    public transient Class<?>[][] constructorParamTypes;
    public transient Constructor<?> defaultConstructor;
	public FieldNamesAndConstructors() {}

}
