package cc.darhao.jiminal.config;

/**
 * 默认配置类
 * <br>
 * <b>2018年8月23日</b>
 * @author 沫熊工作室 <a href="http://www.darhao.cc">www.darhao.cc</a>
 */
public class SocketConfig {
	
	/**
	 * 最长回复时间
	 */
	private long maxReplyTime = 5000;
	
	
	/**
	 * 获取最长回复时间
	 * @return
	 */
	public long getMaxReplyTime() {
		return maxReplyTime;
	}


	/**
	 * 设置最长回复时间
	 * @param maxReplyTime
	 */
	public void setMaxReplyTime(long maxReplyTime) {
		this.maxReplyTime = maxReplyTime;
	}

}
