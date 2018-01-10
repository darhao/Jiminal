package com.jimi.psh.exception;

import java.util.List;

/**
 * 协议未找到异常
 * @author 沫熊工作室 <a href="http://www.darhao.cc">www.darhao.cc</a>
 */
public class ProtocolNotMatchException extends PackageParseException{

	public ProtocolNotMatchException(List<Byte> bytes) {
		super(bytes);
	}
	
}
