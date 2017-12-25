package com.jimi.smt.eps.pkh.entity;

/**
 * 通讯协议包基类
 * 
 * @author 沫熊工作室 <a href="http://www.darhao.cc">www.darhao.cc</a>
 */
public class Package {

	/**
	 * 包长度
	 */
	protected int length;
	/**
	 * 协议类型
	 */
	protected String protocol;
	/**
	 * 信息序列号
	 */
	protected int serialNo;
	/**
	 * 错误校验码
	 */
	protected int crc;
	
	
	public int getLength() {
		return length;
	}
	public void setLength(int length) {
		this.length = length;
	}
	public String getProtocol() {
		return protocol;
	}
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}
	public int getSerialNo() {
		return serialNo;
	}
	public void setSerialNo(int serialNo) {
		this.serialNo = serialNo;
	}
	public int getCrc() {
		return crc;
	}
	public void setCrc(int crc) {
		this.crc = crc;
	}
	
}
