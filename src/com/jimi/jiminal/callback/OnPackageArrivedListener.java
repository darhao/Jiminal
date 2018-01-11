package com.jimi.jiminal.callback;

import java.io.IOException;

import com.jimi.jiminal.entity.BasePackage;

/**
 * 包到达监听器
 * @author 沫熊工作室 <a href="http://www.darhao.cc">www.darhao.cc</a>
 */
public interface OnPackageArrivedListener {

	/**
	 * 客户端包到达时调用，该方法返回时将发送的参数中的回复包
	 * @param p 客户端发来的包
	 * @param r 该方法返回时发送给客户端的回复包(请为该包的各项成员赋值)
	 */
	public void onPackageArrived(BasePackage p , BasePackage r);
	
	/**
	 * 捕获到IO异常
	 * @param exception
	 */
	public void onCatchIOException(IOException exception);
}
