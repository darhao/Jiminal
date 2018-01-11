package com.jimi.jiminal.core;

import java.io.IOException;
import java.net.SocketTimeoutException;

import com.jimi.jiminal.callback.OnConnectedListener;
import com.jimi.jiminal.callback.OnPackageArrivedListener;
import com.jimi.jiminal.callback.OnReplyPackageArrivedListener;
import com.jimi.jiminal.entity.BasePackage;
import com.jimi.jiminal.exception.PackageParseException;

/**
 * 几米物联 —— 包通讯类（异步版）
 * <br>
 * <b>2018年1月3日</b>
 * @author 沫熊工作室 <a href="http://www.darhao.cc">www.darhao.cc</a>
 */
public class AsyncCommunicator extends Communicator{
	
	
	/**
	 * 创建一个通讯器
	 * @param localPort 本地服务端口
	 * @param packagePath 通讯包类的包目录名
	 */
	public AsyncCommunicator(int localPort, String packagePath){
		super(null, 0, localPort, packagePath);
	}
	
	
	/**
	 * 创建一个通讯器
	 * @param remoteIp 远程设备IP
	 * @param remotePort 远程设备端口
	 * @param packagePath 通讯包类的包目录名
	 */
	public AsyncCommunicator(String remoteIp ,int remotePort, String packagePath){
		super(remoteIp, remotePort, 0, packagePath);
	}
	
	/**
	 * 创建一个通讯器
	 * @param remoteIp 远程设备IP
	 * @param remotePort 远程设备端口
	 * @param localPort 本地服务端口
	 * @param packagePath 通讯包类的包目录名
	 */
	public AsyncCommunicator(String remoteIp, int remotePort, int localPort, String packagePath){
		super(remoteIp, remotePort, localPort, packagePath);
	}
	
	
	/**
	 * 作为客户端，连接到服务器
	 */
	public void connect(OnConnectedListener onConnectedListener) {
		new Thread("客户端连接线程") {
			@Override
			public void run() {
				try {
					AsyncCommunicator.super.connect();
					//调用连接成功
					onConnectedListener.onSucceed();
				} catch (IOException e) {
					if(onConnectedListener != null) {
						onConnectedListener.onFailed(e);
					}
				}
			}
		}.start();
	}


	@Override
	public void startServer(OnPackageArrivedListener onPackageArrivedListener) {
		new Thread("服务器守护线程") {
			@Override
			public void run() {
				AsyncCommunicator.super.startServer(onPackageArrivedListener);
			}
		}.start();
	}


	/**
	 * 发送一个包到远程设备，监听回来的包<br>如果远程设备在指定的超时时间（默认5秒）内没有回复正确的包，将再次发送包，直到成功或重试指定次数（默认3次）为止
	 * @param p
	 * @param listener
	 */
	public void send(BasePackage p, OnReplyPackageArrivedListener onReplyPackageArrivedListener){
		new Thread("客户端发送数据线程") {
			public void run(){
				try {
					BasePackage r = AsyncCommunicator.super.send(p);
					onReplyPackageArrivedListener.onReplyPackageArrived(r);
				} catch (PackageParseException | SocketTimeoutException e) {
					if(retriedTimes < maxRetryTimes) {
						retriedTimes++;
						System.err.println("系统未在规定时间内收到正确的回复包，已重新发送包....(第"+retriedTimes+"次)");
						try {
							client.close();
							AsyncCommunicator.super.connect();
							send(p, onReplyPackageArrivedListener);
						} catch (IOException e1) {
							onReplyPackageArrivedListener.onCatchIOException(new IOException("重新连接服务器失败"));
						}
					}else {
						onReplyPackageArrivedListener.onCatchIOException(new IOException("重新发包次数超过最大重试次数"));
					}
				} catch (IOException e) {
					onReplyPackageArrivedListener.onCatchIOException(e);
					return;
				}
			}
		}.start();
	}
	
}
