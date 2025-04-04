package org.json.test;

import java.util.Map;
import java.util.Set;

import org.json.JSONObject;
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
	
	public static void testReflect(Object bean) {
		Set<FieldsAndMethods> r = ReflectFieldsAndMethods.reflect(bean);
		for(FieldsAndMethods fam : r) {
			System.out.println("---BEGIN--");
			System.out.println(fam);
			System.out.println("---END--");
		}
	}
	
	
	public static void main(String[] args) {
		Object[] o1 = new Object[] {new GenericBean<Long>((long) 123),
		new BrokenToString(),new ExceptionalBean(),new Fraction(1L,2L),new GenericBeanInt(321),new MyBeanCustomName(),
		new MyBeanCustomNameSubClass(),new MyEnumClass(),new MyNumber(), new MyNumberContainer(),new MyPublicClass(),
		new RecursiveBean("beaner"),Singleton.getInstance(),SingletonEnum.getInstance(),new StringsResourceBundle(),
		new WeirdList()};
		for(Object o : o1)
			testReflect(o);
	}

}
