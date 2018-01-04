package com.jimi.psh.core;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * sadasdasdas:几米物联 —— 包通讯基类<br>可以做服务器亦可以做客户端<br>
 * 需要指定一个通讯包的包路径<br>
 * 可配置项：<br>
 * <ul>
 * 	<li>起始、结束位（限定各两个字节）
 * 	<li>超时时间（单位毫秒）
 * 	<li>最多重试次数
 * </ul>
 * <br>
 * <b>通讯包编写说明：</b>
 * <br>
 *  * 继承Package类，编写子类，使用Protocol注解整个类，并使用Parse注解各个字段<br>
 * 注意：规则如下<br>
 * 1.通讯包类名须以<b>"Package"</b>结尾<br>
 * 2.对应的回复包类名须以<b>"ReplyPackgae"</b>结尾<br>
 * 3.支持五种字段类型：字符串、布尔、整数、日期、枚举，分别对应的java类型为String, boolean, int, Date, Enum
 * <br>编写枚举类型时，枚举元素的顺序需要对应协议的值顺序，如：<br>
 * <pre>
public enum JustForTestReturnCode {
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
public abstract class Communicator {
	
	/**
	 * 远程设备通信端口
	 */
	protected int remotePort = 23333;
	
	/**
	 * 本地服务监听端口
	 */
	protected int localPort = 23333;
	
	/**
	 * 信息序列号
	 */
	protected int serialNo = 0;
	
	/**
	 * 中控的ip地址
	 */
	protected String remoteIp;
	
	/**
	 * 已重试次数
	 */
	protected int retriedTimes = 0;
	
	/**
	 * 最大重试次数
	 */
	protected int maxRetryTimes = 3;
	
	/**
	 * 超时时间（毫秒 ）
	 */
	protected int timeout = 5000;
	
	/**
	 * 起始标志位 
	 */
	protected byte[] startFlags = new byte[]{(byte) 0x80, (byte) 0x80};
	
	/**
	 * 结束标志位
	 */
	protected byte[] endFlags = new byte[]{(byte) 0x0D, (byte) 0x0A};
	
	
	/**
	 * 通讯包类的包名
	 */
	protected String packagePath;
	
	
	/**
	 *客户端 
	 */
	protected Socket client;
	
	/**
	 * 服务器
	 */
	protected ServerSocket server;
	
	/**
	 * 服务器接收的客户列表
	 */
	protected List<Socket> serverAccpetClients;
	
	
	/**
	 * 创建一个通讯器
	 * @param localPort 本地服务端口
	 * @param packagePath 通讯包类的包目录名
	 */
	public Communicator(int localPort, String packagePath){
		this(null, 0, localPort, packagePath);
	}
	
	
	/**
	 * 创建一个通讯器
	 * @param remoteIp 远程设备IP
	 * @param remotePort 远程设备端口
	 * @param packagePath 通讯包类的包目录名
	 */
	public Communicator(String remoteIp ,int remotePort, String packagePath){
		this(remoteIp, remotePort, 0, packagePath);
	}
	
	/**
	 * 创建一个通讯器
	 * @param remoteIp 远程设备IP
	 * @param remotePort 远程设备端口
	 * @param localPort 本地服务端口
	 * @param packagePath 通讯包类的包目录名
	 */
	public Communicator(String remoteIp, int remotePort, int localPort, String packagePath){
		this.remoteIp = remoteIp;
		this.remotePort = remotePort;
		this.localPort = localPort;
		this.packagePath = packagePath;
		this.serverAccpetClients = new ArrayList<Socket>();
	}
	
	
	/**
	 * 关闭通讯器 
	 */
	public void close() {
		try {
			client.getInputStream().close();
			client.getOutputStream().close();
			client.close();
			for (Socket socket : serverAccpetClients) {
				socket.getInputStream().close();
				socket.getOutputStream().close();
				socket.close();
			}
			server.close();
		} catch (Exception e) {
		}
		serverAccpetClients = null;
		client = null;
		server = null;
	}
	
	
	/**
	 * 获取远程设备IP
	 * @return
	 */
	public String getRemoteIp() {
		return remoteIp;
	}


	/**
	 * 获取最大重试次数
	 * @return
	 */
	public int getMaxRetryTimes() {
		return maxRetryTimes;
	}


	/**
	 * 设置最大重试次数
	 * @param maxRetryTimes
	 */
	public void setMaxRetryTimes(int maxRetryTimes) {
		this.maxRetryTimes = maxRetryTimes;
	}


	/**
	 * 获取远程设备端口
	 * @return
	 */
	public int getRemotePort() {
		return remotePort;
	}


	/**
	 * 获取信息序列号
	 * @return
	 */
	public int getSerialNo() {
		return serialNo;
	}


	/**
	 * 获取超时时间（毫秒）
	 * @return
	 */
	public int getTimeout() {
		return timeout;
	}


	/**
	 * 设置超时时间（毫秒）
	 * @param timeout
	 */
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}


	/**
	 * 获取起始标志位
	 * @return
	 */
	public byte[] getStartFlags() {
		return startFlags;
	}


	/**
	 * 设置起始标志位
	 * @param b1
	 * @param b2
	 */
	public void setStartFlags(int b1, int b2) {
		this.startFlags[0] = (byte) b1;
		this.startFlags[1] = (byte) b2;
	}


	/**
	 * 获取结束标志位
	 * @return
	 */
	public byte[] getEndFlags() {
		return endFlags;
	}


	/**
	 * 设置结束标志位
	 * @param b1
	 * @param b2
	 */
	public void setEndFlags(int b1, int b2) {
		this.endFlags[0] = (byte) b1;
		this.endFlags[1] = (byte) b2;
	}


	/**
	 * 获取通讯包类的包名
	 * @return
	 */
	public String getPackagePath() {
		return packagePath;
	}
	
	
	
	
}
