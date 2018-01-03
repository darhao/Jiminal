package com.jimi.psh.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.jimi.psh.entity.JustForTestBoardNumPackage;
import com.jimi.psh.entity.JustForTestBoardNumReplyPackage;
import com.jimi.psh.entity.JustForTestBoardResetPackage;
import com.jimi.psh.entity.JustForTestBoardResetReplyPackage;
import com.jimi.psh.entity.JustForTestControl2Package;
import com.jimi.psh.entity.JustForTestControlPackage;
import com.jimi.psh.entity.JustForTestControlReplyPackage;
import com.jimi.psh.entity.JustForTestHeartPackage;
import com.jimi.psh.entity.JustForTestHeartReplyPackage;
import com.jimi.psh.entity.JustForTestLoginPackage;
import com.jimi.psh.entity.JustForTestLoginReplyPackage;
import com.jimi.psh.entity.Package;
import com.jimi.psh.util.BytesParser;
import com.jimi.psh.util.CRC16Util;
import com.jimi.psh.util.ClassScanner;
import com.jimi.psh.util.FieldUtil;

/**
 * 工具方法单元测试
 * <br>
 * <b>2017年12月27日</b>
 * @author 沫熊工作室 <a href="http://www.darhao.cc">www.darhao.cc</a>
 */
public class UtilsTest {

	@Test
	public void crc() {
		List<Byte> bytes = new ArrayList<Byte>();
		bytes.add((byte) 0x80);
		bytes.add((byte) 0x80);
		bytes.add((byte) 0x90);
		bytes.add((byte) 0x90);
		int result = CRC16Util.CRC16_X25(bytes);
		Assert.assertEquals(0x5088, result);
	}
	
	@Test
	public void copy() {
		Package a = new Package();
		a.length = 100;
		a.protocol= "aaa";
		JustForTestHeartPackage b = new JustForTestHeartPackage();
		FieldUtil.copy(a, b);
		Assert.assertEquals(a.protocol, b.protocol);
		Assert.assertEquals(b.length, b.length);
	}
	
	@Test
	public void searchClass() {
		List<Class> classes = ClassScanner.searchClass("com.jimi.psh.entity");
		List<Class> classes2 = new ArrayList<Class>();
		classes2.add(JustForTestBoardNumPackage.class);
		classes2.add(JustForTestBoardNumReplyPackage.class);
		classes2.add(JustForTestBoardResetPackage.class);
		classes2.add(JustForTestBoardResetReplyPackage.class);
		classes2.add(JustForTestControl2Package.class);
		classes2.add(JustForTestControlPackage.class);
		classes2.add(JustForTestControlReplyPackage.class);
		classes2.add(JustForTestHeartPackage.class);
		classes2.add(JustForTestHeartReplyPackage.class);
		classes2.add(JustForTestLoginPackage.class);
		classes2.add(JustForTestLoginReplyPackage.class);
		classes2.add(Package.class);
		Assert.assertArrayEquals(classes.toArray(), classes2.toArray());
	}
	
	@Test
	public void parseBytesToInteger() {
		List<Byte> bytes = Arrays.asList(new Byte[] {(byte) 0xFF, 0x0B, 0x0C, (byte) 0x80});
		int i = BytesParser.parseBytesToInteger(bytes);
		Assert.assertEquals(0xFF0B0C80, i);
	}
	
	@Test
	public void parseIntegerToBytes() {
		List<Byte> bytes = BytesParser.parseIntegerToBytes(0xFF0B0CFF);
		List<Byte> bytes2 = Arrays.asList(new Byte[] {(byte) 0xFF, 0x0B, 0x0C, (byte) 0xFF});
		Assert.assertArrayEquals(bytes.toArray(), bytes2.toArray());
	}
	
	@Test
	public void parseStringToBytes() {
		List<Byte> bytes = BytesParser.parseStringToBytes("80 78 20 DE CD 0B");
		List<Byte> bytes2 = Arrays.asList(new Byte[] {(byte) 0x80, 0x78, 0x20, (byte) 0xDE, (byte) 0xCD, 0x0B});
		Assert.assertArrayEquals(bytes.toArray(), bytes2.toArray());
	}
	
	@Test
	public void parseBytesToString() {
		List<Byte> bytes2 = Arrays.asList(new Byte[] {0x00, (byte) 0x98, 0x20, 0x2E, 0x2D, 0x3B});
		String result = BytesParser.parseBytesToString(bytes2);
		Assert.assertEquals(result, "00 98 20 2E 2D 3B");
	}
	
	@Test
	public void getBit() {
		boolean b = BytesParser.getBit((byte) 0b10010000, 4);
		Assert.assertEquals(b, true);
		b = BytesParser.getBit((byte) 0b10010000, 5);
		Assert.assertEquals(b, false);
	}
	
	@Test
	public void setBit() {
		byte b = BytesParser.setBit((byte) 0b10101010, 1, false);
		Assert.assertEquals((byte)0b10101000, b);
		b = BytesParser.setBit((byte) 0b00101010, 7, true);
		Assert.assertEquals((byte)0b10101010, b);
	}
	
}
