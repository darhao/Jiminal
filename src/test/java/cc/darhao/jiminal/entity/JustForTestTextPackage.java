package cc.darhao.jiminal.entity;

import cc.darhao.jiminal.annotation.Parse;
import cc.darhao.jiminal.annotation.Protocol;
import cc.darhao.jiminal.core.BasePackage;

@Protocol(0x03)
public class JustForTestTextPackage extends BasePackage {

	@Parse(value={0,16},utf8=true)
	private String content;


	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	
}
