package com.jimi.jiminal.core;

import java.io.IOException;
import java.net.SocketTimeoutException;

import com.jimi.jiminal.callback.OnPackageArrivedListener;
import com.jimi.jiminal.entity.BasePackage;
import com.jimi.jiminal.exception.PackageParseException;

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


	@Override
	public void connect() throws IOException {
		super.connect();
	}


	@Override
	public void startServer(OnPackageArrivedListener onPackageArrivedListener) {
		super.startServer(onPackageArrivedListener);
	}

	/**
	 * 发送一个包到远程设备，返回回复包<br>
	 * 如果远程设备在指定的超时时间（默认5秒）内没有回复正确的包，将再次发送包，直到成功或重试指定次数（默认3次）为止
	 * @param p
	 * @throws IOException
	 */
	public BasePackage send(BasePackage p) throws IOException {
		try {
			return super.send(p);
		} catch (PackageParseException | SocketTimeoutException e) {
			if (retriedTimes < maxRetryTimes) {
				retriedTimes++;
				System.err.println("系统未在规定时间内收到正确的回复包，已重新发送包....(第" + retriedTimes + "次)");
				try {
					client.close();
					super.connect();
					return send(p);
				} catch (IOException e1) {
					throw new IOException("重新连接服务器失败");
				}
			} else {
				// 重置重试次数
				retriedTimes = 0;
				throw new IOException("重新发包次数超过最大重试次数");
			}
		} catch (IOException e) {
			throw e;
		}
	}

}
