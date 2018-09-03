package cc.darhao.jiminal.callback;

import cc.darhao.jiminal.pack.BasePackage;
import cc.darhao.jiminal.socket.Jiminal;

/**
 * 基础回调
 * <br>
 * <b>2018年9月3日</b>
 * @author 几米物联自动化部-洪达浩
 */
public interface JiminalBaseCallback {

	/**
	 * 包到达时调用
	 * @param p 对方发来的包
	 * @param r 在该方法返回时，我方会回复对方的包
	 * @param session 收到包的会话
	 */
	 void onPackageArrived(BasePackage p, BasePackage r, Jiminal session);
	 

	/**
	 * 回复包到达时调用
	 * @param r 对方发来的回复包
	 * @param session 收到包的会话
	 */
	 void onReplyArrived(BasePackage r, Jiminal session);
	 
	 
	/**
	 * 会话遇到异常时调用
	 * @param e 异常实体
	 * @param session 产生异常的会话
	 */
	 void onCatchException(Exception e, Jiminal session);
}
