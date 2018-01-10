package com.jimi.psh.exception.runtime;

import com.jimi.psh.entity.BasePackage;
import com.jimi.psh.util.FieldUtil;

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
