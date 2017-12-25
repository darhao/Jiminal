package com.jimi.smt.eps.pkh.parser;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.jimi.smt.eps.pkh.annotation.Parse;
import com.jimi.smt.eps.pkh.annotation.Protocol;
import com.jimi.smt.eps.pkh.entity.Package;
import com.jimi.smt.eps.pkh.exception.CRCException;
import com.jimi.smt.eps.pkh.exception.FieldEnumNotFoundException;
import com.jimi.smt.eps.pkh.exception.ProtocolNotMatchException;
import com.jimi.smt.eps.pkh.exception.ReplyPackageNotMatchException;
import com.jimi.smt.eps.pkh.util.CRC16Util;
import com.jimi.smt.eps.pkh.util.ClassScanner;
import com.jimi.smt.eps.pkh.util.FieldUtil;

/**
 * 包序列号和反序列化工具类<br>
 * 继承Package类，编写子类，并使用Parse注解各个字段<br>
 * 注意：规则如下<br>
 * 1.通讯包类名须以<b>"Package"</b>结尾<br>
 * 2.对应的回复包类名须以<b>"ReplyPackgae"</b>结尾<br>
 * 3.支持五种字段类型：字符串、布尔、整数、日期、枚举，分别对应的java类型为String, boolean, int, Date, 
 * 枚举类型需要指定enumClass属性，值为枚举类本身
 * <br>编写枚举类型时，枚举元素的顺序需要对应协议的值顺序，如：<br>
 * <pre>
public enum ReturnCode {
	SUCCEED,
	RELAY_FAILURE,
}
</pre>
 * 对应到文档里的话，那么0x00就应该表示SUCCEED,0x01就应该表示RELAY_FAILURE;
 * <br>
 * 以此类推...
 * <br>
 * @author 沫熊工作室 <a href="http://www.darhao.cc">www.darhao.cc</a>
 */
public class PackageParser {

	/**
	 * 把字节集解析成包
	 * @param bytes
	 * @param packagePath
	 * @param isReplyPackage
	 * @return
	 * @throws CRCException  
	 * @throws ProtocolNotMatchException 
	 */
	public static Package parse(List<Byte> bytes , String packagePath , boolean isReplyPackage) throws CRCException, ProtocolNotMatchException {
		if(!crc(bytes)) {
			throw new CRCException(bytes.toString());
		}
		//解析基类
		Package p = new Package();
		p.setLength(bytes.get(0));
		p.setSerialNo(parseBytesToInteger(bytes.subList(bytes.size() - 4, bytes.size() - 3)));
		p.setCrc(parseBytesToInteger(bytes.subList(bytes.size() - 2, bytes.size() - 1)));
		//判断协议类型
		byte protocalType = bytes.get(1);
		for (Class cls : ClassScanner.searchClass(packagePath)) {
			//匹配协议包
			Protocol protocol = (Protocol) cls.getAnnotation(Protocol.class);
			if(protocol == null) {
				continue;
			}
			if((protocol.code() == protocalType) && (cls.getSimpleName().endsWith("Reply") == isReplyPackage)){
				try {
					//解析协议名
					p.setProtocol(protocol.name());
					//创建实例
					Object target = cls.newInstance();
					//把父类数据复制到子类
					FieldUtil.copy(p, target);
					//根据注解把数据集填充到包对象并返回
					for (Field field : cls.getDeclaredFields()) {
						Parse parse = field.getAnnotation(Parse.class);
						if(parse == null) {
							continue;
						}
						//定位字节位置
						int a = parse.position()[0];
						int b = parse.position()[1];
						List<Byte> bs =  bytes.subList(a, b);
						//判断字段类型
						switch (field.getType().getName()) {
						case "java.util.Date":
							Date date = new Date(parseBytesToInteger(bs) * 1000);
							field.set(p, date);
							break;
						case "int":
						case "java.lang.Integer":
							int number = parseBytesToInteger(bs);
							field.set(p, number);
							break;
						case "java.lang.String":
							String string = parseBytesToString(bs);
							field.set(p, string);
							break;
						case "boolean":
						case "java.lang.Boolean":
							boolean bool = getBit(bytes.get(a), b);
							field.set(p, bool);
							break;
						default:
							//获取枚举类
							Class c = parse.enumClass();
							if(c.isEnum()) {
								throw new FieldEnumNotFoundException();
							}
							//设置枚举值
							Method method = c.getMethod("values", new Class[] {});
							Object[] objects = (Object[]) method.invoke(null, new Object[] {});
							Object enumValue = objects[bytes.get(a)];
							field.set(p, enumValue);
							break;
						}
						return p;
					}
				} catch (ReflectiveOperationException e) {
					e.printStackTrace();
				}
			}
		}
		//如果未找到匹配的包类
		throw new ProtocolNotMatchException(bytes.toString());
	}


	/**
	 * 把包序列化成字节集
	 * @param p
	 * @return
	 */
	public static List<Byte> serialize(Package p, String packagePath){
		//初始化父类信息
		initPackageInfo(p, packagePath);
		//创建字节集
		List<Byte> bytes = new ArrayList<Byte>();
		//序列化长度
		bytes.add((byte) p.getLength());
		//序列化协议
		bytes.add((byte) p.getClass().getAnnotation(Protocol.class).code());
		//添加占位字节，数量为：长度 - 5
		for (int i = 0; i < p.getLength() - 5; i++) {
			bytes.add((byte) 0);
		}
		try {
			//序列化信息主体，替换占位字节
			for (Field field : p.getClass().getDeclaredFields()) {
				Parse parse = field.getAnnotation(Parse.class);
				if(parse == null) {
					continue;
				}
				//定位字节位置
				int a = parse.position()[0];
				int b = parse.position()[1];
				//判断字段类型
				switch (field.getType().getName()) {
				case "java.util.Date":
					Date date = (Date) field.get(p);
					List<Byte> dateData = parseIntegerToBytes((int) (date.getTime() / 1000));
					for (int i = 0; i < dateData.size(); i++) {
						bytes.set(a+i, dateData.get(i));
					}
					break;
				case "int":
				case "java.lang.Integer":
					int number = (int) field.get(p);
					List<Byte> numberData = parseIntegerToBytes(number);
					for (int i = 0; i < numberData.size(); i++) {
						bytes.set(a+i, numberData.get(i));
					}
					break;
				case "java.lang.String":
					String string = (String) field.get(p);
					List<Byte> stringData = parseStringToBytes(string);
					for (int i = 0; i < stringData.size(); i++) {
						bytes.set(a+i, stringData.get(i));
					}
					break;
				case "boolean":
				case "java.lang.Boolean":
					boolean bool = (boolean) field.get(p);
					byte result = setBit(bytes.get(a), b, bool);
					bytes.set(a, result);
					break;
				default:
					//获取枚举类
					Class c = parse.enumClass();
					if(c.isEnum()) {
						throw new FieldEnumNotFoundException();
					}
					//获取枚举值
					Method method = c.getMethod("values", new Class[] {});
					Object[] objects = (Object[]) method.invoke(null, new Object[] {});
					Object enumValue = field.get(p);
					//匹配枚举值
					for (int i = 0; i < objects.length; i++) {
						if(enumValue.equals(objects[i])){
							bytes.set(a, (byte) i);
							break;
						}
					}
					break;
				}
			}
		} catch (ReflectiveOperationException e) {
			e.printStackTrace();
		}
		//序列化信息序列号
		bytes.addAll(parseIntegerToBytes(p.getSerialNo()));
		//序列化crc
		bytes.addAll(parseIntegerToBytes(CRC16Util.CRC16_MODBUS(bytes)));
		return bytes;
	}
	
	
	/**
	 * 根据包，构建回复包实例，并复制信息序列号
	 * @param p
	 * @return
	 */
	public static Package createReplyPackage(Package p, String packagePath) {
		//根据类名匹配回复包类
		for (Class cls : ClassScanner.searchClass(packagePath)) {
			//匹配
			String string1 = cls.getSimpleName();
			String string2 = p.getClass().getSimpleName();
			string1 = string1.substring(0, string1.indexOf("ReplyPackage"));
			string2 = string2.substring(0, string2.indexOf("Package"));
			if(string1.equals(string2)) {
				try {
					//创建对象
					Package r = (Package) cls.newInstance();
					//复制信息序列号
					r.setSerialNo(p.getSerialNo());
					return r;
				} catch (ReflectiveOperationException e) {
					e.printStackTrace();
				}
			}
		}
		throw new ReplyPackageNotMatchException();
	}
	
	
	/**
	 * 初始化包的基本信息（基本信息包括：长度、协议名）
	 * @param p 需要进行初始化的包对象
	 * @return
	 */
	private static void initPackageInfo(Package p, String packagePath) {
		//计算长度（已知长度：协议号+信息序列号+校验位=5）
		int length = 5;
		for (Field field : p.getClass().getDeclaredFields()) {
			//过滤没有注解的字段
			Parse parse = field.getAnnotation(Parse.class);
			if(parse != null) {
				length += parse.position()[1];
			}
		}
		p.setLength(length);
		//获取协议名
		String protocol = p.getClass().getAnnotation(Protocol.class).name();
		p.setProtocol(protocol);
		throw new RuntimeException("该类无法创建成通讯包对象");
	}
	
	
	/**
	 * 对字节集进行CRC校验，无误返回true
	 * @param bytes
	 * @return
	 */
	private static boolean crc(List<Byte> bytes) {
		int calculationResults = CRC16Util.CRC16_MODBUS(bytes.subList(0, bytes.size() - 2));
		int record = parseBytesToInteger(bytes.subList(bytes.size() - 2, bytes.size() - 1));
		return calculationResults == record;
	}


	/**
	 * 取字节的某一位的布尔值，从右边的0算起
	 * @param a
	 * @param b
	 * @return
	 */
	private static boolean getBit(byte a, int b) {
		return ((a >> b) & 0b00000001) == 1 ? true : false;
	}


	/**
	 *  设置字节的某一位的布尔值，从右边的0算起，返回被设置后的字节
	 * @param byte1
	 * @param b
	 * @param bool
	 * @return
	 */
	private static byte setBit(byte a, int b, boolean bool) {
		if(bool) {
			int temp = 0b00000001;
			temp <<= b;
			a |= temp;
		}else {
			int temp = 0b1111111101111111;
			temp >>= (7 - b);
			a &= temp;
		}
		return a;
	}


	/**
	 * 把字节集转换成十六进制字符串，中间用空格隔开
	 * @param bs
	 * @return
	 */
	private static String parseBytesToString(List<Byte> bs) {
		StringBuffer sb = new StringBuffer();
		for (Byte b1 : bs) {
			sb.append(Integer.toHexString(b1));
			sb.append(" ");
		}
		return sb.toString().trim();
	}
	
	
	/**
	 * 把字节集转换成十六进制字符串，中间用空格隔开
	 * @param bs
	 * @return
	 */
	private static List<Byte> parseStringToBytes(String string) {
		List<Byte> bytes = new ArrayList<Byte>();
		String[] strings =  string.split(" ");
		for (String s : strings) {
			bytes.add((byte) Integer.parseInt(s, 16));
		}
		return bytes;
	}


	/**
	 * 把1-4个字节集拼接成32位整型
	 * @param bytes
	 * @return
	 */
	private static int parseBytesToInteger(List<Byte> bytes) {
		int i = 0;
		for (Byte b : bytes) {
			i += b;
			i <<= 8;
		}
		return i;
	}
	
	
	/**
	 * 把32位整型分解成1-4个字节集
	 * @param bytes
	 * @return
	 */
	private static List<Byte> parseIntegerToBytes(int i) {
		List<Byte> bytes = new ArrayList<Byte>();
		while(i != 0) {
			bytes.add((byte) i);
			i >>= 8;
		}
		Collections.reverse(bytes);
		return bytes;
	}
}
