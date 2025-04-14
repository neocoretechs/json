package org.json.reflect;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
* Utilizes helper classes {@link MethodNamesAndParams} {@link FieldNamesAndConstructors} and attempts to find 
* the best match between passed params and reflected
* method params and so takes polymorphic calls into account.
* @author Jonathan Groff Copyright (C) NeoCoreTechs 2025
*/
public final class ReflectFieldsAndMethods  {
	private static final boolean DEBUG = false;
	static HandlerClassLoader hcl = new HandlerClassLoader();
	private static Map<Class<?>,Set<FieldsAndMethods>> classes = new ConcurrentHashMap<Class<?>,Set<FieldsAndMethods>>();
       
    public ReflectFieldsAndMethods() {}
    
    /**
     * Main entry point for class hierarchy reflection, attempt cache retrieval first
     * @param o object to reflect hierarchy
     * @return The HashSet of reflected hierarchy
     */
    public static Set<FieldsAndMethods> reflect(Object o) {
    	Class<?> clazz = o.getClass();
        Set<FieldsAndMethods> classesSet = classes.get(clazz);
    	if(classesSet == null) {
    		classesSet = new HashSet<FieldsAndMethods>();
    		collectClasses(clazz, classesSet);
    		classes.put(clazz, classesSet);
    	}
        return classesSet;
    }
    
    private static void collectClasses(Class<?> clazz, Set<FieldsAndMethods> classes) {
        if (clazz != null) {
        	if(DEBUG)
        		System.out.println("ReflectFieldsAndMethods Adding class:"+clazz);
            classes.add(init(clazz));
            // Add superclass
            Class<?> superClass = clazz.getSuperclass();
            if(superClass != java.lang.Object.class)
            	collectClasses(superClass, classes);
        }
    }
    
    /**
     * Build arrays and lists of method names and parameters to facilitate method lookup and invocation.
     * Methods are looked up by name, then parameters are compared such that overloaded methods can be invoked properly.
     * The {@link MethodNamesAndParams} class is used to build the arrays and lists. In this class, the methods array holds reflected
     * methods and a TreeMap of method name mapped to a list of indexes into the methods array allows us to look up candidate
     * overloaded methods.
     * @param tclass The class to reflect
     * @throws ClassNotFoundException
     */
    private static FieldsAndMethods init(Class<?> clazz) {
    	Method m[] = clazz.getMethods();
      	MethodNamesAndParams accessors = MethodNamesAndParams.reflectorAccessorFactory(clazz, m);
    	MethodNamesAndParams mutators = MethodNamesAndParams.reflectorMutatorFactory(clazz, m);
    	FieldNamesAndConstructors fields = FieldNamesAndConstructors.reflectorFieldNamesAndConstructorFactory(clazz);
    	//
    	FieldsAndMethods fam = new FieldsAndMethods();
    	fam.accessors = accessors;
    	fam.mutators = mutators;
    	fam.fields = fields;
    	return fam;
    }
    
    public static class FieldsAndMethods {
    	public MethodNamesAndParams accessors;
    	public MethodNamesAndParams mutators;
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
    public static Object invokeMethod(MethodNamesAndParams tmc, String targetMethod, Object localObject, Object[] methodParams) throws Exception {             
    	if(DEBUG) {
    		System.out.println("ReflectFieldsAndMethods Target method:"+targetMethod+" remote request:"+tmc+" localObject:"+localObject);
    	}
    	tmc.methodIndex = tmc.methodNames.indexOf(targetMethod);
    	if(tmc.methodIndex == -1)
    		throw new Exception("Method not found in provided Method Names and Parameter structure");
    	ArrayList<Integer> methodIndexList = tmc.methodLookup.get(tmc.methodIndex);
    	String whyNotFound = "No such method";
		Class[] params = tmc.methodParams[tmc.methodIndex];
		//
		// We are going to reflect the method params and determine the best one to invoke
		// based on the parameters being assignable from the method parameters
		//
    	if(methodIndexList != null ) {
    		TreeMap<Integer,Integer> methodRank = new TreeMap<Integer,Integer>();
    		boolean found = false;
    		for(int methodIndexCtr = 0; methodIndexCtr < methodIndexList.size(); methodIndexCtr++) {
    			tmc.methodIndex = methodIndexList.get(methodIndexCtr);
    			if (DEBUG) {
    				for(int iparm1 = 0; iparm1 < params.length ; iparm1++) {        
    					System.out.println("ReflectFieldsAndMethods Target method:"+targetMethod+" Calling param: "+params[iparm1]);
    				}
    			}
    			int sumParamRank = 0;
    			if( params.length == tmc.methodParams[tmc.methodIndex].length ) {
    				found = true; // we found a method with required number of params
    				for(int paramIndex = 0 ; paramIndex < params.length; paramIndex++) {
    					// exact match? If passed param is null, count as 0 rank, below assignable or exact
    					if( params[paramIndex] != null ) {
    						//
    						// Establish our ranking of method params to parameters we passed for the call
    						//
    						if(tmc.methodParams[tmc.methodIndex][paramIndex] == params[paramIndex]) {
    							sumParamRank+=2;
    						} else {
    							// can we cast it?	
    							if(tmc.methodParams[tmc.methodIndex][paramIndex].isAssignableFrom(params[paramIndex])) {
    								sumParamRank+=1;
    							} else {
    								// parameter doesnt match, reduce to ineligible
    								sumParamRank = -1;
    								break;
    							}
    						}
    					}
    				}
    				methodRank.put(sumParamRank, tmc.methodIndex);
    			}
    		}
    		//
    		if(found) {
    			tmc.methodIndex = methodRank.get(methodRank.lastKey());
    			if(tmc.methodIndex < 0) {
    				whyNotFound = "parameters do not match";
    			} else {
    				// invoke it for return
    				if(DEBUG) {
    					System.out.println("ReflectFieldsAndMethods Invoking method:"+tmc.methods[tmc.methodIndex]+" on object "+localObject+" with params "+Arrays.toString(methodParams));
    				}
    				//tmc.setReturnClass(methods[methodIndex].getReturnType().getName());
    				Object or =tmc.methods[tmc.methodIndex].invoke(localObject, methodParams);
    				if(or != null) {
    					tmc.setReturnClass(or.getClass().getName());
    					if(DEBUG)
    						System.out.println("ReflectFieldsAndMethods return from invocation:"+or+" class:"+or.getClass().getName());
    				} else {
    					tmc.setReturnClass(tmc.methods[tmc.methodIndex].getReturnType().getName());
    					if(DEBUG)
    						System.out.println("ReflectFieldsAndMethods returned NULL from invocation, setting return class "+tmc.methods[tmc.methodIndex].getReturnType().getName());
    				}
    				return or;
    			}
    		} else {
    			whyNotFound = "wrong number of parameters";
    		}
    	}
    	throw new NoSuchMethodException("Method "+targetMethod+" not found in "+tmc.className+" "+whyNotFound);
    }

}

