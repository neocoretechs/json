package org.json.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
* Utilizes helper classes {@link MethodNameAndParams} {@link FieldNamesAndConstructors} and attempts to find 
* the best match between passed params and reflected
* method params and so takes polymorphic calls into account.
* @author Jonathan Groff Copyright (C) NeoCoreTechs 2025
*/
public final class ReflectFieldsAndMethods  {
	private static final boolean DEBUG = false;
	static HandlerClassLoader hcl = new HandlerClassLoader();
	private static Map<Class<?>,FieldsAndMethods> classes = new ConcurrentHashMap<Class<?>,FieldsAndMethods>();
       
    public ReflectFieldsAndMethods() {}
    
    /**
     * Main entry for class
     * @param clazz target class
     * @return FieldsAndMethods structure
     */
    public static FieldsAndMethods getFieldsAndMethods(Class<?> clazz) {
    	FieldsAndMethods fam = classes.get(clazz);
    	if(fam == null)
    		fam = reflect(clazz);
    	return fam;
    }
    /**
     * Main entry for object
     * @param o target object, class extracted
     * @return FieldsAndMethods structure
     */
    public static FieldsAndMethods getFieldsAndMethods(Object o) {
    	Class<?> clazz = o.getClass();
    	return getFieldsAndMethods(clazz);
    }
    
    
    /**
     * Main entry point for class hierarchy reflection, attempt cache retrieval first
     * @param c CLass to reflect hierarchy
     * @return The HashSet of reflected hierarchy
     */
    private static FieldsAndMethods reflect(Class<?> clazz) {
        FieldsAndMethods classesSet = classes.get(clazz);
    	if(classesSet == null) {
    		classesSet = init(clazz);
    		classes.put(clazz, classesSet);
    	}
        return classesSet;
    }
    
    /**
     * Build arrays and lists of method names and parameters to facilitate method lookup and invocation.
     * Methods are looked up by name, then parameters are compared such that overloaded methods can be invoked properly.
     * The {@link MethodNameAndParams} class is used to build the arrays and lists. In this class, the methods array holds reflected
     * methods and a TreeMap of method name mapped to a list of indexes into the methods array allows us to look up candidate
     * overloaded methods.
     * @param tclass The class to reflect
     * @throws ClassNotFoundException
     */
    private static FieldsAndMethods init(Class<?> clazz) {
    	// recurses superclasses
    	FieldNamesAndConstructors fields = FieldNamesAndConstructors.getFieldNamesAndConstructors(clazz);
    	// get the set of methods for fields and superclass fields
       	MethodNameAndParams accessors = MethodNameAndParams.reflectorAccessorFactory(fields);
    	MethodNameAndParams mutators = MethodNameAndParams.reflectorMutatorFactory(fields);
    	//
    	FieldsAndMethods fam = new FieldsAndMethods();
    	fam.accessors = accessors;
    	fam.mutators = mutators;
    	fam.fields = fields;
    	return fam;
    }
    
    public static class FieldsAndMethods {
    	public MethodNameAndParams accessors;
    	public MethodNameAndParams mutators;
    	public FieldNamesAndConstructors fields;
    	public String toString() {
    		return fields.toString();
    	}
    }
    
    /**
     * Verify and invoke the proper
     * method.  We assume there is a table of class names and this and
     * it has been used to locate this object. 
     * @return Object of result of method invocation
     */
    public static Object invokeAccessorMethod(Field field, Object localObject, Object[] methodParams) throws Exception {             
    	if(DEBUG) {
    		System.out.println("invokeAccessorMethod field:"+field+" localObject:"+localObject+" "+Arrays.toString(methodParams));
    	}
    	// invoke it for return
    	MethodNameAndParams method = MethodNameAndParams.accessorMethods.get(field);
    	if(method == null)
    		throw new NoSuchMethodException("Accessor Method for "+field+" not found.");
    	Object or = method.method.invoke(localObject, methodParams);
    	return or;
    }
    /**
     * Verify and invoke the proper
     * method.  We assume there is a table of class names and this and
     * it has been used to locate this object. 
     * @return Object of result of method invocation
     */
    public static void invokeMutatorMethod(Field field, Object localObject, Object[] methodParams) throws Exception {             
    	if(DEBUG) {
    		System.out.println("invokeMutatorMethod field:"+field+" localObject:"+localObject+" "+Arrays.toString(methodParams));
    	}
    	// invoke it for return
    	MethodNameAndParams method = MethodNameAndParams.mutatorMethods.get(field);
    	if(method == null)
    		throw new NoSuchMethodException(" Method for "+field+" not found.");
    	method.method.invoke(localObject, methodParams);
    }
}

