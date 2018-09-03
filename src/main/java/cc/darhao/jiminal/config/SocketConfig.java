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
	 * 起始标志位 
	 */
	private byte[] startFlags = new byte[]{(byte) 0x80, (byte) 0x80};
	
	/**
	 * 结束标志位
	 */
	private byte[] endFlags = new byte[]{(byte) 0x0D, (byte) 0x0A};
	
	/**
	 * 结束标志位的去语义位（如果文中出现结束标志位，则会用该标志作为前缀，注释掉结束标志位）
	 */
	private byte[] endInvalidFlags = new byte[]{(byte) 0xFF, (byte) 0xFF};

	
	/**
	 * 获取起始标志位
	 * @return
	 */
	public byte[] getStartFlags() {
		return startFlags;
	}

	
	/**
	 * 设置起始标志位
	 * @param b1
	 * @param b2
	 */
	public void setStartFlags(int b1, int b2) {
		startFlags[0] = (byte) b1;
		startFlags[1] = (byte) b2;
	}

	
	/**
	 * 获取结束标志位
	 * @return
	 */
	public byte[] getEndFlags() {
		return endFlags;
	}

	
	/**
	 * 设置结束标志位
	 * @param b1
	 * @param b2
	 */
	public void setEndFlags(int b1, int b2) {
		endFlags[0] = (byte) b1;
		endFlags[1] = (byte) b2;
	}

	
	/**
	 * 获取结束标志位去语义位
	 * @return
	 */
	public byte[] getEndInvalidFlags() {
		return endInvalidFlags;
	}

	
	/**
	 * 设置结束标志位去语义位
	 * @param b1
	 * @param b2
	 */
	public void setEndInvalidFlags(int b1, int b2) {
		endInvalidFlags[0] = (byte) b1;
		endInvalidFlags[1] = (byte) b2;
	}


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
