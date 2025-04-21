package org.json.reflect;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
* A basic Serializable helper class used with arrays of Method names and 
* parameters for a target class.
* @author Jonathan Groff Copyright (C) NeoCoreTechs, Inc. 2025
*/
public final class MethodNameAndParams implements Serializable {
	static final long serialVersionUID = 8837760295724028863L;
	private static boolean DEBUG = true;
	public transient Class<?> classClass;
	public String className;
	protected transient Method method;
	public transient String methodName;
	public transient Class<?>[] methodParams;
	public String methodSig;
	public transient Class<?> returnType;
	// last invocation class name and index to method
	private String returnClassName;
	// TreeMap of method name mapping to list of indexes to arrays of name and params overloads
	protected static Map<Field, MethodNameAndParams> accessorMethods = new ConcurrentHashMap<Field, MethodNameAndParams>();
	protected static Map<Field, MethodNameAndParams> mutatorMethods = new ConcurrentHashMap<Field, MethodNameAndParams>();
	//

	/**
	 * No arg ctor call for deserialized
	 */
	public MethodNameAndParams() {}

	public String getMethodSig() { return methodSig; }

	public Class<?> getReturnType() { return returnType; }

	public String getMethodName() { return methodName; }

	public void setReturnClass(String retClass) {
		this.returnClassName = retClass;
	}
	public String getReturnClass() {
		return returnClassName;
	}

	/**
	 * Process the accessor methods for a given class: getXXX, non static, params length = 0, NOT void return, not bridge, not native, not abstract
	 * @param clazz target class
	 * @param m clazz.getMethods()
	 * @return MethodNamesAndParams this instance populated
	 */
	public static MethodNameAndParams reflectorAccessorFactory(FieldNamesAndConstructors fieldz) {
		MethodNameAndParams accessors = new MethodNameAndParams();
		accessors.classClass = fieldz.classClass;
		accessors.className = fieldz.className;
		Method[] m = accessors.classClass.getDeclaredMethods();
		if(FieldNamesAndConstructors.recursedFields == null)
			return null;
		for(Field fields: FieldNamesAndConstructors.recursedFields.keySet()) {
			char firstChar = fields.getName().charAt(0);
			String accName = "get"+String.valueOf(firstChar).toUpperCase()+fields.getName().substring(1);
			for(int i = 0; i < m.length; i++) {
				final int modifiers = m[i].getModifiers();
				if(DEBUG)
					if(m[i].getName().equals(accName))
					System.out.printf("Possible accessor Method: %s static:%b native:%b paramLen:%d bridge:%b abstract:%b retType:%s%n", m[i].getName(),
						Modifier.isStatic(modifiers),
						Modifier.isNative(modifiers),
						m[i].getParameterTypes().length,
						m[i].isBridge(),
						Modifier.isAbstract(modifiers),
						m[i].getReturnType());
				if(m[i].getName().equals(accName) &&
						!Modifier.isStatic(modifiers)
						&& m[i].getParameterTypes().length == 0
						&& !Modifier.isNative(modifiers)
						&& !m[i].isBridge()
						&& !Modifier.isAbstract(modifiers)
						&& m[i].getReturnType() != Void.TYPE) {
					accessors.methodName = m[i].getName();
					accessors.method = m[i];
					accessors.methodParams = m[i].getParameterTypes();
					accessors.methodSig = m[i].toString();
					accessors.returnType = m[i].getReturnType();
					accessorMethods.put(fields,  accessors);
					if(DEBUG)
						System.out.println("MethodNamesAndParams accessor Method "+m[i].toString());
				}
			}
		}
		return accessors;
	}
	/**
	 * populate the mutator methods for a given class: setXXX, void return, params length != 0, not bridge, not static, not native, not abstract
	 * @param clazz target class
	 * @param m clazz.getMethods()
	 * @return this instance populated
	 */
	public static MethodNameAndParams reflectorMutatorFactory(FieldNamesAndConstructors fieldz) {  
		MethodNameAndParams mutators = new MethodNameAndParams();
		mutators.classClass = fieldz.classClass;
		mutators.className = fieldz.className;   
		Method[] m = mutators.classClass.getDeclaredMethods();
		if(FieldNamesAndConstructors.recursedFields == null)
			return null;
		for(Field fields: FieldNamesAndConstructors.recursedFields.keySet()) {
			char firstChar = fields.getName().charAt(0);
			String mutName = "set"+String.valueOf(firstChar).toUpperCase()+fields.getName().substring(1);
			for(int i = 0; i < m.length; i++) {
				final int modifiers = m[i].getModifiers();
				if(DEBUG)
					if(m[i].getName().equals(mutName))
					System.out.printf("Possible mutator Method: %s static:%b native:%b paramLen:%d bridge:%b abstract:%b retType:%s%n", m[i].getName(),
						Modifier.isStatic(modifiers),
						Modifier.isNative(modifiers),
						m[i].getParameterTypes().length,
						m[i].isBridge(),
						Modifier.isAbstract(modifiers),
						m[i].getReturnType());
				if(m[i].getName().equals(mutName)
						&& !Modifier.isStatic(modifiers)
						&& !Modifier.isNative(modifiers)
						&& m[i].getParameterTypes().length != 0
						&& !m[i].isBridge()
						&& !Modifier.isAbstract(modifiers)
						&& m[i].getReturnType() == Void.TYPE) {
					mutators.methodName = m[i].getName();
					mutators.methodName = m[i].getName();
					mutators.method = m[i];
					mutators.methodParams = m[i].getParameterTypes();
					mutators.methodSig = m[i].toString();
					mutators.returnType = m[i].getReturnType();
					mutatorMethods.put(fields,  mutators);
					if(DEBUG)
						System.out.println("MethodNamesAndParams mutator Method "+m[i].toString());
				}
			}
		}
		return mutators;
	}

}
