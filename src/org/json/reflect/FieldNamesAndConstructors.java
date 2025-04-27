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
* The declared fields, and all superclass fields will be included. The reference will be to the base class however.
* @author Jonathan Groff Copyright (C) NeoCoreTechs, Inc. 2025
*/
public class FieldNamesAndConstructors implements Serializable {
	private static final long serialVersionUID = -738720973959363650L;
	private static boolean DEBUG = false;
	public static boolean NOTIFY = false; // notify on field set or reflect fail, for debug
	public static boolean ignoreTransient = true; // process transient fields?
    public transient Class<?> classClass;
    public String className;
	protected transient Field[] fields;
    public ArrayList<String> fieldNames = new ArrayList<String>();
    public transient Class<?>[] fieldTypes;
    public transient Constructor<?>[] constructors;
    public transient Class<?>[][] constructorParamTypes;
    public transient Constructor<?> defaultConstructor;
    public static Map<Field,FieldNamesAndConstructors> recursedFields = new LinkedHashMap<Field,FieldNamesAndConstructors>();
    public static Map<Class<?>,FieldNamesAndConstructors> allClasses = new LinkedHashMap<Class<?>,FieldNamesAndConstructors>();
    
	public FieldNamesAndConstructors() {}
	
	public FieldNamesAndConstructors(Class<?> clazz) {
		this.classClass = clazz;
		this.className = clazz.getName();
	}
	
	/**
	 * Main entry point
	 * @param clazz target class
	 * @return retrieved or processed FieldNamesAndConstructors
	 */
	public static FieldNamesAndConstructors getFieldNamesAndConstructors(Class<?> clazz) {
		FieldNamesAndConstructors fields = allClasses.get(clazz);
		if(fields != null) 
			return fields;
		fields = new FieldNamesAndConstructors(clazz);
		allClasses.put(clazz, reflectorFieldFactory(fields));
		return fields;
	}
	
	public Field getField(int ifield) {
		return fields[ifield];
	}

	/**
	 * Collect default constructor and other constructors NOT private, fields defined as not transient, not static
	 * not volatile, native or final.
	 * @param clazz target class
	 * @return this populated
	 */
	private static FieldNamesAndConstructors reflectorFieldFactory(FieldNamesAndConstructors fields) {
		if(DEBUG)
			System.out.println("FieldNamesAndConstructors.reflectorFieldFactory class:"+fields.classClass);
    	// process fields
		Class<?> clazz = fields.classClass;
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
    	ArrayList<Field> allFields = new ArrayList<Field>();
    	Class<?> rClass = clazz;
    	for(;;) {
    		if(rClass == null)
    			break;
    		Field[] fieldz = rClass.getDeclaredFields();
    		for(int i = 0; i < fieldz.length; i++) {
    			Field field = fieldz[i];
    			allFields.add(field);
    			int fmods = field.getModifiers();
    			if((ignoreTransient && !Modifier.isTransient(fmods)) &&
    					!Modifier.isStatic(fmods) &&
    					!Modifier.isVolatile(fmods) &&
    					!Modifier.isNative(fmods) &&
    					!Modifier.isFinal(fmods) ) {
    				fieldIndex.add(allFields.size()-1);
    				//if(field.getType() != java.lang.Object.class && !field.getType().equals(clazz)) // prevent cyclic stack overflow
    				//	fields.recursedFields = getAllSuperclasses(field);
    			}
    		}
    		rClass = rClass.getSuperclass();
    	}
     	//
     	fields.classClass = clazz;
     	fields.className = clazz.getName();
     	fields.fields = new Field[fieldIndex.size()];
     	fields.fieldTypes = new Class[fieldIndex.size()];
       	methCnt = 0;
    	//
    	for(int i = 0; i < fieldIndex.size(); i++) {
    		Field fi = allFields.get(fieldIndex.get(i));
    		recursedFields.put(fi,  fields);
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
		return className.equals(other.className);
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
				if(DEBUG || NOTIFY)
					System.out.println("Object "+bean+" exception:"+ioe.getMessage()+" setAccessable failed for field "+fields[i]);
			}
			try {
				Object ob = fields[i].get(bean);
				 //Package objectPackage = ob.getClass().getPackage();
		            //String objectPackageName = objectPackage != null ? objectPackage.getName() : "";
				if(ob != null && !ob.getClass().isPrimitive()) {//&& 
				    //!(objectPackageName.startsWith("java.") || objectPackageName.startsWith("javax.") || ob.getClass().getClassLoader() == null) ) {
					FieldNamesAndConstructors fnac = getFieldNamesAndConstructors(ob.getClass());
					fnac.reflect(ob);
				}
				o2.put(fieldNames.get(i),ob);
				if(DEBUG)
					System.out.println("Object put "+fieldNames.get(i)+" for field "+fields[i]+" json"+o2);
			} catch(IllegalArgumentException | IllegalAccessException iae) {
				if(DEBUG || NOTIFY) {
					System.out.println("Object "+bean+" exception:"+iae.getMessage()+" get failed for field "+fields[i]);
					iae.printStackTrace();
				}
				try {
					o2.put(fieldNames.get(i), ReflectFieldsAndMethods.invokeAccessorMethod(fields[i], bean, new Object[]{}));
				} catch (Exception e) {
					if(DEBUG || NOTIFY) {
						System.out.println("Object "+bean+" exception:"+e.getMessage()+" accessor failed for field "+fields[i]);
						e.printStackTrace();
					}
				}
			}
		}
		JSONObject o3 = new JSONObject();
		if(DEBUG)
			System.out.println("RETURN from reflect, put:"+o3.has(bean.getClass().getName())+" key:"+bean.getClass().getName());
		o3.put(bean.getClass().getName(),o2);
		return o3;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(className == null ? "NULL" : className);
		sb.append(" as Class:");
		sb.append(classClass == null ? "NULL" : classClass);
		sb.append("\r\nConstructors:\r\n");
		if(defaultConstructor != null) {
			sb.append(defaultConstructor);
			sb.append("\r\n");
		}
		if(constructors != null)
			for(int i = 0; i < constructors.length; i++) {
				sb.append(constructors[i]);
				sb.append(" Param Types:");
				sb.append(Arrays.toString(constructorParamTypes[i]));
				sb.append("\r\n");
			}
		if(fields != null)
			for(int i = 0; i < fields.length; i++) {
				sb.append(fieldNames.get(i));
				sb.append(" FieldType:");
				sb.append(fieldTypes[i]);
				sb.append("\r\n");
			}
		return sb.toString();
	}

}
