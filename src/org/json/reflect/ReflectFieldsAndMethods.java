package org.json.reflect;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

/**
* Utilizes helper classes {@link MethodNamesAndParams} {@link FieldNamesAndConstructors} and attempts to find 
* the best match between passed params and reflected
* method params and so takes polymorphic calls into account.
* @author Jonathan Groff Copyright (C) NeoCoreTechs 2025
*/
public final class ReflectFieldsAndMethods  {
	private static final boolean DEBUG = true;
	static HandlerClassLoader hcl = new HandlerClassLoader();
       
    public ReflectFieldsAndMethods() {}
    
    /**
     * Main entry point for class hierarchy reflection
     * @param clazz
     * @return
     */
    public static Set<FieldsAndMethods> reflect(Object o) {
    	Class<?> clazz = o.getClass();
        Set<FieldsAndMethods> classes = new HashSet<>();
        collectClasses(clazz, classes);
        return classes;
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
    	//NoSuchMethodException, InvocationTargetException, IllegalAccessException, PowerSpaceException  {               
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
    				// if skipArgs, don't compare first skipArgs params
    				for(int paramIndex = 0 ; paramIndex < params.length; paramIndex++) {
    					// exact match? If passed param is null, count as 0 rank, below assignable or exact
    					// For our Json version of this, we are going to intercept the params as they
    					// are passed from Gson reflection and massage them back to an object model
    					// for some reason Gson refuses to do anything but change some objects to its internal map form
    					if( params[paramIndex] != null ) {
    						if(params[paramIndex] == LinkedHashMap.class) {
    							if(DEBUG) {
    								System.out.println("ReflectFieldsAndMethods Target method:"+targetMethod+" Method param: "+tmc.methodParams[tmc.methodIndex][paramIndex]+" has map");
    							}
    							// set the calling param back to method param
    							params[paramIndex] = tmc.methodParams[tmc.methodIndex][paramIndex];
    							// create the method param with constructor using Gson map attributes
    							// start with the ctor of the main object that serves as param to method
    							// paramArray matches params. we will xfer the gson map to param array
    							Constructor ctor = params[paramIndex].getConstructor();
    							// newinstance of param to method call
    							Object o = ctor.newInstance();
    							// extract the args from the map, it has field name, string value
    							LinkedHashMap ltm =  (LinkedHashMap) o;
    							Set ltmSet = ltm.entrySet();
    							Iterator ltmIterator = ltmSet.iterator();
    							// each field name and value in gson map
    							while(ltmIterator.hasNext()) {
    								Map.Entry ltmEntry = (Entry) ltmIterator.next();
    								// get the field name from the param based on key in gson map
    								Field[] fields = params[paramIndex].getDeclaredFields();//.getDeclaredField((String) ltmEntry.getKey());
    								Field field = null;
    								for(int j = 0; j < fields.length; j++) {	
    									if(fields[j].getName().equals((String) ltmEntry.getKey()) && 
    											!Modifier.toString(fields[j].getModifiers()).contains("private")) {
    										field = fields[j];
    										break;
    									}
    								}
    								//
    								// try to either set the field directly if not private, or setFieldname mutator method if it is
    								//
    								if(field == null) {
    									StringBuilder sb = new StringBuilder("set");
    									sb.append(String.valueOf(((String)ltmEntry.getKey()).charAt(0)).toUpperCase());
    									sb.append(((String)ltmEntry.getKey()).substring(1));
    									String setString = sb.toString();
    									if(DEBUG)
    										System.out.println("ReflectFieldsAndMethods Target method:"+targetMethod+" trying mutator "+setString);
    									Method setMethod = params[paramIndex].getDeclaredMethod(setString, new Class[] {String.class});
     									if(DEBUG)
    										System.out.println("ReflectFieldsAndMethods Target method:"+targetMethod+" mutator "+setMethod+" for "+o);
    									setMethod.invoke(o, ltmEntry.getValue());
    									methodParams[paramIndex] = o;
    									if(DEBUG)
    										System.out.println("ReflectFieldsAndMethods Target method:"+targetMethod+" mutator "+setMethod+" result "+methodParams[paramIndex] );
    								} else {
    									// get the constructor for the param, we have to assume string constructor
    									Constructor fieldCtor = field.getType().getConstructor(String.class);
    									if(DEBUG)
    										System.out.println("ReflectFieldsAndMethods Target method:"+targetMethod+" trying ctor "+fieldCtor);
    									// make the new instance, call the string ctor with value from gson map
    									Object ofield = fieldCtor.newInstance(ltmEntry.getValue());
    									if(DEBUG)
    										System.out.println("ReflectFieldsAndMethods Target method:"+targetMethod+" field "+ofield);
    									// set the value of the newinstance of param to method call
    									field.set(o, ofield);
    									if(DEBUG)
    										System.out.println("ReflectFieldsAndMethods Target method:"+targetMethod+" field "+ofield+" set for "+o);
    									// set the actual param to new instance of field name from gson map, called with string ctor from gson map
    									methodParams[paramIndex] = o;
      									if(DEBUG)
    										System.out.println("ServerInvokeJson Target method:"+targetMethod+" field "+ofield+" set param array "+methodParams[paramIndex]);
    								}
    							}
    						}
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

