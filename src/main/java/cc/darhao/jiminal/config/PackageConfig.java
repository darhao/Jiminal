package cc.darhao.jiminal.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import cc.darhao.jiminal.pack.BasePackage;

/**
 * 通讯包管理类
 * <br>
 * <b>2018年8月24日</b>
 * @author 沫熊工作室 <a href="http://www.darhao.cc">www.darhao.cc</a>
 */
public class PackageConfig {

	private Map<Class<? extends BasePackage>, Boolean> packageInfos;
	
	
	public PackageConfig() {
		packageInfos = new HashMap<>();
	}
	
	
	/**
	 * 添加通讯包类，并声明该通讯包类属于我方主动发送或是对方主动发送【无须添加回复包类】
	 * @param clazz 类
	 * @param isOwner 是否属于我方主动发送
	 */
	public void add(Class<? extends BasePackage> clazz, boolean isOwner) {
		try {
			packageInfos.put(clazz, isOwner);
			Class replyClass = Class.forName(clazz.getName().replace("Package", "ReplyPackage"));
			packageInfos.put(replyClass, isOwner);
		} catch (ClassNotFoundException e) {
			System.out.println("没有找到包：" + e.getMessage());
		}
	}
	
	
	/**
	 * 获取所有通讯包类
	 * @return 通讯包类集合
	 */
	public Set<Class<? extends BasePackage>> getAll(){
		return packageInfos.keySet();
	}
	
	
	/**
	 * 返回该通讯包类是否属于我方主动发送
	 * @param clazz 通讯包类
	 * @return 是否属于我方发送
	 */
	public boolean isOwner(Class<? extends BasePackage> clazz) {
		Boolean result = packageInfos.get(clazz);
		if(result == null) {
			throw new NullPointerException("没有找到该通讯包类");
		}else {
			return result;
		}
	}
}
