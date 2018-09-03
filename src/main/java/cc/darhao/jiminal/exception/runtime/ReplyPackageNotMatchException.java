package cc.darhao.jiminal.exception.runtime;

import cc.darhao.dautils.api.FieldUtil;
import cc.darhao.jiminal.pack.BasePackage;

/**
 * 未匹配到回复包异常
 * @author 沫熊工作室 <a href="http://www.darhao.cc">www.darhao.cc</a>
 */
public class ReplyPackageNotMatchException extends RuntimeException {
	
	public ReplyPackageNotMatchException(BasePackage p) {
		System.err.println("=========↓异常包对象信息↓==========");
		FieldUtil.print(p);
		System.err.println("=========↑异常包对象信息↑==========");
	}
	
}
