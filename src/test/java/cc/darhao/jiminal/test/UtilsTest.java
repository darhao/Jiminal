package cc.darhao.jiminal.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import cc.darhao.jiminal.core.BasePackage;
import cc.darhao.jiminal.entity.JustForTestBoardNumPackage;
import cc.darhao.jiminal.entity.JustForTestBoardNumReplyPackage;
import cc.darhao.jiminal.entity.JustForTestBoardResetPackage;
import cc.darhao.jiminal.entity.JustForTestBoardResetReplyPackage;
import cc.darhao.jiminal.entity.JustForTestControl2Package;
import cc.darhao.jiminal.entity.JustForTestControlPackage;
import cc.darhao.jiminal.entity.JustForTestControlReplyPackage;
import cc.darhao.jiminal.entity.JustForTestHeartPackage;
import cc.darhao.jiminal.entity.JustForTestHeartReplyPackage;
import cc.darhao.jiminal.entity.JustForTestLoginPackage;
import cc.darhao.jiminal.entity.JustForTestLoginReplyPackage;
import cc.darhao.dautils.api.BytesParser;
import cc.darhao.dautils.api.CRC16Util;
import cc.darhao.dautils.api.ClassScanner;
import cc.darhao.dautils.api.FieldUtil;

/**
 * 工具方法单元测试
 * <br>
 * <b>2017年12月27日</b>
 * @author 沫熊工作室 <a href="http://www.darhao.cc">www.darhao.cc</a>
 */
public class UtilsTest {
	
	public void p(int a) {
		System.out.println(Integer.toBinaryString(a) + " = " + a);
	}
	
	public void testNumberType() {
		System.out.println("字面量测试：高位不写，默认是0；字面量永远是int类型");
		int a = 0xF;
		int b = 0xFF;
		p(a);
		p(b);
		System.out.println("32位以下类型，移位测试：满足-128~127之间的int值可以赋值给byte，以此类推；无符号右移只对int以及long有效");
		System.out.println("32位以下类型，移位测试：低级类型左移溢出不会转换成更高级的类型，而是会变成负数，继续左移将变成0，再左移将出现奇怪的数字");
		byte c = 0xF;
		byte c1 = 0xF;
		byte c2 = 0xFFFFFFFF;
		c1 >>= 4;
		c2 >>= 4;
		p(c2);
		c2 >>= 2;
		p(c2);
		c2 >>>= 2;//无符号右移无效
		p(c2);
		int c3 = -1;
		c3 >>>= 8;//无符号右移有效
		p(c3);
		p(c1);
		p(c);
		c <<= 1;
		p(c);
		c >>= 1;
		p(c);
		c <<= 4;
		p(c);
		c >>= 4;
		p(c);
		c <<= 4;
		p(c);
		c >>>= 4;
		p(c);
		short e = 0x1FFF;
		p(e);
		e <<= 2;
		p(e);
		e <<= 2;
		p(e);
		int e1 = 0x1FFF;
		e1 <<= 4;
		p(e1);
		System.out.println("自动类型转换测试：升级后符号位不变");
		byte f = 0x32;
		p(f);
		short f1 = f;
		p(f1);
		int f2 = f1;
		p(f2);
		byte g = 0xFFFFFF82;
		p(g);
		short g1 = g;
		p(g1);
		int g2 = g1;
		p(g2);
		byte z = (byte) 0x82;
		p(z);
		int z1 = z;
		p(z1);
		System.out.println("强制类型转换测试：降级会丢失符号位");
		int h = 0x10;
		short h1 = (short) h;
		p(h);
		p(h1);
		int i = 0x10101010;
		short i1 = (short) i;
		p(i);
		p(i1);
		int j = 0xFF222222;
		short j1 = (short) j;
		p(j);
		p(j1);
	}
	
	
	@Test
	public void negativeIntegerBytes() {
		List<Byte> bytes = Arrays.asList(new Byte[] {(byte) 0xF4, (byte) 0xF3, (byte) 0x80});
		List<Byte> bytes2 = BytesParser.negativeIntegerBytes(Arrays.asList(new Byte[] {(byte) 0xFF, 0x0B, 0x0C, (byte) 0x80}));
		Assert.assertArrayEquals(bytes.toArray(), bytes2.toArray());
	} 
	
	
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
		BasePackage a = new BasePackage();
		a.length = 100;
		a.protocol= "aaa";
		JustForTestHeartPackage b = new JustForTestHeartPackage();
		FieldUtil.copy(a, b);
		Assert.assertEquals(a.protocol, b.protocol);
		Assert.assertEquals(b.length, b.length);
	}
	
	@Test
	public void searchClass() {
		List<Class> classes = ClassScanner.searchClass("cc.darhao.jiminal.entity");
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
