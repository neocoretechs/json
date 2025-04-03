package org.json.test;

import java.util.Map;

import org.json.JSONObject;
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
		o3.toMap().forEach((k,v)->{
			System.out.println("key:"+k+" val:"+v.getClass().getName()+" "+v);
			if(v instanceof Map) {
				JSONObject o4 = (JSONObject) JSONObject.wrap(v);
				o4.toMap().forEach((k4,v4)->{
					System.out.println("subkey:"+k4+" sub val:"+v4.getClass().getName()+" "+v4);
				});
			}
		});
		System.out.println("-----");
	}
	
	public static void main(String[] args) {
		Object o1 = new GenericBean<Long>((long) 123);
		test(o1);
		o1 = new BrokenToString();
		test(o1);
		o1 = new ExceptionalBean();
		test(o1);
		o1 = new Fraction(1L,2L);
		test(o1);
		o1 = new GenericBeanInt(321);
		test(o1);
		o1 = new MyBeanCustomName();
		test(o1);
		o1 = new MyBeanCustomNameSubClass();
		test(o1);
		o1 = new MyEnumClass();
		test(o1);
		o1 = new MyNumber();
		test(o1);
		o1 = new MyNumberContainer();
		test(o1);
		o1 = new MyPublicClass();
		test(o1);
		o1 = new RecursiveBean("beaner");
		test(o1);
		o1 = Singleton.getInstance();
		test(o1);
		o1 = SingletonEnum.getInstance();
		test(o1);
		o1 = new StringsResourceBundle();
		test(o1);
		o1 = new WeirdList();
		test(o1);
	}

}
