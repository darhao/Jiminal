package cc.darhao.jiminal.exception.runtime;

import java.util.List;

/**
 * 客户端未连接服务器异常
 * @author 沫熊工作室 <a href="http://www.darhao.cc">www.darhao.cc</a>
 */
public class EnumClassNotFoundException extends PackageParseRuntimeException {
	
	public EnumClassNotFoundException(List<Byte> bytes) {
		super(bytes);
	}
	
}
