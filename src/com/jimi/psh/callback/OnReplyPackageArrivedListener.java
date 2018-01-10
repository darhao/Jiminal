package com.jimi.psh.callback;

import java.io.IOException;

import com.jimi.psh.entity.BasePackage;

/**
 * 回复包到达监听器
 * @author 沫熊工作室 <a href="http://www.darhao.cc">www.darhao.cc</a>
 */
public interface OnReplyPackageArrivedListener {

	/**
	 * 回复包到达时调用
	 */
	public void onReplyPackageArrived(BasePackage r);
	
	/**
	 * 捕获到IO异常
	 * @param exception
	 */
	public void onCatchIOException(IOException exception);
}
