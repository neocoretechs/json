package org.json.reflect;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
* A basic Serializable helper class used with arrays of field names and 
* constructors for a target class.
* @author Jonathan Groff Copyright (C) NeoCoreTechs, Inc. 2025
*/
public class FieldNamesAndConstructors implements Serializable {
	private static final long serialVersionUID = -738720973959363650L;
	private static boolean DEBUG = true;
	
    public transient Class<?> classClass;
    public String className;
	protected transient Field[] fields;
    public ArrayList<String> fieldNames = new ArrayList<String>();
    public transient Class<?>[] fieldTypes;
    public transient Constructor<?>[] constructors;
    public transient Class<?>[][] constructorParamTypes;
    public transient Constructor<?> defaultConstructor;
    public transient ArrayList<Map<Field,FieldNamesAndConstructors>> recursedFields = new ArrayList<Map<Field,FieldNamesAndConstructors>>();
    
	public FieldNamesAndConstructors() {}
	
	public static Map<Field,FieldNamesAndConstructors> getAllSuperclasses(Field field) {
	        Map<Field,FieldNamesAndConstructors> classes = new LinkedHashMap<Field,FieldNamesAndConstructors>();
	        collectClasses(field, classes);
	        return classes;
	} 
	
	private static void collectClasses(Field field, Map<Field,FieldNamesAndConstructors> classes) {
	        if (field != null) {
	        	if(DEBUG)
	        		System.out.println("FieldNamesAndConstructors Putting field "+field);
	            classes.put(field, reflectorFieldNamesAndConstructorFactory(field.getType()));
	            // Add superclass
	            Class<?> superClass = field.getType().getSuperclass();
	            if(superClass == null || superClass == java.lang.Object.class)
	            	return;
	            if(DEBUG)
	            	System.out.println("FieldNAmesAndConstructors Recursing superclass "+superClass);
	            Field[] fields = superClass.getFields();
	            for(Field nextField: fields) {
	            	if(!nextField.getType().isPrimitive())
	            		collectClasses(nextField, classes);
	            }
	        }
	}
	/**
	 * Collect default constructor and other constructors NOT private, fields defined as not transient, not static
	 * not volatile, native or final.
	 * @param clazz target class
	 * @return this populated
	 */
	public static FieldNamesAndConstructors reflectorFieldNamesAndConstructorFactory(Class<?> clazz) {
		if(DEBUG)
			System.out.println("FieldNamesAndConstructors.reflectorFieldNamesAndConstructorFactory class:"+clazz);
	  	FieldNamesAndConstructors fields = new FieldNamesAndConstructors();
  	  	fields.classClass = clazz;
    	fields.className = clazz.getName();
    	// process fields
      	ArrayList<Integer> fieldIndex = new ArrayList<Integer>();
       	ArrayList<Integer> constructorIndex = new ArrayList<Integer>();
     	Constructor<?>[] ctors = clazz.getConstructors();
       	for(int i = ctors.length-1; i >= 0 ; i--) {
       		int cmods = ctors[i].getModifiers();
       		if(!Modifier.isPrivate(cmods)) {
       			if(ctors[i].getParameterCount() == 0)
       				fields.defaultConstructor = ctors[i];
       			else
       				constructorIndex.add(i);
       		}
       	}
       	fields.constructors = new Constructor<?>[constructorIndex.size()];
       	fields.constructorParamTypes = new Class[constructorIndex.size()][];
     	int methCnt = 0;
    	//
    	for(int i = constructorIndex.size()-1; i >= 0 ; i--) {
    		Constructor<?> ctor = ctors[constructorIndex.get(i)];
    		fields.constructors[methCnt] = ctor;
    		fields.constructorParamTypes[methCnt++] = ctor.getParameterTypes();
    	}
       	//
      	Field[] fieldz = clazz.getDeclaredFields();
     	for(int i = fieldz.length-1; i >= 0 ; i--) {
     		Field field = fieldz[i];
      		int fmods = field.getModifiers();
      		if(!Modifier.isTransient(fmods) &&
      			!Modifier.isStatic(fmods) &&
      			!Modifier.isVolatile(fmods) &&
      			!Modifier.isNative(fmods) &&
      			!Modifier.isFinal(fmods) ) {
      			fieldIndex.add(i);
      			if(field.getType() != java.lang.Object.class && !field.getType().equals(clazz)) // prevent cyclic stack overflow
      				fields.recursedFields.add(getAllSuperclasses(field));
      		}		
      	}
     	//
     	fields.classClass = clazz;
     	fields.className = clazz.getName();
     	fields.fields = new Field[fieldIndex.size()];
     	fields.fieldTypes = new Class[fieldIndex.size()];
       	methCnt = 0;
    	//
    	for(int i = fieldIndex.size()-1; i >= 0 ; i--) {
    		Field fi = fieldz[fieldIndex.get(i)];
    		fields.fields[methCnt] = fi;
    		fields.fieldTypes[methCnt++] = fi.getType();
    		fields.fieldNames.add(fi.getName());
    	}
    	return fields;
	}

}
