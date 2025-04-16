package org.json.reflect;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import java.util.ArrayList;
import java.util.Arrays;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.json.JSONException;
import org.json.JSONObject;

/**
* A basic Serializable helper class used with arrays of field names and 
* constructors for a target class. Ignores transient fields by default, changeable by boolean field.
* Entry point is factory method, leaving various fields and array populated.
* @author Jonathan Groff Copyright (C) NeoCoreTechs, Inc. 2025
*/
public class FieldNamesAndConstructors implements Serializable {
	private static final long serialVersionUID = -738720973959363650L;
	private static boolean DEBUG = false;
	public static boolean NOTIFY = true; // notify on field set or reflect fail, for debug
	public static boolean ignoreTransient = true; // process transient fields?
    public transient Class<?> classClass;
    public String className;
	protected transient Field[] fields;
    public ArrayList<String> fieldNames = new ArrayList<String>();
    public transient Class<?>[] fieldTypes;
    public transient Constructor<?>[] constructors;
    public transient Class<?>[][] constructorParamTypes;
    public transient Constructor<?> defaultConstructor;
    public transient Map<Field,FieldNamesAndConstructors> recursedFields;
    
	public FieldNamesAndConstructors() {}
	
	public Field getField(int ifield) {
		return fields[ifield];
	}
	
	public static Map<Field,FieldNamesAndConstructors> getAllSuperclasses(Field field) {
	        Map<Field,FieldNamesAndConstructors> classes = new LinkedHashMap<Field,FieldNamesAndConstructors>();
	        collectClasses(field, classes);
	        return classes;
	} 
	
	private static void collectClasses(Field field, Map<Field,FieldNamesAndConstructors> classes) {
		if (field != null) {
			if(DEBUG) {
				System.out.println("FieldNamesAndConstructors Putting field "+field);
				if(classes.get(field) != null)
					System.out.println("***pre-existing field:"+field);
			}
			classes.put(field, reflectorFieldNamesAndConstructorFactory(field.getType()));
			// Add superclass
			Class<?> superClass = field.getType().getSuperclass();
			if(superClass == null || superClass == java.lang.Object.class)
				return;
			if(DEBUG)
				System.out.println("FieldNAmesAndConstructors Recursing superclass "+superClass);
			Field[] fields = superClass.getDeclaredFields();
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
       	for(int i = 0; i < ctors.length ; i++) {
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
    	for(int i = 0; i < constructorIndex.size(); i++) {
    		Constructor<?> ctor = ctors[constructorIndex.get(i)];
    		fields.constructors[methCnt] = ctor;
    		fields.constructorParamTypes[methCnt++] = ctor.getParameterTypes();
    	}
       	//
      	Field[] fieldz = clazz.getDeclaredFields();
     	for(int i = 0; i < fieldz.length; i++) {
     		Field field = fieldz[i];
      		int fmods = field.getModifiers();
      		if((ignoreTransient && !Modifier.isTransient(fmods)) &&
      			!Modifier.isStatic(fmods) &&
      			!Modifier.isVolatile(fmods) &&
      			!Modifier.isNative(fmods) &&
      			!Modifier.isFinal(fmods) ) {
      			fieldIndex.add(i);
      			if(field.getType() != java.lang.Object.class && !field.getType().equals(clazz)) // prevent cyclic stack overflow
      				fields.recursedFields = getAllSuperclasses(field);
      		}		
      	}
     	//
     	fields.classClass = clazz;
     	fields.className = clazz.getName();
     	fields.fields = new Field[fieldIndex.size()];
     	fields.fieldTypes = new Class[fieldIndex.size()];
       	methCnt = 0;
    	//
    	for(int i = 0; i < fieldIndex.size(); i++) {
    		Field fi = fieldz[fieldIndex.get(i)];
    		fields.fields[methCnt] = fi;
    		fields.fieldTypes[methCnt++] = fi.getType();
    		fields.fieldNames.add(fi.getName());
    	}
    	return fields;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(className, fieldNames);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof FieldNamesAndConstructors)) {
			return false;
		}
		FieldNamesAndConstructors other = (FieldNamesAndConstructors) obj;
		return Objects.equals(className, other.className) && Objects.equals(fieldNames, other.fieldNames);
	}

	/**
	 * Works on established structures after build
	 * @param bean target instance
	 * @return JSONObject of reflected and processed bean
	 * @throws JSONException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public JSONObject reflect(Object bean) throws JSONException {
		JSONObject o2 = new JSONObject();//(JSONObject) JSONObject.wrap(v);
		for(int i = 0; i < fields.length; i++) {
			try {
				fields[i].setAccessible(true);
			} catch(Exception ioe) {
				if(NOTIFY)
					System.out.println("Object "+bean+" exception:"+ioe.getMessage()+" setAccessable failed for field "+fields[i]);
			}
			FieldNamesAndConstructors fnac = recursedFields.get(fields[i]);
			try {
				if(fnac != null)
					o2.put(fieldNames.get(i),fnac.reflect(bean));
			} catch(IllegalArgumentException iae) {
				if(NOTIFY)
					System.out.println("Object "+bean+" exception:"+iae.getMessage()+" reflection failed for recursed field "+fields[i]);
			}
			try {
				o2.put(fieldNames.get(i),fields[i].get(bean));
			} catch(IllegalArgumentException | IllegalAccessException iae) {
				if(NOTIFY) {
					System.out.println("Object "+bean+" exception:"+iae.getMessage()+" get failed for field "+fields[i]);
					iae.printStackTrace();
				}
				try {
					o2.put(fieldNames.get(i), ReflectFieldsAndMethods.invokeAccessorMethod(fields[i], bean, new Object[]{}));
				} catch (Exception e) {
					if(NOTIFY) {
						System.out.println("Object "+bean+" exception:"+e.getMessage()+" accessor failed for field "+fields[i]);
						e.printStackTrace();
					}
				}
			}
		}
		JSONObject o3 = new JSONObject();
		o3.put(bean.getClass().getName(),o2);
		return o3;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(className == null ? "NULL" : className);
		sb.append("\r\n");
		if(defaultConstructor != null) {
			sb.append(defaultConstructor);
			sb.append("\r\n");
		}
		if(constructors != null)
			for(int i = 0; i < constructors.length; i++) {
				sb.append(constructors[i]);
				sb.append(":");
				sb.append(Arrays.toString(constructorParamTypes[i]));
				sb.append("\r\n");
			}
		if(fields != null)
			for(int i = 0; i < fields.length; i++) {
				sb.append(fieldNames.get(i));
				sb.append(":");
				sb.append(fieldTypes[i]);
				sb.append("\r\n");
				FieldNamesAndConstructors fnac = recursedFields.get(fields[i]);
				if(fnac != null) {
					sb.append(fieldNames.get(i));
					sb.append(".)");
					sb.append(fnac.toString());
				}
			}
		return sb.toString();
	}

}
