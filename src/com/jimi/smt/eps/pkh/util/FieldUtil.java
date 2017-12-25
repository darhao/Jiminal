package com.jimi.smt.eps.pkh.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;

/**
 * 对象属性工具类
 * @author 沫熊工作室 <a href="http://www.darhao.cc">www.darhao.cc</a>
 */
public class FieldUtil {
	
	
	/**
	 * 根据各属性计算MD5,相同的对象将会得出相同的MD5值
	 * @param object
	 * @return
	 */
	public static String md5(Object object) {
		Method[] methods = object.getClass().getMethods();
		sortByMethodName(methods);
		StringBuffer sb = new StringBuffer();
		MessageDigest digester = null;
		try {
			digester = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e1) {
			e1.printStackTrace();
		}
		for (Method method : methods) {
			if(method.getName().startsWith("get")) {
				try {
					String string = new String();
					Object object2 = method.invoke(object, new Object[] {});
					if(object2 == null || object2.equals("")) {
						continue;
					}else {
						string = object2.toString();
					}
					sb.append(new String(digester.digest(string.getBytes())));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		StringBuffer sb2 = new StringBuffer();
		byte[] b = digester.digest(sb.toString().getBytes());
		for (int i = 0; i < b.length; i++) {
		    String tmp = Integer.toHexString(b[i] & 0xFF);
		    if (tmp.length() == 1) {
		    sb2.append("0" + tmp);
		    } else {
		    sb2.append(tmp);
		    }
		}
		return sb2.toString();
	}


	/**
	 * 打印该对象所有公有属性，需要get方法。
	 * @param object
	 */
	public static void print(Object object) {
		Method[] methods = object.getClass().getMethods();
		sortByMethodName(methods);
		for (Method method : methods) {
			if(method.getName().startsWith("get")) {
				try {
					System.out.println(method.getName().substring(3) + " = " +method.invoke(object, new Object[] {}));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		System.out.println("======================================================");
	}

	
	/**
	 * 拷贝相同名称的属性
	 * @param source
	 * @param target
	 */
	public static void copy(Object source, Object target) {
		Field[] sourceFields = source.getClass().getDeclaredFields();
		Field[] targetFields = target.getClass().getDeclaredFields();
		for (Field sourceField : sourceFields) {
			for (Field targetField : targetFields) {
				if(sourceField.getName().equals(targetField.getName())) {
					try {
						sourceField.setAccessible(true);
						targetField.setAccessible(true);
						Object value = sourceField.get(source);
						targetField.set(target, value);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	

	private static void sortByMethodName(Method[] methods) {
		ArrayList<Method> methods2 = new ArrayList<Method>(methods.length);
		for (Method method : methods) {
			methods2.add(method);
		}
		methods2.sort(new Comparator<Method>() {
			@Override
			public int compare(Method o1, Method o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		methods2.toArray(methods);
	}
	
	
	
}
