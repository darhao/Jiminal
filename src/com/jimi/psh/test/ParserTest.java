package com.jimi.psh.test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.jimi.psh.constant.JustForTestLine;
import com.jimi.psh.core.PackageParser;
import com.jimi.psh.entity.JustForTestBoardNumPackage;
import com.jimi.psh.entity.JustForTestBoardNumReplyPackage;
import com.jimi.psh.entity.JustForTestControlReplyPackage;
import com.jimi.psh.entity.JustForTestHeartPackage;
import com.jimi.psh.entity.Package;
import com.jimi.psh.exception.CRCException;
import com.jimi.psh.exception.EnumValueNotExistException;
import com.jimi.psh.exception.PackageParseException;
import com.jimi.psh.exception.ProtocolNotMatchException;
import com.jimi.psh.exception.runtime.EnumClassNotFoundException;
import com.jimi.psh.exception.runtime.ReplyPackageNotMatchException;
import com.jimi.psh.util.FieldUtil;

/**
 * 解析器单元测试
 * <br>
 * <b>2017年12月27日</b>
 * @author 沫熊工作室 <a href="http://www.darhao.cc">www.darhao.cc</a>
 */
public class ParserTest{

	private String packagePath = "com.jimi.psh.entity";

	
	@Test
	public void crc() {
		List<Byte> bytes = Arrays.asList(new Byte[] {0x09, 0x48, 0x01, 0x5A, 0x1B, (byte) 0xAD, (byte) 0xA3, 0x03, 0x00, 0x02, 0x17, 0x5B});
		Assert.assertEquals(true, PackageParser.crc(bytes));
		List<Byte> bytes2 = Arrays.asList(new Byte[] {0x09, 0x42, 0x01, 0x5A, 0x1B, (byte) 0xAD, (byte) 0xA3, 0x03, 0x00, 0x02, 0x17, 0x5B});
		Assert.assertEquals(false, PackageParser.crc(bytes2));
	}
	
	@Test
	public void initPackageInfo() {
		JustForTestHeartPackage justForTestHeartPackage = new JustForTestHeartPackage();
		PackageParser.initPackageInfo(justForTestHeartPackage, packagePath);
		Assert.assertEquals(justForTestHeartPackage.length, 5 + 6);
		Assert.assertEquals(justForTestHeartPackage.protocol, "JustForTestHeart");
		
		JustForTestControlReplyPackage justForTestControlReplyPackage = new JustForTestControlReplyPackage();
		PackageParser.initPackageInfo(justForTestControlReplyPackage, packagePath);
		Assert.assertEquals(justForTestControlReplyPackage.length, 5 + 3);
		Assert.assertEquals(justForTestControlReplyPackage.protocol, "JustForTestControl");
	}
	
	@Test
	public void createReplyPackage() {
		JustForTestBoardNumPackage justForTestBoardNumPackage = new JustForTestBoardNumPackage();
		justForTestBoardNumPackage.serialNo = 999;
		JustForTestBoardNumReplyPackage justForTestBoardNumReplyPackage = (JustForTestBoardNumReplyPackage) PackageParser.createReplyPackage(justForTestBoardNumPackage, packagePath);
		Assert.assertEquals(justForTestBoardNumPackage.serialNo, justForTestBoardNumReplyPackage.serialNo);
		try{
			PackageParser.createReplyPackage(justForTestBoardNumPackage, "com.not.exist.test");
			Assert.fail();
		}catch (ReplyPackageNotMatchException e) {
			System.out.println(" '回复包未匹配' 测试异常捕捉成功");
		}
	}
	
	@Test
	public void serialize() throws ParseException {
		JustForTestHeartPackage justForTestHeartPackage = new JustForTestHeartPackage();
		justForTestHeartPackage.serialNo = 1024;
		justForTestHeartPackage.setAlarmEnabled(true);
		justForTestHeartPackage.setConveyorEnabled(true);
		justForTestHeartPackage.setInfraredEnabled(true);
		justForTestHeartPackage.setLine(JustForTestLine.L307);
		justForTestHeartPackage.setTimestamp(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2017-01-01 18:30:01"));
		List<Byte> bytes = PackageParser.serialize(justForTestHeartPackage, packagePath);
		List<Byte> bytes2 =  Arrays.asList(new Byte[] {0x0B, 0x48, 0x07, 0x58, 0x68, (byte) 0xDA, 0x29, 0x07, 0x04, 0x00, 0x5F, (byte) 0x9F});
		Assert.assertArrayEquals(bytes.toArray(), bytes2.toArray());
	}
	
	@Test
	public void parse() throws ParseException, PackageParseException {
		//参考对象
		JustForTestHeartPackage justForTestHeartPackage = new JustForTestHeartPackage();
		justForTestHeartPackage.serialNo = 1024;
		justForTestHeartPackage.setAlarmEnabled(true);
		justForTestHeartPackage.setConveyorEnabled(true);
		justForTestHeartPackage.setInfraredEnabled(true);
		justForTestHeartPackage.setLine(JustForTestLine.L307);
		justForTestHeartPackage.crc = 0x5F9F;
		justForTestHeartPackage.setTimestamp(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2017-01-01 18:30:01"));
		PackageParser.initPackageInfo(justForTestHeartPackage, packagePath);
		
		List<Byte> normal =  Arrays.asList(new Byte[] {0x0B, 0x48, 0x07, 0x58, 0x68, (byte) 0xDA, 0x29, 0x07, 0x04, 0x00, 0x5F, (byte) 0x9F});
		List<Byte> crcError =  Arrays.asList(new Byte[] {0x09 , 0x43 , (byte) 0xFF , 0x08 , 0x02 , 0x01 , 0x01 , 0x05 , (byte) 0xD3 , 0x15});
		List<Byte> enumError =  Arrays.asList(new Byte[] {0x09 , (byte) 0xFF , (byte) 0xFF , 0x08 , 0x02 , 0x01 , 0x00 , 0x05 ,  0x32 , (byte) 0xD8});
		List<Byte> protocolError =  Arrays.asList(new Byte[] {0x09 ,  0x01 , (byte) 0xFF , 0x08 , 0x02 , 0x01 , 0x00 , 0x05 ,  0x23 , (byte)0xA8});
		JustForTestHeartPackage heartPackage2 = (JustForTestHeartPackage) PackageParser.parse(normal, packagePath, false);
		Assert.assertEquals(FieldUtil.md5(justForTestHeartPackage), FieldUtil.md5(heartPackage2));
		try {
			PackageParser.parse(crcError, packagePath, false);
			Assert.fail();
		} catch (CRCException e) {
			System.out.println(" 'CRC校验错误' 测试异常捕捉成功");
			System.out.println(e.getMessage());
		}
		try {
			PackageParser.parse(enumError, packagePath, false);
			Assert.fail();
		} catch (EnumClassNotFoundException e) {
			System.out.println(" '字段枚举未匹配' 测试异常捕捉成功");
		}
		try {
			PackageParser.parse(protocolError, packagePath, false);
			Assert.fail();
		} catch (ProtocolNotMatchException e) {
			System.out.println(" '协议未匹配' 测试异常捕捉成功");
			System.out.println(e.getMessage());
		}
	}
	
	@Test
	public void finalTest() throws PackageParseException{
		try{
			List<Byte> bytes =  Arrays.asList(new Byte[] {0x08, 0x43, (byte) 0xFF, 0x01, 0x00, 0x00 , 0x05, (byte) 0xE2, 0x7C});
			PackageParser.parse(bytes, packagePath, true);
			Assert.fail();
		}catch (EnumValueNotExistException e) {
			System.out.println(" '枚举值未匹配' 测试异常捕捉成功");
			System.out.println(e.getMessage());
		}
		List<Byte> bytes =  Arrays.asList(new Byte[] {0x08, 0x43, (byte) 0x02, 0x01, 0x00, 0x00 , 0x05, 0x01, (byte) 0xAE});
		Package p = PackageParser.parse(bytes, packagePath, true);
		List<Byte> bytes2 = PackageParser.serialize(p, packagePath);
		Assert.assertArrayEquals(bytes.toArray(), bytes2.toArray());
	}
}
