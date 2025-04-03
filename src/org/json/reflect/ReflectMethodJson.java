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
* The remote call mechanism depends on Java reflection to provide access to methods that can be
* remotely invoked via serializable arguments and method name. By designating the reflected classes at startup
* in the server module, remote calls have access to reflected methods designated with the {@link ServerMethod} annotation.
* This class handles reflection of the user requests to call designated methods in the server side classes.<p/>
* It utilizes helper class {@link MethodNamesAndParams} and attempts to find the best match between passed params and reflected
* method params and so takes polymorphic calls into account.
* It starts by populating a table of those methods, and at runtime, creates a method call transport for client,
* and provides for server-side invocation of those methods.
* Option to skip leading arguments, for whatever reason, is provided.<p>
* For our Json version of this, we are going to intercept the params as they
* are passed from Gson reflection and massage them back to an object model
* for some reason Gson refuses to do anything but change some objects to its internal map form
* @author Jonathan Groff Copyright (C) NeoCoreTechs 1998-2000, 2015, 2025
*/
public final class ReflectMethodJson  {
	private static final boolean DEBUG = false;
	static HandlerClassLoader hcl = new HandlerClassLoader();
       
    public ReflectMethodJson() {}
    
    public static Set<FieldsAndMethods> getAllSuperclasses(Class<?> clazz) {
        Set<FieldsAndMethods> classes = new HashSet<>();
        collectClasses(clazz, classes);
        return classes;
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
      	MethodNamesAndParams accessors = new MethodNamesAndParams();
      	MethodNamesAndParams mutators = new MethodNamesAndParams();
    	FieldNamesAndConstructors fields = new FieldNamesAndConstructors();
    	//
    	accessors.classClass = clazz;
    	accessors.className = accessors.classClass.getName();
    	mutators.classClass = clazz;
    	mutators.className = mutators.classClass.getName();
    	Method m[];
    	m = clazz.getMethods();
    	ArrayList<Integer> accessorIndex = new ArrayList<Integer>();
      	ArrayList<Integer> mutatorIndex = new ArrayList<Integer>();
     	ArrayList<Integer> fieldIndex = new ArrayList<Integer>();
       	ArrayList<Integer> constructorIndex = new ArrayList<Integer>();
      	for(int i = m.length-1; i >= 0 ; i--) {
      		final int modifiers = m[i].getModifiers();
      		if(m[i].getName().startsWith("get") &&
      			!Modifier.isStatic(modifiers)
      			&& m[i].getParameterTypes().length == 0
      			&& !m[i].isBridge()
      			&& m[i].getReturnType() != Void.TYPE) {
      				accessors.methodNames.add(m[i].getName());
      				accessorIndex.add(i);
      				System.out.println("accessor Method "+m[i].toString());
      		} else {
      			if(m[i].getName().startsWith("set")
      				&& !Modifier.isStatic(modifiers)
      	      		&& m[i].getParameterTypes().length != 0
      	      		&& !m[i].isBridge()
      	      		&& m[i].getReturnType() == Void.TYPE) {
      					mutators.methodNames.add(m[i].getName());
      					mutatorIndex.add(i);
      					System.out.println("mutator Method "+m[i].toString());
      			}
      		}
      	}
      	//
      	// process accessors
      	// create arrays
      	accessors.methods = new Method[accessorIndex.size()];
      	accessors.methodParams = new Class[accessorIndex.size()][];
      	accessors.methodSigs = new String[accessorIndex.size()];
      	accessors.returnTypes = new Class[accessorIndex.size()];
      	mutators.methods = new Method[mutatorIndex.size()];
      	mutators.methodParams = new Class[mutatorIndex.size()][];
    	mutators.methodSigs = new String[mutatorIndex.size()];
    	mutators.returnTypes = new Class[mutatorIndex.size()];
    	int methCnt = 0;
    	//
    	for(int i = accessorIndex.size()-1; i >= 0 ; i--) {
    		Method mi = m[accessorIndex.get(i)];
    		accessors.methodParams[methCnt] = mi.getParameterTypes();
    		accessors.methodSigs[methCnt] = mi.toString();
    		accessors.returnTypes[methCnt] = mi.getReturnType();
    		if( accessors.returnTypes[methCnt] == void.class ) 
    			accessors.returnTypes[methCnt] = Void.class;

    		ArrayList<Integer> mPos = accessors.methodLookup.get(mi.getName());
    		if(mPos == null) {
    			mPos = new ArrayList<Integer>();
    			accessors.methodLookup.put(mi.getName(), mPos);
    		}
    		mPos.add(methCnt);
    		accessors.methods[methCnt++] = mi;
    	}
      	methCnt = 0;
    	//
    	for(int i = mutatorIndex.size()-1; i >= 0 ; i--) {
    		Method mi = m[mutatorIndex.get(i)];
    		mutators.methodParams[methCnt] = mi.getParameterTypes();
    		mutators.methodSigs[methCnt] = mi.toString();
    		mutators.returnTypes[methCnt] = mi.getReturnType();
    		if( mutators.returnTypes[methCnt] == void.class ) 
    			mutators.returnTypes[methCnt] = Void.class;

    		ArrayList<Integer> mPos = mutators.methodLookup.get(mi.getName());
    		if(mPos == null) {
    			mPos = new ArrayList<Integer>();
    			mutators.methodLookup.put(mi.getName(), mPos);
    		}
    		mPos.add(methCnt);
    		mutators.methods[methCnt++] = mi;
    	}
    	//
    	// process fields
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
     	methCnt = 0;
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
      		Class<?> ftype = field.getType();
      		int fmods = field.getModifiers();
      		if(!Modifier.isTransient(fmods) &&
      			!Modifier.isStatic(fmods) &&
      			!Modifier.isVolatile(fmods) &&
      			!Modifier.isNative(fmods) &&
      			!Modifier.isFinal(fmods) ) {
      			fieldIndex.add(i);
      		}		
      	}
     	//
     	fields.classClass = clazz;
     	fields.className = clazz.getName();
     	fields.fields = new Field[fieldIndex.size()];
     	fields.fieldTypes = new Class[fieldIndex.size()];
       	methCnt = 0;
    	//
    	for(int i = accessorIndex.size()-1; i >= 0 ; i--) {
    		Field fi = fieldz[fieldIndex.get(i)];
    		fields.fields[methCnt] = fi;
    		fields.fieldTypes[methCnt++] = fi.getType();
    		fields.fieldNames.add(fi.getName());
    	}
    	//
    	FieldsAndMethods fam = new FieldsAndMethods();
    	fam.accessors = accessors;
    	fam.mutators = mutators;
    	fam.fields = fields;
    	return fam;
    }
    
    private static void collectClasses(Class<?> clazz, Set<FieldsAndMethods> classes) {
        if (clazz != null) {
            classes.add(init(clazz));
            // Add superclass
            collectClasses(clazz.getSuperclass(), classes);
        }
    }
    
    public static class FieldsAndMethods {
    	public MethodNamesAndParams accessors;
    	public MethodNamesAndParams mutators;
    	public FieldNamesAndConstructors fields;
    }
    /**
     * For an incoming RelatrixStatement, verify and invoke the proper
     * method.  We assume there is a table of class names and this and
     * it has been used to locate this object. 
     * @return Object of result of method invocation
     */
    public static Object invokeMethod(MethodNamesAndParams tmc, String targetMethod, Object localObject, Object[] methodParams) throws Exception {
    	//NoSuchMethodException, InvocationTargetException, IllegalAccessException, PowerSpaceException  {               
    	if(DEBUG) {
    		System.out.println("ServerInvokeJson Target method:"+targetMethod+" remote request:"+tmc+" localObject:"+localObject);
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
    					System.out.println("ServerInvokeJson Target method:"+targetMethod+" Calling param: "+params[iparm1]);
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
    								System.out.println("ServerInvokeJson Target method:"+targetMethod+" Method param: "+tmc.methodParams[tmc.methodIndex][paramIndex]+" has map");
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
    										System.out.println("ServerInvokeJson Target method:"+targetMethod+" trying mutator "+setString);
    									Method setMethod = params[paramIndex].getDeclaredMethod(setString, new Class[] {String.class});
     									if(DEBUG)
    										System.out.println("ServerInvokeJson Target method:"+targetMethod+" mutator "+setMethod+" for "+o);
    									setMethod.invoke(o, ltmEntry.getValue());
    									methodParams[paramIndex] = o;
    									if(DEBUG)
    										System.out.println("ServerInvokeJson Target method:"+targetMethod+" mutator "+setMethod+" result "+methodParams[paramIndex] );
    								} else {
    									// get the constructor for the param, we have to assume string constructor
    									Constructor fieldCtor = field.getType().getConstructor(String.class);
    									if(DEBUG)
    										System.out.println("ServerInvokeJson Target method:"+targetMethod+" trying ctor "+fieldCtor);
    									// make the new instance, call the string ctor with value from gson map
    									Object ofield = fieldCtor.newInstance(ltmEntry.getValue());
    									if(DEBUG)
    										System.out.println("ServerInvokeJson Target method:"+targetMethod+" field "+ofield);
    									// set the value of the newinstance of param to method call
    									field.set(o, ofield);
    									if(DEBUG)
    										System.out.println("ServerInvokeJson Target method:"+targetMethod+" field "+ofield+" set for "+o);
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
    					System.out.println("ServerInvokeJson Invoking method:"+tmc.methods[tmc.methodIndex]+" on object "+localObject+" with params "+Arrays.toString(methodParams));
    				}
    				//tmc.setReturnClass(methods[methodIndex].getReturnType().getName());
    				Object or =tmc.methods[tmc.methodIndex].invoke(localObject, methodParams);
    				if(or != null) {
    					tmc.setReturnClass(or.getClass().getName());
    					if(DEBUG)
    						System.out.println("ServerInvokeJson return from invocation:"+or+" class:"+or.getClass().getName());
    				} else {
    					tmc.setReturnClass(tmc.methods[tmc.methodIndex].getReturnType().getName());
    					if(DEBUG)
    						System.out.println("ServerInvokeJson returned NULL from invocation, setting return class "+tmc.methods[tmc.methodIndex].getReturnType().getName());
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

