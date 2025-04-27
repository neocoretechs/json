package org.json.reflect;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.reflect.FieldNamesAndConstructors;
import org.json.reflect.ReflectFieldsAndMethods;
import org.json.reflect.ReflectFieldsAndMethods.FieldsAndMethods;

public class JsonUtil {

	private static boolean NOTIFY = true;
	private static boolean DEBUG = false;

	public JsonUtil() {}
	
	public static void generateParams(Object[] params, Class<?>[] paramTypes) {
		for(int i = 0; i < params.length; i++) {
			Object o = null;
			if(params[i].getClass() != paramTypes[i]) {
				if(params[i].getClass() == java.util.HashMap.class) { // a map of fieldnames and values, try to set field, if fail, use mutator
					try {
						Constructor<?> c = paramTypes[i].getConstructor();
						o = c.newInstance();
						if(DEBUG)
							System.out.println("JsonUtil.generateParams HashMap NewInstance o="+o+" class:"+o.getClass());
					} catch (InstantiationException | IllegalAccessException | IllegalArgumentException| InvocationTargetException | NoSuchMethodException | SecurityException e2) {
						e2.printStackTrace();
					}
					FieldsAndMethods fam = ReflectFieldsAndMethods.getFieldsAndMethods(paramTypes[i]);
					for(Object e: ((HashMap)params[i]).entrySet()) {
						Map.Entry me = (Map.Entry)e;
						String fieldName = (String) me.getKey();
						int fnum = fam.fields.fieldNames.indexOf(fieldName);
						if(fnum == -1)
							throw new RuntimeException("Can't find field:"+fieldName);
						Field field = fam.fields.getField(fnum);
						if(DEBUG)
							System.out.println("JsonUtil.generateParams HashMap Field:"+field);
						try {
							field.setAccessible(true);
							if(DEBUG)
								System.out.println("JsonUtil.generateParams HashMap Field set for o="+me.getValue()+" class:"+me.getValue().getClass());
							field.set(o, me.getValue());
						} catch (IllegalArgumentException | IllegalAccessException e1) {
							try {
								ReflectFieldsAndMethods.invokeMutatorMethod(field, o, new Object[]{me.getValue()});
							} catch (Exception e2) {
								e2.printStackTrace();
							}
						}
					}
				} else {
					if(params[i].getClass() == java.util.ArrayList.class) { // an array of elements to form into java array
						try {	
							Object[] fArray = ((ArrayList)params[i]).toArray();
							int length = fArray.length;
							if(DEBUG)
								System.out.println("JsonUtil.generateParams ArrayList paramTypes[i]="+paramTypes[i]+" paramTypes[i].getComponentType()="+paramTypes[i].getComponentType());
							if(paramTypes[i].getComponentType() == Byte.TYPE) {
								ByteBuffer b = ByteBuffer.allocate(length);
								for(int j = 0; j < length; j++) {
									b.put((byte)((int)fArray[j] & 0xff));
								}
								o = b.array();
							} else {
								o = Array.newInstance(paramTypes[i].getComponentType(), fArray.length);
								if(DEBUG)
									System.out.println("Component newInstance o="+o+" class:"+o.getClass());
								for(int j = 0; j < length; j++) {
									if(DEBUG)
										System.out.println("JsonUtil.generateParams ArrayList fArray[j]="+fArray[j]+" class:"+fArray[j].getClass());
									Array.set(o, j, paramTypes[i].getComponentType().cast(fArray[j]));	
								}
							}
						} catch (IllegalArgumentException | SecurityException e) {
							e.printStackTrace();
						}
					} else {
						if(paramTypes[i] == java.lang.Character.class) {
							o = ((String)params[i]).charAt(0);
						} else {
							// a string type for set via constructor
							try {
								Constructor<?> c = paramTypes[i].getConstructor(String.class);
								o = c.newInstance(params[i]);
							} catch (InstantiationException | IllegalAccessException | IllegalArgumentException| InvocationTargetException | NoSuchMethodException | SecurityException e2) {
								e2.printStackTrace();
							}
						}
					}
				}
				params[i] = o;
			}
		}
	}

}
