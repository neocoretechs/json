package org.json.test;

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

public class BeanTest {

	public BeanTest() {
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
	
	/**
	 * Test map generation from object
	 * @param bean
	 */
	public static void test(Object bean) {
		JSONObject o1 = new JSONObject(bean);
		System.out.println(o1);
		JSONObject o2 = new JSONObject(o1.toString());
		JSONObject o3 = new JSONObject();
		o3.put(bean.getClass().getName(),o2);
		System.out.println("---BEGIN--");
		o3.toMap().forEach((k,v)->{
			System.out.println("key:"+k+" val:"+v.getClass().getName()+" "+v);
			if(v instanceof Map) {
				JSONObject o4 = (JSONObject) JSONObject.wrap(v);
				o4.toMap().forEach((k4,v4)->{
					System.out.println("subkey:"+k4+" sub val:"+v4.getClass().getName()+" "+v4);
				});
			}
		});
		System.out.println("---END--");
	}
	/**
	 * Test reflection; reflect an object, iterate fields and methods
	 * @param bean
	 * @throws JSONException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public static void testReflect(Object bean) throws JSONException, IllegalArgumentException, IllegalAccessException {
		Set<FieldsAndMethods> r = ReflectFieldsAndMethods.reflect(bean);
		Iterator i = r.iterator();
		int c = 1;
		System.out.println("--Begin fields and methods");
		while(i.hasNext())
			System.out.println("fields and methods "+(c++)+":"+i.next());
		System.out.println("End fields and methods--");
		for(FieldsAndMethods fam : r) {
			System.out.println("---BEGIN--");
			String json = fam.fields.reflect(bean).toString();
			System.out.println("json string:"+json);
			new JSONObject(json).toMap().forEach((k,v)->{
				System.out.println("key:"+k+" val:"+v.getClass().getName()+" "+v);
				if(v instanceof Map) {
					JSONObject o4 = (JSONObject) JSONObject.wrap(v);
					o4.toMap().forEach((k4,v4)->{
						System.out.println("subkey:"+k4+" sub val:"+v4.getClass().getName()+" "+v4);
					});
				}
			});
			System.out.println("---END--");
		}
	}
	
	
	public static void main(String[] args) throws Exception {
		Object[] o1 = new Object[] {new GenericBean<Long>((long) 123),
		new ExceptionalBean(),new Fraction(1L,2L),new GenericBeanInt(321),new MyBeanCustomName(),
		new MyBeanCustomNameSubClass(),new MyEnumClass(),new MyNumber(), new MyNumberContainer(),new MyPublicClass(),
		new RecursiveBean("beaner"),Singleton.getInstance(),SingletonEnum.getInstance(),new StringsResourceBundle(),
		new WeirdList(),new FieldNamesAndConstructors()};
		for(Object o : o1)
			//testReflect(o);
			try {
				testObject(o);
			} catch(Exception e) {
				e.printStackTrace();
			}
	}

}
