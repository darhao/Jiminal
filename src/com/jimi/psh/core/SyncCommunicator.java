package com.jimi.psh.core;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import com.jimi.psh.callback.OnPackageArrivedListener;
import com.jimi.psh.entity.Package;
import com.jimi.psh.exception.PackageParseException;

/**
 * 几米物联 —— 包通讯类（同步版） <br>
 * <b>2018年1月3日</b>
 * 
 * @author 沫熊工作室 <a href="http://www.darhao.cc">www.darhao.cc</a>
 */
public class SyncCommunicator extends Communicator {

	/**
	 * 创建一个通讯器
	 * 
	 * @param localPort
	 *            本地服务端口
	 * @param packagePath
	 *            通讯包类的包目录名
	 */
	public SyncCommunicator(int localPort, String packagePath) {
		super(null, 0, localPort, packagePath);
	}

	/**
	 * 创建一个通讯器
	 * 
	 * @param remoteIp
	 *            远程设备IP
	 * @param remotePort
	 *            远程设备端口
	 * @param packagePath
	 *            通讯包类的包目录名
	 */
	public SyncCommunicator(String remoteIp, int remotePort, String packagePath) {
		super(remoteIp, remotePort, 0, packagePath);
	}

	/**
	 * 创建一个通讯器
	 * 
	 * @param remoteIp
	 *            远程设备IP
	 * @param remotePort
	 *            远程设备端口
	 * @param localPort
	 *            本地服务端口
	 * @param packagePath
	 *            通讯包类的包目录名
	 */
	public SyncCommunicator(String remoteIp, int remotePort, int localPort, String packagePath) {
		super(remoteIp, remotePort, localPort, packagePath);
	}

	/**
	 * 作为客户端，连接到服务器
	 * 
	 * @throws IOException
	 */
	public void connect() throws IOException {
		client = new Socket(remoteIp, remotePort);
		client.setSoTimeout(timeout);
	}

	/**
	 * 作为服务器，初始化本地服务器，并开始监听数据包到达事件
	 */
	public void startServer(OnPackageArrivedListener onPackageArrivedListener) {
		try {
			server = new ServerSocket(localPort);
			while (true) {
				// 捕获socket
				Socket socket = server.accept();
				// 存到列表中
				serverAccpetClients.add(socket);
				// 创建单独线程
				new Thread() {

					private Socket threadSocket = socket;
					private boolean isContent = false;
					ArrayList<Byte> bytes = new ArrayList<Byte>();
					byte b1, b2 = -2;

					public void run() {
						while (true) {
							try {
								// 读一个字节，缓存一个字节
								b1 = b2;
								b2 = (byte) threadSocket.getInputStream().read();
								System.out.print(Integer.toHexString(b2) + " ");
								// 判断正文
								if (isContent) {
									bytes.add((byte) b2);
								}
								// 判断流结束
								if (b2 == -1) {
									break;
								}
								// 判断包头
								if (b1 == startFlags[0] && b2 == startFlags[1]) {
									isContent = true;
									bytes.clear();
								}
								// 判断包尾
								if (b1 == endFlags[0] && b2 == endFlags[1]) {
									isContent = false;
									bytes.remove(bytes.size() - 1);
									bytes.remove(bytes.size() - 1);
									// 把bytes解析成Entity
									Package p = PackageParser.parse(bytes, packagePath, false);
									// 构建回复包
									Package r = PackageParser.createReplyPackage(p, packagePath);
									// 调用监听器方法
									if (onPackageArrivedListener == null) {
										continue;
									}
									onPackageArrivedListener.onPackageArrived(p, r);
									// 回复对方
									List<Byte> bytes = PackageParser.serialize(r, packagePath);
									// 加上起始位和结束位
									bytes.add(0, startFlags[0]);
									bytes.add(1, startFlags[1]);
									bytes.add(endFlags[0]);
									bytes.add(endFlags[1]);
									// 发送
									for (Byte b : bytes) {
										System.err.print(Integer.toHexString(b) + " ");
										socket.getOutputStream().write(b);
									}
									System.err.println();
								}
							} catch (IOException e) {
								onPackageArrivedListener.onCatchIOException(e);
								return;
							} catch (PackageParseException e) {
								System.err.println("包解析出错，已抛弃该包");
							}
						}
					};
				}.start();

			}
		} catch (IOException e) {
			onPackageArrivedListener.onCatchIOException(e);
			return;
		}
	}

	/**
	 * 发送一个包到远程设备，返回回复包<br>
	 * 如果远程设备在指定的超时时间（默认5秒）内没有回复正确的包，将再次发送包，直到成功或重试指定次数（默认3次）为止
	 * 
	 * @param p
	 * @throws IOException
	 */
	public Package send(Package p) throws IOException {

		boolean isContent = false;
		byte b1, b2 = -2;

		try {
			if (client == null || !client.isConnected()) {
				throw new IOException("客户端未连接上服务端");
			}
			p.serialNo = serialNo++;
			List<Byte> bytes = PackageParser.serialize(p, packagePath);
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
			while (true) {
				// 读一个字节，缓存一个字节
				b1 = b2;
				b2 = (byte) client.getInputStream().read();
				System.out.print(Integer.toHexString(b2) + " ");
				// 判断正文
				if (isContent) {
					bytes.add((byte) b2);
				}
				// 判断流结束
				if (b2 == -1) {
					break;
				}
				// 判断包头
				if (b1 == startFlags[0] && b2 == startFlags[1]) {
					isContent = true;
					bytes.clear();
				}
				// 判断包尾
				if (b1 == endFlags[0] && b2 == endFlags[1]) {
					isContent = false;
					bytes.remove(bytes.size() - 1);
					bytes.remove(bytes.size() - 1);
					// 把bytes解析成Entity
					Package r = PackageParser.parse(bytes, packagePath, true);
					// 重置重试次数
					retriedTimes = 0;
					return r;
				}
			}
		} catch (PackageParseException | SocketTimeoutException e) {
			if (retriedTimes < maxRetryTimes) {
				retriedTimes++;
				System.err.println("系统未在规定时间内收到正确的回复包，已重新发送包....(第" + retriedTimes + "次)");
				try {
					client.close();
					client = new Socket(remoteIp, remotePort);
					client.setSoTimeout(timeout);
					return send(p);
				} catch (IOException e1) {
					throw new IOException("重新连接服务器失败");
				}
			} else {
				throw new IOException("重新发包次数超过最大重试次数");
			}
		} catch (IOException e) {
			throw e;
		}
		// 重置重试次数
		retriedTimes = 0;
		return null;
	}

}
