package cc.darhao.jiminal.entity;

import cc.darhao.jiminal.annotation.Parse;
import cc.darhao.jiminal.annotation.Protocol;

@Protocol(0x4C)
public class JustForTestLoginPackage extends BasePackage {

	@Parse({0,6})
	private String centerControllerMAC;

	public String getCenterControllerMAC() {
		return centerControllerMAC;
	}

	public void setCenterControllerMAC(String centerControllerMAC) {
		this.centerControllerMAC = centerControllerMAC;
	}
	
}
