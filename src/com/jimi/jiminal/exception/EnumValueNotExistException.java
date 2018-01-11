package com.jimi.jiminal.exception;

import java.util.List;

/**
 * CRC校验异常
 * @author 沫熊工作室 <a href="http://www.darhao.cc">www.darhao.cc</a>
 */
public class EnumValueNotExistException extends PackageParseException {

	public EnumValueNotExistException(List<Byte> bytes) {
		super(bytes);
	}
	
}
