package com.jimi.psh.exception;

import java.util.List;

/**
 * CRC校验异常
 * @author 沫熊工作室 <a href="http://www.darhao.cc">www.darhao.cc</a>
 */
public class CRCException extends PackageParseException {

	public CRCException(List<Byte> bytes) {
		super(bytes);
	}
	
}
