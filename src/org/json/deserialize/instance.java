package org.json.deserialize;
import java.io.*;
import java.util.*;

import org.json.JSONObject;

/**
 * Represents an instance of a non-enum, non-Class, non-ObjectStreamClass, 
 * non-array class, including the non-transient field values, for all classes in its
 * hierarchy and inner classes.
 */
public class instance extends contentbase implements JsonOutInterface {
    /**
     * Collection of field data, organized by class description.  
     */
    public Map<classdesc, Map<field, Object>> fielddata;

    /**
     * Class description for this instance.
     */
    public classdesc classdesc;

    /**
     * Constructor.
     */
    public instance() {
        super(contenttype.INSTANCE);
        this.fielddata = new HashMap<classdesc, Map<field, Object>>();
    }
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(classdesc.name).append(' ').append("_h").append(jdeserialize.hex(handle))
            .append(" = r_").append(jdeserialize.hex(classdesc.handle)).append(";  ");
        //sb.append("// [instance " + jdeserialize.hex(handle) + ": " + jdeserialize.hex(classdesc.handle) + "/" + classdesc.name).append("]");
        return sb.toString();
    }
    /**
     * Object annotation data.
     */
    public Map<classdesc, List<content>> annotations;

	@Override
	public void toJson(JSONObject json) {
        for(field f: fielddata.get(classdesc).keySet()) {
            Object o = fielddata.get(classdesc).get(f);
        	System.out.println("instanceToJson classdesc:"+classdesc+" field:"+f+" object:"+o+" object class:"+o.getClass().getName());
            if(o instanceof content) {
                content c = (content)o;
                if(c instanceof JsonOutInterface) {
                	json.append("type", f.type.getJavaType());
                	((JsonOutInterface)c).toJson(json);
                } else
                	if(c instanceof JsonArrayOutInterface)	
                		((JsonArrayOutInterface)c).toJson(json);
                	else
                		System.out.println("Unknown content:"+c+" class:"+c.getClass().getName());
            } else {
            	json.append("type", f.type.getJavaType());
                json.append(f.name, o);
            }
        }
		
	}
}
