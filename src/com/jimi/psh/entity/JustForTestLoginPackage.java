package com.jimi.psh.entity;

import com.jimi.psh.annotation.Parse;
import com.jimi.psh.annotation.Protocol;

@Protocol(0x4C)
public class JustForTestLoginPackage extends Package {

	@Parse({0,6})
	private String centerControllerMAC;

	public String getCenterControllerMAC() {
		return centerControllerMAC;
	}

	public void setCenterControllerMAC(String centerControllerMAC) {
		this.centerControllerMAC = centerControllerMAC;
	}
	
}
