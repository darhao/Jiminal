package cc.darhao.jiminal.exception;

import java.util.List;

import cc.darhao.dautils.api.BytesParser;

/**
 * 包解析异常
 * <br>
 * <b>2017年12月28日</b>
 * @author 沫熊工作室 <a href="http://www.darhao.cc">www.darhao.cc</a>
 */
public class PackageParseException extends Exception {

	public PackageParseException(List<Byte> bytes) {
		super(BytesParser.parseBytesToHexString(bytes));
	}
	
}
