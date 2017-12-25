package com.jimi.smt.eps.pkh.socket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import com.jimi.smt.eps.pkh.callback.OnConnectedListener;
import com.jimi.smt.eps.pkh.callback.OnPackageArrivedListener;
import com.jimi.smt.eps.pkh.callback.OnReplyPackageArrivedListener;
import com.jimi.smt.eps.pkh.entity.Package;
import com.jimi.smt.eps.pkh.exception.CRCException;
import com.jimi.smt.eps.pkh.exception.ClientNotConnectedException;
import com.jimi.smt.eps.pkh.exception.ProtocolNotMatchException;
import com.jimi.smt.eps.pkh.parser.PackageParser;

/**
 * 包通讯类<br>可以做服务器亦可以做客户端<br>
 * 需要指定一个通讯包的包路径<br>
 * 可配置项：<br>
 * <ul>
 * 	<li>起始、结束位
 * 	<li>超时时间
 * 	<li>最多重试次数
 * </ul>
 * @author 沫熊工作室 <a href="http://www.darhao.cc">www.darhao.cc</a>
 */
public class Communicator {
	
	/**
	 * 远程设备通信端口
	 */
	private int remotePort = 23333;
	
	/**
	 * 本地服务监听端口
	 */
	private int localPort = 23333;
	
	/**
	 * 信息序列号
	 */
	private int serialNo = 0;
	
	/**
	 * 中控的ip地址
	 */
	private String remoteIp;
	
	/**
	 * 已重试次数
	 */
	private int retriedTimes = 0;
	
	/**
	 * 最大重试次数
	 */
	private int maxRetryTimes = 3;
	
	/**
	 * 超时时间（毫秒 ）
	 */
	private int timeout = 5000;
	
	/**
	 * 起始标志位 
	 */
	private byte[] startFlags = new byte[]{(byte) 0x80, (byte) 0x80};
	
	/**
	 * 结束标志位
	 */
	private byte[] endFlags = new byte[]{(byte) 0x0D, (byte) 0x0A};
	
	
	/**
	 * 通讯包类的包名
	 */
	private String packagePath;
	
	
	/**
	 *客户端 
	 */
	private Socket client;
	
	/**
	 * 服务器
	 */
	private ServerSocket server;
	
	
	/**
	 * 创建一个通讯器
	 * @param localPort 本地服务端口
	 * @param packagePath 通讯包类的包目录名
	 */
	public Communicator(int localPort, String packagePath){
		this.localPort = localPort;
		this.packagePath = packagePath;
	}
	
	
	/**
	 * 创建一个通讯器
	 * @param remoteIp 远程设备IP
	 * @param remotePort 远程设备端口
	 * @param packagePath 通讯包类的包目录名
	 */
	public Communicator(String remoteIp ,int remotePort, String packagePath){
		this.remoteIp = remoteIp;
		this.remotePort = remotePort;
		this.packagePath = packagePath;
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
	}
	
	
	/**
	 * 作为客户端，连接到中控<br>PS：此操作为异步操作
	 */
	public void connect(OnConnectedListener onConnectedListener) {
		new Thread() {
			@Override
			public void run() {
				try {
					client = new Socket(remoteIp, remotePort);
					client.setSoTimeout(timeout);
					//调用连接成功
					onConnectedListener.onSucceed();
				} catch (IOException e) {
					if(onConnectedListener != null) {
						onConnectedListener.onFailed(e);
						e.printStackTrace();
					}
				}
			}
		}.start();
	}


	/**
	 * 作为服务器，初始化本地服务器，并开始监听数据包到达事件
	 */
	public void initServer(OnPackageArrivedListener onPackageArrivedListener) {
		new Thread() {
			@Override
			public void run() {
				try {
					server = new ServerSocket(localPort);
					while(true) {
						//捕获socket
						Socket socket = server.accept();
						socket.setSoTimeout(timeout);
						//创建单独线程
						new Thread() {
							
							private Socket threadSocket = socket;
							private boolean isContent = false;
							ArrayList<Byte> bytes = new ArrayList<Byte>();
							int b1, b2 = -2;
							
							public void run() {
								
								while(true) {
									try {
										//读一个字节，缓存一个字节
										b1 = b2;
										b2 = threadSocket.getInputStream().read();
										//判断正文
										if(isContent) {
											bytes.add((byte) b2);
										}
										//判断流结束
										if(b2 == -1) {
											break;
										}
										//判断包头
										if(b1 == startFlags[0] && b2 == startFlags[1]) {
											isContent = true;
											bytes.clear();
										}
										//判断包尾
										if(b1 == endFlags[0] && b2 == endFlags[1]) {
											isContent = false;
											bytes.remove(bytes.size() - 1);
											bytes.remove(bytes.size() - 1);
											//把bytes解析成Entity
											Package p = PackageParser.parse(bytes, packagePath, false);
											//构建回复包
											Package r = PackageParser.createReplyPackage(p, packagePath);
											//调用监听器方法
											if(onPackageArrivedListener == null) {
												continue;
											}
											onPackageArrivedListener.onPackageArrived(p, r);
											//回复对方
											List<Byte> bytes = PackageParser.serialize(r, packagePath);
											//加上起始位和结束位
											bytes.add(0, startFlags[0]);
											bytes.add(1, startFlags[1]);
											bytes.add(bytes.size() - 1, endFlags[0]);
											bytes.add(bytes.size() - 1, endFlags[1]);
											//发送
											for (Byte b : bytes) {
												socket.getOutputStream().write(b);
											}
										}
									} catch (IOException e) {
										e.printStackTrace();
										onPackageArrivedListener.onCatchIOException(e);
										return;
									} catch (CRCException | ProtocolNotMatchException e) {
										e.printStackTrace();
									}
								}
							};
						}.start();
						
					}
				} catch (IOException e) {
					e.printStackTrace();
					onPackageArrivedListener.onCatchIOException(e);
					return;
				}
			}
		}.start();
	}


	/**
	 * 发送一个包到远程设备，监听回来的包<br>如果远程设备在指定的超时时间（默认5秒）内没有回复正确的包，将再次发送包，直到成功或重试指定次数（默认3次）为止
	 * @param p
	 * @param listener
	 */
	public void send(Package p, OnReplyPackageArrivedListener onReplyPackageArrivedListener){
		new Thread() {
			
			private boolean isContent = false;
			int b1, b2 = -2;
			
			public void run(){
				try {
					if(client == null) {
						throw new ClientNotConnectedException();
					}
					p.setSerialNo(serialNo++);
					List<Byte> bytes = PackageParser.serialize(p, packagePath);
					//加上起始位和结束位
					bytes.add(0, startFlags[0]);
					bytes.add(1, startFlags[1]);
					bytes.add(bytes.size() - 1, endFlags[0]);
					bytes.add(bytes.size() - 1, endFlags[1]);
					//发送
					for (Byte b : bytes) {
						client.getOutputStream().write(b);
					}
					//接收回复包
					while(true) {
						//读一个字节，缓存一个字节
						b1 = b2;
						b2 = client.getInputStream().read();
						//判断正文
						if(isContent) {
							bytes.add((byte) b2);
						}
						//判断流结束
						if(b2 == -1) {
							break;
						}
						//判断包头
						if(b1 == startFlags[0] && b2 == startFlags[1]) {
							isContent = true;
							bytes.clear();
						}
						//判断包尾
						if(b1 == endFlags[0] && b2 == endFlags[1]) {
							isContent = false;
							bytes.remove(bytes.size() - 1);
							bytes.remove(bytes.size() - 1);
							//把bytes解析成Entity
							Package p = PackageParser.parse(bytes, packagePath, true);
							onReplyPackageArrivedListener.onReplyPackageArrived(p);
							//退出循环
							break;
						}
					}
					//重置重试次数
					retriedTimes = 0;
				} catch (CRCException | ProtocolNotMatchException | SocketTimeoutException e) {
					e.printStackTrace();
					if(retriedTimes < maxRetryTimes) {
						retriedTimes++;
						send(p, onReplyPackageArrivedListener);
					}
				} catch (IOException e) {
					e.printStackTrace();
					onReplyPackageArrivedListener.onCatchIOException(e);
					return;
				}
			}
		}.start();
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
