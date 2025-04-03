package org.json.reflect;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;


/**
* A basic Serializable helper class used with {@link ServerInvokeMethod} with arrays of Method names and 
* parameters for a target class. Contains serializable representation of non serializable elements and results of method call set after
* invocation.
* @author Jonathan Groff Copyright (C) NeoCoreTechs, Inc. 2025
*/
public final class MethodNamesAndParams implements Serializable {
       static final long serialVersionUID = 8837760295724028863L;
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

}
