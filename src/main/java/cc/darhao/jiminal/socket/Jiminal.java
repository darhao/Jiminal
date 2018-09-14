package cc.darhao.jiminal.socket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import cc.darhao.dautils.api.BytesParser;
import cc.darhao.dautils.api.DateUtil;
import cc.darhao.jiminal.callback.JiminalBaseCallback;
import cc.darhao.jiminal.callback.JiminalCallback;
import cc.darhao.jiminal.callback.JiminalServerCallback;
import cc.darhao.jiminal.config.PackageConfig;
import cc.darhao.jiminal.config.SocketConfig;
import cc.darhao.jiminal.exception.PackageParseException;
import cc.darhao.jiminal.pack.BasePackage;
import cc.darhao.jiminal.parse.PackageParser;

/**
 * 可配置项：<br>
 * <ul>
 * 	<li>超时时间（单位毫秒）
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
public class Jiminal {

	/**
	 * 信息序列号
	 */
	private short serialNo;
	
	/**
	 * 远程设备的ip地址
	 */
	private String remoteIp;
	
	/**
	 * 远程设备通信端口
	 */
	private int remotePort;
	
	/**
	 *套接字
	 */
	private Socket socket;
	
	/**
	 * 回调对象
	 */
	private JiminalBaseCallback callback;
	
	/**
	 * 已经处理的包的序列号集合：所有放在这个集合的序列号都是已经回复过的
	 */
	private Set<Short> handledPackageSerialNos;
	
	/**
	 * 已发送的包的序列号与是否收到回复的映射：所有发送出去的包是否已经收到回复都会在这里记录
	 */
	private Map<Short, Boolean> sendSerialNoReplys;
	
	/**
	 * 通讯协议配置
	 */
	private SocketConfig socketConfig;
	
	/**
	 * 通讯包配置
	 */
	private PackageConfig packageConfig;
	
	/**
	 * 指令接收线程
	 */
	private Thread receiveThread;
	
	
	/**
	 * 创建一个通讯器
	 * @param remoteIp 远程设备IP
	 */
	public Jiminal(String remoteIp, int remotePort, PackageConfig packageConfig, JiminalCallback callback){
		init(remoteIp, remotePort, packageConfig);
		this.callback = callback;
		this.socket = new Socket();
	}


	Jiminal(Socket socket, PackageConfig packageConfig, JiminalServerCallback callback) {
		init(socket.getInetAddress().getHostAddress(), socket.getPort(), packageConfig);
		this.callback = callback;
		this.socket = socket;
	}
	
	
	private void init(String remoteIp, int remotePort, PackageConfig packageConfig) {
		this.remoteIp = remoteIp;
		this.remotePort = remotePort;
		this.packageConfig = packageConfig;
		this.socketConfig = new SocketConfig();
		serialNo = 0;
		handledPackageSerialNos = new HashSet<>();
		sendSerialNoReplys = new HashMap<>();
	}


	/**
	 * 作为客户端，连接到服务器
	 * @throws IOException
	 */
	public void connect() {
		//连接服务器
		SocketAddress endpoint = new InetSocketAddress(remoteIp, remotePort);
		try {
			socket.connect(endpoint);
			//开启回复处理子线程
			startReceiveThread();
			//回调
			((JiminalCallback)callback).onConnected();
		} catch (IOException e) {
			//回调
			callback.onCatchException(e, this);
		}
	}


	/**
	 * 发送一个包到远程Jiminal
	 * @param p 欲发送的包
	 */
	public void send(BasePackage p) {
		//生成信息序列号
		p.serialNo = genSerialNo();
		//把该信息序列号加入到等待回复的映射中
		sendSerialNoReplys.put(p.serialNo, false);
		realSend(p);
		//开启回复检测子线程
		startCheckReplyThread(p);
	}


	private void realSend(BasePackage p) {
		//解包：序列化成字节集并去语义化、加上头尾
		List<Byte> bytes = unJig(p);
		// 发送数据
		try {
			writeData(bytes);
		}catch (Exception e) {
			//回调
			callback.onCatchException(e, this);
		}
	}


	private synchronized Short genSerialNo() {
		serialNo %= 65536;
		serialNo++;
		return serialNo;
	}


	private void startCheckReplyThread(BasePackage p) {
		new Thread( () -> {
			//检测映射该信息序列号的包是否收到回复
			for (Entry<Short, Boolean> entry : sendSerialNoReplys.entrySet()) {
				if(entry.getKey().shortValue() == p.serialNo.shortValue()) {
					//等待N秒
					try {
						Thread.sleep(socketConfig.getMaxReplyTime());
					} catch (InterruptedException e) {
					}
					if(!entry.getValue()) {
						callback.onCatchException(new RuntimeException("对方未在规定时间内收到正确的回复包，已断开连接"), Jiminal.this);
						receiveThread.interrupt();
						close();
						break;
					}			
					break;
				}
			}
		}).start();
	}


	private synchronized void writeData(List<Byte> bytes) throws IOException {
		for (Byte b : bytes) {
			socket.getOutputStream().write(b);
		}
		System.out.println();
	}


	void startReceiveThread() {
		receiveThread = new Thread( () -> {
			try {
				while (true) {
					if(receiveThread.isInterrupted()) {
						break;
					}
					
					//存放字节集
					List<Byte> bytes = new ArrayList<>();
					
					//0xFF连续次数超过64次将认为远程设备已断开连接
					int remoteOfflineFlagsCount = 0;
					
					//读第一个字节识别包长度
					byte b1 = (byte) socket.getInputStream().read();
					bytes.add(b1);
					int length = Byte.toUnsignedInt(b1);
					
					//读取包长度大小的字节集
					for (int i = 0; i < length; i++) {
						byte b2 = (byte) socket.getInputStream().read();
						//判断是否是FF
						if(b2 == -1) {
							remoteOfflineFlagsCount++;
							if(remoteOfflineFlagsCount == 64) {
								throw new IOException("远程设备已断开连接");
							}
						}else {
							remoteOfflineFlagsCount = 0;
						}
						bytes.add(b2);
					}
					
					//拼包：去掉头尾，去语义化，并且反序列化包
					BasePackage p = jigsaw(bytes);
					//处理业务
					handle(p);
				}
			} catch (Exception e) {
				callback.onCatchException(e, this);
			}
		});
		receiveThread.start();
	}
	
	
	/**
	 * 关闭通讯器 
	 */
	public void close() {
		try {
			receiveThread.interrupt();
			socket.close();
		} catch (Exception e) {
		}
	}


	private void handle(BasePackage p) {
		//判断是否是回复包
		if(p.getClass().getSimpleName().contains("ReplyPackage")) {
			//匹配信息序列号
			for (Entry<Short, Boolean> entry : sendSerialNoReplys.entrySet()) {
				if(entry.getKey().shortValue() == p.serialNo.shortValue()) {
					//判断是否该信息序列号对应的值为false
					if(!entry.getValue()) {
						entry.setValue(true);
						//业务处理
						callback.onReplyArrived(p, this);
					}
					return;
				}
			}
		}else {
			//匹配信息序列号
			for (Short serialNo : handledPackageSerialNos) {
				if(serialNo.shortValue() == p.serialNo.shortValue()) {
					return;
				}
			}
			handledPackageSerialNos.add(p.serialNo);
			//生成对应回复包（仅包含信息序列号）
			BasePackage r = PackageParser.createReplyPackage(p, packageConfig);
			//业务处理
			callback.onPackageArrived(p, r, this);
			//回复
			realSend(r);
		}
	}


	private List<Byte> unJig(BasePackage p) {
		//序列化
		List<Byte> bytes = PackageParser.serialize(p);
		//打印日志
		System.out.println("[发送至] [" + remoteIp + ":"+ remotePort +"] [" + DateUtil.yyyyMMddHHmmss(new Date()) + "]");
		System.out.println(BytesParser.parseBytesToHexString(bytes));
		return bytes;
	}


	private BasePackage jigsaw(List<Byte> bytes) throws PackageParseException{
		//打印日志
		System.out.println("[接收自] [" + remoteIp + ":"+ remotePort +"] [" + DateUtil.yyyyMMddHHmmss(new Date()) + "]");
		System.out.println(BytesParser.parseBytesToHexString(bytes));
		// 把bytes解析成Entity
		return PackageParser.parse(bytes, packageConfig);
	}


	/**
	 * 获取远程设备IP
	 * @return
	 */
	public String getRemoteIp() {
		return remoteIp;
	}


	/**
	 * 获取信息序列号
	 * @return
	 */
	public int getSerialNo() {
		return serialNo;
	}


	/**
	 * 配置通讯协议
	 * @param socketConfig
	 */
	public void setSocketConfig(SocketConfig socketConfig) {
		this.socketConfig = socketConfig;
	}


}
