package com.jimi.psh.callback;

import java.io.IOException;

/**
 * 连接完成监听器
 * @author 沫熊工作室 <a href="http://www.darhao.cc">www.darhao.cc</a>
 */
public abstract class OnConnectedListener {
	/**
	 * 成功时调用
	 */
	public abstract void onSucceed();
	
	/**
	 * 失败时调用
	 */
	public abstract void onFailed(IOException e);
}
