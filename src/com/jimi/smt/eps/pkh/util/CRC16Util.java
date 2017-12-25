package com.jimi.smt.eps.pkh.util;

import java.util.List;

/**
 * CRC16Util
 * @author 沫熊工作室 <a href="http://www.darhao.cc">www.darhao.cc</a>
 */
public class CRC16Util {
	
	/**
	 * 传入一组字节集，调用CRC16-MODBUS算法，返回一个16位的校验码
	 * @param Buf
	 * @return
	 */
	public static int CRC16_MODBUS(byte[] Buf) {
		int CRC = 0xffff;
		for (int i = 0; i < Buf.length; i++) {
			CRC = CRC ^ (Buf[i] & 0xff);
			for (int j = 0; j < 8; j++) {
				if ((CRC & 0x01) == 1) {
					CRC = (CRC >> 1) ^ 0xA001;
				}
				else {
					CRC = CRC >> 1;
				}
			}
		}
		return CRC;
	}
	
	
	/**
	 * 传入一组字节集，调用CRC16-MODBUS算法，返回一个16位的校验码
	 * @param Buf
	 * @return
	 */
	public static int CRC16_MODBUS(List<Byte> bytes) {
		byte[] bs = new byte[bytes.size()];
		for (int i = 0; i < bs.length; i++) {
			bs[i] = bytes.get(i);
		}
		return CRC16_MODBUS(bs);
	}
	
	
	public static void main(String[] args) {
		System.out.println(CRC16_MODBUS(new byte[] {0x0,0x1,0x2,0x3}));
	}
	
}
