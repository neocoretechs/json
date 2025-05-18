package org.json.test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.reflect.FieldNamesAndConstructors;
import org.json.reflect.ReflectFieldsAndMethods;
import org.json.reflect.ReflectFieldsAndMethods.FieldsAndMethods;
import org.json.test.data.BrokenToString;
import org.json.test.data.ExceptionalBean;
import org.json.test.data.Fraction;
import org.json.test.data.GenericBean;
import org.json.test.data.GenericBeanInt;
import org.json.test.data.MyBeanCustomName;
import org.json.test.data.MyBeanCustomNameSubClass;
import org.json.test.data.MyEnumClass;
import org.json.test.data.MyNumber;
import org.json.test.data.MyNumberContainer;
import org.json.test.data.MyPublicClass;
import org.json.test.data.RecursiveBean;
import org.json.test.data.Singleton;
import org.json.test.data.SingletonEnum;
import org.json.test.data.StringsResourceBundle;
import org.json.test.data.WeirdList;

public class BagTest {
	ArrayList bag;
	Object[] array;
	public BagTest() {
	}
	
	/**
	 * Simulate round trip via wire
	 * @param bean
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 * @throws JSONException 
	 * @throws InstantiationException 
	 */
	public static void testObject(Object bean) throws JSONException, IllegalArgumentException, IllegalAccessException, InstantiationException {
		//this constructor wont add class name as JSON field
		//JSONObject o1 = new JSONObject(bean);
		//-------
		//Set<FieldsAndMethods> r = ReflectFieldsAndMethods.reflect(bean);
		//for(FieldsAndMethods fam : r) {
			//String json = fam.fields.reflect(bean).toString();
		String json = JSONObject.toJson(bean);
			System.out.println("reflected JSON:"+json);
			JSONObject o2 = new JSONObject(json);
			System.out.println(o2.toObject());
			System.out.println("================");
		//}
	}
	
	
	public static void main(String[] args) throws Exception {
		BagTest bagtest = new BagTest();
		bagtest.bag = new ArrayList();
		bagtest.array = new Object[5];
		bagtest.array[0] = "1";
		bagtest.array[1] = "11";
		bagtest.array[2] = "111";
		bagtest.bag.add("2");
		bagtest.bag.add("22");
		bagtest.bag.add("222");
			try {
				testObject(bagtest);	
			} catch(Exception e) {
				e.printStackTrace();
			}
			com.neocoretechs.relatrix.Result1 r = new com.neocoretechs.relatrix.Result1("yo");
			r.packForTransport();
			System.out.println("r="+r);
			System.out.println( new JSONObject(r));
	}

}
