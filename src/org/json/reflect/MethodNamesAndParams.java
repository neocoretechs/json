package org.json.reflect;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
* A basic Serializable helper class used with arrays of Method names and 
* parameters for a target class.
* @author Jonathan Groff Copyright (C) NeoCoreTechs, Inc. 2025
*/
public final class MethodNamesAndParams implements Serializable {
       static final long serialVersionUID = 8837760295724028863L;
       private static boolean DEBUG = true;
       public transient Class<?> classClass;
       public String className;
       protected transient Method[] methods;
       public transient ArrayList<String> methodNames = new ArrayList<String>();
       public transient Class<?>[][] methodParams;
       public String[] methodSigs;
       public transient Class<?>[] returnTypes;
       // last invocation class name and index to method
       private String returnClassName;
       public int methodIndex;
       // TreeMap of method name mapping to list of indexes to arrays of name and params overloads
       protected TreeMap<String, ArrayList<Integer>> methodLookup = new TreeMap<String, ArrayList<Integer>>();
       //
 
       /**
       * No arg ctor call for deserialized
       */
       public MethodNamesAndParams() {}

       public String[] getMethodSigs() { return methodSigs; }

       public Class<?>[] getReturnTypes() { return returnTypes; }

       public List<String> getMethodNames() { return methodNames; }
       
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
       public static MethodNamesAndParams reflectorAccessorFactory(Class<?> clazz, Method[] m) {
       		MethodNamesAndParams accessors = new MethodNamesAndParams();
    	  	accessors.classClass = clazz;
        	accessors.className = accessors.classClass.getName();
        	ArrayList<Integer> accessorIndex = new ArrayList<Integer>();
          	for(int i = m.length-1; i >= 0 ; i--) {
          		final int modifiers = m[i].getModifiers();
          		if(m[i].getName().startsWith("get") &&
          			!Modifier.isStatic(modifiers)
          			&& m[i].getParameterTypes().length == 0
          			&& !Modifier.isNative(modifiers)
          			&& !m[i].isBridge()
          			&& !Modifier.isAbstract(modifiers)
          			&& m[i].getReturnType() != Void.TYPE) {
          				accessors.methodNames.add(m[i].getName());
          				accessorIndex.add(i);
          				if(DEBUG)
          					System.out.println("MethodNamesAndParams accessor Method "+m[i].toString());
          		}
          	}
          	//
          	// process accessors
          	// create arrays
          	accessors.methods = new Method[accessorIndex.size()];
          	accessors.methodParams = new Class[accessorIndex.size()][];
          	accessors.methodSigs = new String[accessorIndex.size()];
          	accessors.returnTypes = new Class[accessorIndex.size()];
        	//
          	int methCnt = 0;
        	for(int i = accessorIndex.size()-1; i >= 0 ; i--) {
        		Method mi = m[accessorIndex.get(i)];
        		accessors.methodParams[methCnt] = mi.getParameterTypes();
        		accessors.methodSigs[methCnt] = mi.toString();
        		accessors.returnTypes[methCnt] = Void.class;

        		ArrayList<Integer> mPos = accessors.methodLookup.get(mi.getName());
        		if(mPos == null) {
        			mPos = new ArrayList<Integer>();
        			accessors.methodLookup.put(mi.getName(), mPos);
        		}
        		mPos.add(methCnt);
        		accessors.methods[methCnt++] = mi;
        	}
        	return accessors;
       }
       /**
        * populate the mutator methods for a given class: setXXX, void return, params length != 0, not bridge, not static, not native, not abstract
        * @param clazz target class
        * @param m clazz.getMethods()
        * @return this instance populated
        */
       public static MethodNamesAndParams reflectorMutatorFactory(Class<?> clazz, Method[] m) {  
      		MethodNamesAndParams mutators = new MethodNamesAndParams();
        	mutators.classClass = clazz;
        	mutators.className = mutators.classClass.getName();   
          	ArrayList<Integer> mutatorIndex = new ArrayList<Integer>();  
          	for(int i = m.length-1; i >= 0 ; i--) {
          		final int modifiers = m[i].getModifiers();
          		if(m[i].getName().startsWith("set")
          				&& !Modifier.isStatic(modifiers)
          				&& !Modifier.isNative(modifiers)
          				&& m[i].getParameterTypes().length != 0
          				&& !m[i].isBridge()
          				&& !Modifier.isAbstract(modifiers)
          				&& m[i].getReturnType() == Void.TYPE) {
          			mutators.methodNames.add(m[i].getName());
          			mutatorIndex.add(i);
          			if(DEBUG)
          				System.out.println("MethodNamesAndParams mutator Method "+m[i].toString());
          		}
          	}
          	//
          	// process mutators
          	// create arrays
          	mutators.methods = new Method[mutatorIndex.size()];
          	mutators.methodParams = new Class[mutatorIndex.size()][];
        	mutators.methodSigs = new String[mutatorIndex.size()];
        	mutators.returnTypes = new Class[mutatorIndex.size()];
        	int methCnt = 0;
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
        	return mutators;
       }

}
