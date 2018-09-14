package cc.darhao.jiminal.socket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import cc.darhao.jiminal.callback.JiminalServerCallback;
import cc.darhao.jiminal.config.PackageConfig;

/**
 * 通讯器服务端，负责监听通讯器的主动连接，并创建对应的通讯器
 * <br>
 * <b>2018年8月22日</b>
 * @author 沫熊工作室 <a href="http://www.darhao.cc">www.darhao.cc</a>
 */
public class JiminalServer {

	/**
	 * 服务器
	 */
	private ServerSocket server;
	
	/**
	 * 本地服务监听端口
	 */
	private int listenPort;
	
	/**
	 * 通讯包配置
	 */
	private PackageConfig packageConfig;
	
	/**
	 * 服务器回调对象
	 */
	private JiminalServerCallback callback;
	
	
	/**
	 * 创建一个通讯器
	 * @param listenPort
	 * @param packageConfig
	 * @param callback
	 */
	public JiminalServer(int listenPort, PackageConfig packageConfig, JiminalServerCallback callback){
		this.listenPort = listenPort;
		this.packageConfig = packageConfig;
		this.callback = callback;
	}
	

	/**
	 * 开始监听连接
	 */
	public void listenConnect() {
		try {
			server = new ServerSocket(listenPort);
		} catch (IOException e) {
			e.printStackTrace();
		}
		while(true) {
			//捕获socket
			Socket socket;
			try {
				socket = server.accept();
				//创建通讯器
				Jiminal jiminal = new Jiminal(socket, packageConfig, callback);
				//开启接收子线程
				jiminal.startReceiveThread();
				//回调
				callback.onCatchClient(jiminal);
			} catch (IOException e) {
				if(e.getMessage().equals("Socket is closed")) {
					break;
				}else {
					e.printStackTrace();
				}
			}
		}
	}


	/**
	 * 关闭服务监听
	 */
	public void close() {
		try {
			server.close();
		} catch (Exception e) {
		}
	}
	
	
	/**
	 * 获取本地服务监听端口
	 */
	public int getListenPort() {
		return listenPort;
	}

	
}
