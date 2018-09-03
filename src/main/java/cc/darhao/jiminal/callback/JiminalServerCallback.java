package cc.darhao.jiminal.callback;

import cc.darhao.jiminal.socket.Jiminal;

/**
 * 服务器回调监听器
 * @author 沫熊工作室 <a href="http://www.darhao.cc">www.darhao.cc</a>
 */
public interface JiminalServerCallback extends JiminalBaseCallback{
	
	/**
	 * 有客户端连入时调用
	 * @param session 连入的客户端实体
	 */
	void onCatchClient(Jiminal session);
	
}
