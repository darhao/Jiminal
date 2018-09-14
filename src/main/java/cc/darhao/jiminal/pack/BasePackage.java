package cc.darhao.jiminal.pack;

/**
 * 通讯协议包基类
 * 
 * @author 沫熊工作室 <a href="http://www.darhao.cc">www.darhao.cc</a>
 */
public class BasePackage {

	/**
	 * 包长度
	 */
	public byte length;
	/**
	 * 协议类型
	 */
	public String protocol = "";
	
	/**
	 * 信息序列号
	 */
	public Short serialNo;
	
}
