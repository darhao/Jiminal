package com.jimi.smt.eps.pkh.callback;

import java.io.IOException;
import com.jimi.smt.eps.pkh.entity.Package;

/**
 * 回复包到达监听器
 * @author 沫熊工作室 <a href="http://www.darhao.cc">www.darhao.cc</a>
 */
public abstract class OnReplyPackageArrivedListener {

	/**
	 * 回复包到达时调用
	 */
	public abstract void onReplyPackageArrived(Package r);
	
	/**
	 * 捕获到IO异常
	 * @param exception
	 */
	public abstract void onCatchIOException(IOException exception);
}
