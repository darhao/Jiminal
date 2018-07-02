package cc.darhao.jiminal.core;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;

import cc.darhao.dautils.api.ClassScanner;
import cc.darhao.jiminal.callback.OnPackageArrivedListener;
import cc.darhao.jiminal.exception.PackageParseException;

/**
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
 * 1.通讯包类名须以<b>"BasePackage"</b>结尾<br>
 * 2.对应的回复包类名须以<b>"ReplyPackgae"</b>结尾<br>
 * 3.支持五种字段类型：字符串、布尔、整数、日期、枚举，分别对应的java类型为String, boolean, int, Date, Enum
 * <br>编写枚举类型时，枚举元素的顺序需要对应协议的值顺序，如：<br>
 * <pre>
public enum ErrorCode {
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
	protected short serialNo = 0;
	
	/**
	 * 远程设备的ip地址
	 */
	protected String remoteIp;
	
	
//	/**
//	 * 本机IP
//	 */
//	protected String localIp;
	
	
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
	 * 结束标志位的去语义位（如果文中出现结束标志位，则会用该标志作为前缀，注释掉结束标志位）
	 */
	protected byte[] endInvalidFlags = new byte[]{(byte) 0xFF, (byte) 0xFF};
	
	/**
	 * 通讯包类的包名
	 */
	protected String packagePath;
	
	/**
	 * 通讯包类列表
	 */
	protected List<Class> packageClasses;
	
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
	protected Communicator(int localPort, String packagePath){
		this(null, 0, localPort, packagePath);
	}
	
	
	/**
	 * 创建一个通讯器
	 * @param remoteIp 远程设备IP
	 * @param remotePort 远程设备端口
	 * @param packagePath 通讯包类的包目录名
	 */
	protected Communicator(String remoteIp ,int remotePort, String packagePath){
		this(remoteIp, remotePort, 0, packagePath);
	}
	
	/**
	 * 创建一个通讯器
	 * @param remoteIp 远程设备IP
	 * @param remotePort 远程设备端口
	 * @param localPort 本地服务端口
	 * @param packagePath 通讯包类的包目录名
	 */
	protected Communicator(String remoteIp, int remotePort, int localPort, String packagePath){
		this.remoteIp = remoteIp;
		this.remotePort = remotePort;
		this.localPort = localPort;
		this.packagePath = packagePath;
		this.packageClasses = ClassScanner.searchClass(packagePath);
		this.serverAccpetClients = new ArrayList<Socket>();
	}
	
	
	/**
	 * 作为客户端，连接到服务器
	 * @throws IOException
	 */
	protected void connect() throws IOException {
		client = new Socket();
		SocketAddress endpoint = new InetSocketAddress(remoteIp, remotePort);
		client.connect(endpoint, timeout);
		client.setSoTimeout(timeout);
	}
	
	
	/**
	 * 作为服务器，初始化本地服务器，并开始监听数据包到达事件
	 */
	protected void startServer(OnPackageArrivedListener onPackageArrivedListener){
		try {
			server = new ServerSocket(localPort);
			while(true) {
				//捕获socket
				Socket socket = server.accept();
				//存到列表中
				serverAccpetClients.add(socket);
				//创建单独线程
				new Thread("服务子线程") {
					
					String clientSocketIp = socket.getLocalAddress().getHostAddress();
					String serverSocketIp = socket.getInetAddress().getHostAddress();
					Socket threadSocket = socket;
					boolean isContent = false;
					List<Byte> bytes = new ArrayList<Byte>();
					byte b1, b2 = -2;
					//0xFF连续次数超过64次将认为远程设备已断开连接
					int remoteOfflineFlagsCount = 0;
					
					public void run() {
						while(true) {
							try {
								//读一个字节，缓存一个字节
								b1 = b2;
								b2 = (byte) threadSocket.getInputStream().read();
								System.out.print(Integer.toHexString(b2) + " ");
								//判断是否是FF
								if(b2 == -1) {
									remoteOfflineFlagsCount++;
									if(remoteOfflineFlagsCount == 64) {
										//双向关闭
										socket.close();
										serverAccpetClients.remove(socket);
										break;
									}
								}else {
									remoteOfflineFlagsCount = 0;
								}
								//判断正文
								if(isContent) {
									bytes.add((byte) b2);
								}
								//判断包头
								if(b1 == startFlags[0] && b2 == startFlags[1]) {
									isContent = true;
								}
								//判断包尾
								if(b1 == endFlags[0] && b2 == endFlags[1]) {
									//判断前两个字节是否是去语义标识符
									try {
										byte e1 = bytes.get(bytes.size() - 4);
										byte e2 = bytes.get(bytes.size() - 3);
										if(e1 ==  endInvalidFlags[0] && e2 == endInvalidFlags[1]) {
											//去掉去语义标识符
											bytes.remove(bytes.size() - 3);
											bytes.remove(bytes.size() - 3);
											//返回主循环
											continue;
										}
									}catch (IndexOutOfBoundsException e) {
									}
									isContent = false;
									bytes.remove(bytes.size() - 1);
									bytes.remove(bytes.size() - 1);
									//把bytes解析成Entity
									BasePackage p = PackageParser.parse(bytes, packageClasses, false);
									bytes.clear();
									//构建回复包
									BasePackage r = PackageParser.createReplyPackage(p, packageClasses);					
									//设置ip
									r.receiverIp = p.senderIp = clientSocketIp;
									r.senderIp = p.receiverIp = serverSocketIp;
									//调用监听器方法
									onPackageArrivedListener.onPackageArrived(p, r);
									//回复对方
									List<Byte> rBytes = PackageParser.serialize(r);
									//检测文中是否存在结束位，如果有则用去语义标识注释
									byte a1, a2 = 0;
									for (int i = 0; i < rBytes.size(); i++) {
										a1 = a2;
										a2 = rBytes.get(i);
										if(a1 == endFlags[0] && a2 == endFlags[1]) {
											rBytes.add(i - 1, endInvalidFlags[0]);
											rBytes.add(i, endInvalidFlags[1]);
											i += 2;
										}
									}
									//加上起始位和结束位
									rBytes.add(0, startFlags[0]);
									rBytes.add(1, startFlags[1]);
									rBytes.add(endFlags[0]);
									rBytes.add(endFlags[1]);
									//发送
									for (Byte b : rBytes) {
										System.err.print(Integer.toHexString(b) + " ");
										socket.getOutputStream().write(b);
									}
									System.err.println();
								}
							} catch (IOException e) {
								onPackageArrivedListener.onCatchIOException(e);
								return;
							} catch (PackageParseException | IndexOutOfBoundsException e) {
								e.printStackTrace();
								bytes.clear();
							}
						}
					}
				}.start();
			}
		} catch (IOException e) {
			onPackageArrivedListener.onCatchIOException(e);
			return;
		}
	}
	
	
	/**
	 * 发送一个包到远程设备，返回回复包
	 * @param p
	 * @return
	 * @throws IOException
	 * @throws PackageParseException
	 */
	protected BasePackage send(BasePackage p) throws IOException, PackageParseException {
		boolean isContent = false;
		byte b1, b2 = -2;
		if (client == null || !client.isConnected()) {
			throw new IOException("客户端未连接上服务端");
		}
		if(serialNo >= 65536) {
			serialNo = 0;
		}
		p.serialNo = serialNo++;
		List<Byte> bytes = PackageParser.serialize(p);
		try {
			//检测文中是否存在结束位，如果有则用去语义标识注释
			byte a1, a2 = 0;
			for (int i = 0; i < bytes.size(); i++) {
				a1 = a2;
				a2 = bytes.get(i);
				if(a1 == endFlags[0] && a2 == endFlags[1]) {
					bytes.add(i - 1, endInvalidFlags[0]);
					bytes.add(i, endInvalidFlags[1]);
					i += 2;
				}
			}
		} catch (IndexOutOfBoundsException e3) {
		}
		// 加上起始位和结束位
		bytes.add(0, startFlags[0]);
		bytes.add(1, startFlags[1]);
		bytes.add(endFlags[0]);
		bytes.add(endFlags[1]);
		// 发送
		for (Byte b : bytes) {
			System.out.print(Integer.toHexString(b) + " ");
			client.getOutputStream().write(b);
		}
		System.out.println();
		// 接收回复包
		bytes = new ArrayList<Byte>();
		//0xFF连续次数超过64次将认为远程设备已断开连接
		int remoteOfflineFlagsCount = 0;
		while (true) {
			try {
				// 读一个字节，缓存一个字节
				b1 = b2;
				b2 = (byte) client.getInputStream().read();
				System.out.print(Integer.toHexString(b2) + " ");
				//判断是否是FF
				if(b2 == -1) {
					remoteOfflineFlagsCount++;
					if(remoteOfflineFlagsCount == 64) {
						throw new IOException("远程设备已断开连接");
					}
				}else {
					remoteOfflineFlagsCount = 0;
				}
				// 判断正文
				if (isContent) {
					bytes.add((byte) b2);
				}
				// 判断包头
				if (b1 == startFlags[0] && b2 == startFlags[1]) {
					isContent = true;
				}
				// 判断包尾
				if (b1 == endFlags[0] && b2 == endFlags[1]) {
					//判断前两个字节是否是去语义标识符
					try {
						byte e1 = bytes.get(bytes.size() - 4);
						byte e2 = bytes.get(bytes.size() - 3);
						if(e1 ==  endInvalidFlags[0] && e2 == endInvalidFlags[1]) {
							//去掉去语义标识符
							bytes.remove(bytes.size() - 3);
							bytes.remove(bytes.size() - 3);
							//返回主循环
							continue;
						}
					}catch (IndexOutOfBoundsException e) {
					}
					isContent = false;
					bytes.remove(bytes.size() - 1);
					bytes.remove(bytes.size() - 1);
					// 把bytes解析成Entity
					BasePackage r = PackageParser.parse(bytes, packageClasses, true);
					// 重置重试次数
					retriedTimes = 0;
					//设置ip
					r.receiverIp = p.senderIp = client.getLocalAddress().getHostAddress();
					r.senderIp = p.receiverIp = client.getInetAddress().getHostAddress();
					return r;
				}
			} catch (IndexOutOfBoundsException e) {
				e.printStackTrace();
			}
		}
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
	 * 获取本地端口
	 * @return
	 */
	public int getLocalPort() {
		return localPort;
	}


//	/**
//	 * 获取本地ip
//	 * @return
//	 */
//	public String getLocalIp() {
//		return localIp;
//	}


	/**
	 * 获取因未收到正确的回复包而重新发送给服务器包的次数
	 * @return
	 */
	public int getRetriedTimes() {
		return retriedTimes;
	}


	/**
	 * 获取客户端Socket
	 * @return
	 */
	public Socket getClient() {
		return client;
	}


	/**
	 * 获取服务端ServerSocket
	 * @return
	 */
	public ServerSocket getServer() {
		return server;
	}


	/**
	 * 获取与服务端连接的Socket列表
	 * @return
	 */
	public List<Socket> getServerAccpetClients() {
		return serverAccpetClients;
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
	 * 获取结束标志位去语义位
	 * @return
	 */
	public byte[] getEndInvalidFlags() {
		return endInvalidFlags;
	}


	/**
	 * 设置结束标志位去语义位
	 * @param b1
	 * @param b2
	 */
	public void setEndInvalidFlags(int b1, int b2) {
		this.endInvalidFlags[0] = (byte) b1;
		this.endInvalidFlags[1] = (byte) b2;
	}
	

	/**
	 * 获取通讯包类的包名
	 * @return
	 */
	public String getPackagePath() {
		return packagePath;
	}
	
	
	
	
}
