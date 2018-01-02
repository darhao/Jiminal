package com.jimi.psh.entity;

/**
 * 通讯协议包基类
 * 
 * @author 沫熊工作室 <a href="http://www.darhao.cc">www.darhao.cc</a>
 */
public class Package {

	/**
	 * 包长度
	 */
	public int length;
	/**
	 * 协议类型
	 */
	public String protocol;
	/**
	 * 信息序列号
	 */
	public int serialNo;
	/**
	 * 错误校验码
	 */
	public int crc;
	
}
