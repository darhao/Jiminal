package com.jimi.jiminal.core;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.jimi.jiminal.annotation.Parse;
import com.jimi.jiminal.annotation.Protocol;
import com.jimi.jiminal.entity.BasePackage;
import com.jimi.jiminal.exception.CRCException;
import com.jimi.jiminal.exception.EnumValueNotExistException;
import com.jimi.jiminal.exception.PackageParseException;
import com.jimi.jiminal.exception.ProtocolNotMatchException;
import com.jimi.jiminal.exception.runtime.EnumClassNotFoundException;
import com.jimi.jiminal.exception.runtime.ReplyPackageNotMatchException;
import com.jimi.jiminal.util.BytesParser;
import com.jimi.jiminal.util.CRC16Util;
import com.jimi.jiminal.util.ClassScanner;
import com.jimi.jiminal.util.FieldUtil;

/**
 * 包序列号和反序列化工具类
 * @author 沫熊工作室 <a href="http://www.darhao.cc">www.darhao.cc</a>
 */
public class PackageParser {

	/**
	 * 把字节集解析成包
	 * @param isReplyPackage 该布尔值用于区分被解析字节集是正常包还是回复包
	 * @throws CRCException  CRC校验失败时抛出
	 * @throws ProtocolNotMatchException 协议未能匹配时抛出
	 * @throws EnumValueNotExistException 
	 */
	public static BasePackage parse(List<Byte> bytes , String packagePath , boolean isReplyPackage) throws PackageParseException {
		if(!crc(bytes)) {
			throw new CRCException(bytes);
		}
		//解析基类
		BasePackage p = new BasePackage();
		p.length = bytes.get(0);
		p.serialNo = (short) BytesParser.parseBytesToInteger(bytes.subList(bytes.size() - 4, bytes.size() - 2));
		p.crc = (short) BytesParser.parseBytesToInteger(bytes.subList(bytes.size() - 2, bytes.size()));
		//判断协议类型
		byte protocalType = bytes.get(1);
		//获取信息内容字节列表
		List<Byte> bodyBytes = bytes.subList(2, bytes.size() - 4);
		for (Class cls : ClassScanner.searchClass(packagePath)) {
			//匹配协议包
			Protocol protocol = (Protocol) cls.getAnnotation(Protocol.class);
			if(protocol == null) {
				continue;
			}
			if((protocol.value() == protocalType) && (cls.getSimpleName().endsWith("ReplyPackage") == isReplyPackage)){
				try {
					//解析协议名
					String protocolName = cls.getSimpleName()
							.substring(0, cls.getSimpleName().replaceAll("Reply", "").indexOf("Package"));
					p.protocol = protocolName;
					//创建实例
					BasePackage target = (BasePackage) cls.newInstance();
					//把父类数据复制到子类
					FieldUtil.copy(p, target);
					//根据注解把数据集填充到包对象并返回
					for (Field field : cls.getDeclaredFields()) {
						Parse parse = field.getAnnotation(Parse.class);
						if(parse == null) {
							continue;
						}
						//定位字节位置
						int a = parse.value()[0];
						int b = parse.value()[1];
						List<Byte> bs;
						//设置权限
						field.setAccessible(true);
						//判断字段类型
						switch (field.getType().getName()) {
						case "java.util.Date":
							bs =  bodyBytes.subList(a, a+b);
							Date date = new Date((long)BytesParser.parseBytesToInteger(bs) * 1000);
							field.set(target, date);
							break;
						case "int":
						case "java.lang.Integer":
							bs =  bodyBytes.subList(a, a+b);
							//拷贝为可写List
							List<Byte> writableList = new ArrayList<Byte>();
							for (Byte b1 : bs) {
								writableList.add(b1);
							}
							//判断是否有符号：如果有
							if(parse.sign()) {
								//获取符号位：如果为负
								if(BytesParser.getBit(bs.get(0).byteValue(), 7)) {
									//剩余高字节补1
									int oldSize = bs.size();
									for (int i = 0; i < 4 - oldSize; i++) {
										writableList.add(0,(byte) 0xFF);
									}
								}
							}
							int number = BytesParser.parseBytesToInteger(writableList);
							field.set(target, number);
							break;
						case "java.lang.String":
							bs =  bodyBytes.subList(a, a+b);
							String string = BytesParser.parseBytesToString(bs);
							field.set(target, string);
							break;
						case "boolean":
						case "java.lang.Boolean":
							boolean bool = BytesParser.getBit(bodyBytes.get(a), b);
							field.set(target, bool);
							break;
						default:
							//获取枚举类
							Class c = field.getType();
							if(!c.isEnum()) {
								throw new EnumClassNotFoundException(bytes);
							}
							//设置枚举值
							bs =  bodyBytes.subList(a, a+b);
							int enumIndex = BytesParser.parseBytesToInteger(bs);
							Method method = c.getMethod("values", new Class[] {});
							Object[] objects = (Object[]) method.invoke(null, new Object[] {});
							try {
								Object enumValue = objects[enumIndex];
								field.set(target, enumValue);
							}catch (ArrayIndexOutOfBoundsException e) {
								throw new EnumValueNotExistException(bytes);
							}
							break;
						}
					}
					return target;
				} catch (ReflectiveOperationException e) {
					e.printStackTrace();
				}
			}
		}
		//如果未找到匹配的包类
		throw new ProtocolNotMatchException(bytes);
	}


	/**
	 * 把包序列化成字节集
	 */
	public static List<Byte> serialize(BasePackage p, String packagePath){
		//初始化父类信息
		initPackageInfo(p, packagePath);
		//创建字节集
		List<Byte> bytes = new ArrayList<Byte>();
		//序列化长度
		bytes.add((byte) p.length);
		//序列化协议
		bytes.add((byte) p.getClass().getAnnotation(Protocol.class).value());
		//创建占位字节列表，长度为：包长度减去5
		List<Byte> bodyBytes = new ArrayList<Byte>(p.length - 5);
		for (int i = 0; i < p.length - 5; i++) {
			bodyBytes.add((byte) 0);
		}
		try {
			//序列化信息主体，替换占位字节
			for (Field field : p.getClass().getDeclaredFields()) {
				Parse parse = field.getAnnotation(Parse.class);
				if(parse == null) {
					continue;
				}
				//定位字节位置
				int a = parse.value()[0];
				int b = parse.value()[1];
				//设置权限
				field.setAccessible(true);
				//判断字段类型
				switch (field.getType().getName()) {
				case "java.util.Date":
					Date date = (Date) field.get(p);
					List<Byte> dateData = BytesParser.parseIntegerToBytes((int) (date.getTime() / 1000));
					for (int i = 0; i < dateData.size(); i++) {
						bodyBytes.set(a+i, dateData.get(i));
					}
					break;
				case "int":
				case "java.lang.Integer":
					int number = (int) field.get(p);
					List<Byte> numberData = BytesParser.parseIntegerToBytes(number);
					//根据字段长度截取整数
					if(numberData.size() >= b) {
						numberData = numberData.subList(numberData.size() - b, numberData.size());
					}
					for (int i = 0; i < numberData.size(); i++) {
						bodyBytes.set(a+i, numberData.get(i));
					}
					break;
				case "java.lang.String":
					String string = (String) field.get(p);
					List<Byte> stringData = BytesParser.parseStringToBytes(string);
					for (int i = 0; i < stringData.size(); i++) {
						bodyBytes.set(a+i, stringData.get(i));
					}
					break;
				case "boolean":
				case "java.lang.Boolean":
					boolean bool = (boolean) field.get(p);
					byte result = BytesParser.setBit(bodyBytes.get(a), b, bool);
					bodyBytes.set(a, result);
					break;
				default:
					//获取枚举类
					Class c = field.getType();
					if(!c.isEnum()) {
						throw new EnumClassNotFoundException(bytes);
					}
					//获取枚举值
					Method method = c.getMethod("values", new Class[] {});
					Object[] objects = (Object[]) method.invoke(null, new Object[] {});
					Object enumValue = field.get(p);
					//匹配枚举值
					for (int i = 0; i < objects.length; i++) {
						if(enumValue.equals(objects[i])){
							List<Byte> enumIndex = BytesParser.parseIntegerToBytes(i);
							//根据字段长度截取整数
							if(enumIndex.size() >= b) {
								enumIndex = enumIndex.subList(enumIndex.size() - b, enumIndex.size());
							}
							for (int j = 0; j < enumIndex.size(); j++) {
								bodyBytes.set(a+j, enumIndex.get(j));
							}
							break;
						}
					}
					break;
				}
			}
		} catch (ReflectiveOperationException e) {
			e.printStackTrace();
		}
		//拼接
		bytes.addAll(bodyBytes);
		//序列化信息序列号（如果序列号为255以下则高位补零）
		List<Byte> serialNoBytes = BytesParser.parseIntegerToBytes(p.serialNo);
		if(serialNoBytes.size() == 1) {
			serialNoBytes.add(0, (byte) 0x00);
		}
		bytes.addAll(serialNoBytes);
		//序列化crc
		List<Byte> crcData = BytesParser.parseIntegerToBytes(CRC16Util.CRC16_X25(bytes));
		//如果有4个字节则去掉前两个
		crcData = crcData.subList(crcData.size() - 2, crcData.size());
		bytes.addAll(crcData);
		return bytes;
	}
	
	
	/**
	 * 根据包，构建回复包实例，并复制信息序列号
	 */
	public static BasePackage createReplyPackage(BasePackage p, String packagePath) {
		//根据类名匹配回复包类
		for (Class cls : ClassScanner.searchClass(packagePath)) {
			//匹配
			String string1 = cls.getSimpleName();
			if(!string1.contains("ReplyPackage")) {
				continue;
			}
			String string2 = p.getClass().getSimpleName();
			string1 = string1.substring(0, string1.indexOf("ReplyPackage"));
			string2 = string2.substring(0, string2.indexOf("Package"));
			if(string1.equals(string2)) {
				try {
					//创建对象
					BasePackage r = (BasePackage) cls.newInstance();
					//复制信息序列号、协议
					r.serialNo = p.serialNo;
					r.protocol = p.protocol;
					return r;
				} catch (ReflectiveOperationException e) {
					e.printStackTrace();
				}
			}
		}
		throw new ReplyPackageNotMatchException(p);
	}
	
	
	/**
	 * 初始化包的基本信息（基本信息包括：长度、协议名）
	 * @param p 需要进行初始化的包对象
	 */
	public static void initPackageInfo(BasePackage p, String packagePath) {
		//计算长度（已知长度：协议号+信息序列号+校验位=5）
		byte length = 5;
		//创建用于存储Boolean类型字段字节编号的Set
		Set<Integer> byteNoSet = new HashSet<Integer>();
		for (Field field : p.getClass().getDeclaredFields()) {
			//过滤没有注解的字段
			Parse parse = field.getAnnotation(Parse.class);
			if(parse != null) {
				if(field.getType().equals(Boolean.class) || field.getType().getName().equals("boolean")) {
					byteNoSet.add(parse.value()[0]);
				}else {
					length += parse.value()[1];
				}
			}
		}
		length += byteNoSet.size();
		p.length = length;
		//解析协议名
		String protocolName = p.getClass().getSimpleName()
				.substring(0, p.getClass().getSimpleName().replaceAll("Reply", "").indexOf("Package"));
		p.protocol = protocolName;
	}
	
	
	/**
	 * 对字节集进行CRC校验，无误返回true
	 */
	public static boolean crc(List<Byte> bytes) {
		short calculationResults = CRC16Util.CRC16_X25(bytes.subList(0, bytes.size() - 2));
		short record = (short) BytesParser.parseBytesToInteger(bytes.subList(bytes.size() - 2, bytes.size()));
		return calculationResults == record;
	}


}
